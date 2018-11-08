/*
 * ezWifiConfig - Wifi Configuration over BLE
 * Copyright (c) 2018. Geoffrey Matrangola, electrazoom.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>
 *
 *     This program is also available under a commercial license. If you wish
 *     to redistribute this library and derivative work for commercial purposes
 *     please see ProtoBLE.com to obtain a proprietary license that will fit
 *     your needs.
 */
package com.electrazoom.networking;

import org.freedesktop.DBus;
import org.freedesktop.NetworkManager;
import org.freedesktop.Pair;
import org.freedesktop.dbus.*;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.networkmanager.Constants.NM_DEVICE_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Helps deal with NetworkManager interfaces over the Linux DBus
 */
public class NetworkManagerHelper {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkManagerHelper.class);

    public static final String NM_PATH = "/org/freedesktop/NetworkManager";
    public static final String NETWORK_MANAGER_NAME = NetworkManager.class.getCanonicalName();
    public static final Variant<String> INFRASTRUCTURE = new Variant<>("infrastructure");
    public static final Variant TYPE_802_11 = new Variant("802-11-wireless");
    public static final Variant METHOD_AUTO = new Variant("auto");
    public static final int TIMEOUT = 20000;

    private NetworkManager nm;
    private DBusConnection conn;
    private final String version;
    private final DBus.Properties nmProp;

    /**
     * Connect to the DBUS, get a remote object reference to the NetworkManager, and store the version.
     * @throws DBusException if something bad happens
     */
    public NetworkManagerHelper() throws DBusException {
        conn = getConn();
        nmProp = conn.getRemoteObject(NETWORK_MANAGER_NAME,
                NM_PATH, DBus.Properties.class);
        nm = conn.getRemoteObject(NETWORK_MANAGER_NAME, NM_PATH,
                NetworkManager.class);

        version = nmProp.Get(NETWORK_MANAGER_NAME, "Version");
    }

    /**
     * Join the AP with the strongest signal in the aps list
     * @param aps List of AccessPointInfos
     * @param adapter DeviceInfo that will do the connection.
     * @param psk String Preshared key
     * @return ConnectionActiveInfo for the joined network.
     * @throws DBusException if something bad happens
     */
    public ConnectionActiveInfo join(List<AccessPointInfo> aps, DeviceInfo adapter, String psk) throws DBusException {
        AccessPointInfo best = null;
        double last = 0;
        for (AccessPointInfo ap : aps) {
            Variant<Byte> strengthVar = ap.getProperties().get("Strength");
            Byte str = strengthVar.getValue();
            if (str > last) {
                best = ap;
                last = str;
            }
        }
        return join(best, adapter, psk);
    }

    /**
     * Find all the Active connections
     * @return List of ConnectionActiveInfo objects
     * @throws DBusException if something bad happens.
     */
    public List<ConnectionActiveInfo> getActiveConnections() throws DBusException {
        List<Path> activeConnections = nmProp.Get(NETWORK_MANAGER_NAME, "ActiveConnections");
        List<ConnectionActiveInfo> connectionInfo = new ArrayList<>();
        for (Path active : activeConnections) {
            NetworkManager.Connection.Active ac = getConn().getRemoteObject(NETWORK_MANAGER_NAME, active.getPath(), NetworkManager.Connection.Active.class);
            DBus.Properties acProps = getConn().getRemoteObject(NETWORK_MANAGER_NAME, active.getPath(),
                    DBus.Properties.class);
            try {
                Map<String, Variant> map = acProps.GetAll(NetworkManager.Connection.Active.class.getCanonicalName());
                connectionInfo.add(new ConnectionActiveInfo(map, active, ac));
            }
            catch (Exception e) {
                LOG.error("Unable to get properties for {}", active.getPath());
            }
        }

        return connectionInfo;
    }

    /**
     * Join an AP
     * @param apInfo AccessPointInfo to join
     * @param adapter DeviceInfo of the Wifi adapter that will join the AP
     * @param psk String presharedkey password
     * @return ConnectionActiveInfo for the AP that was joined
     * @throws DBusException if someting weird happens
     */
    public ConnectionActiveInfo join(AccessPointInfo apInfo, DeviceInfo adapter, String psk) throws DBusException {
        Map<String, Variant> wirelessMap = new HashMap<>();
        Variant ssid = apInfo.getProperties().get("Ssid");
        wirelessMap.put("ssid", ssid);
        wirelessMap.put("mode", INFRASTRUCTURE);
        Map<String, Variant> conncectionMap = new HashMap<>();

        String connectionName = "NM_CONNECT" + new String((byte[]) ssid.getValue()) + "wlan";
        LOG.debug("join ID: " + connectionName);
        conncectionMap.put("id", new Variant<>(connectionName));
        conncectionMap.put("type", TYPE_802_11);
        conncectionMap.put("autoconnect", new Variant<Boolean>(true));
        UUID uuid = UUID.randomUUID();
        conncectionMap.put("uuid", new Variant<>(uuid.toString()));

        Map<String, Variant> ipv4Map = new HashMap<>();
        ipv4Map.put("method", METHOD_AUTO);

        Map<String, Map<String, Variant>> newConnection = new HashMap<>();
        newConnection.put("connection", conncectionMap);
        newConnection.put("802-11-wireless", wirelessMap);
        newConnection.put("ipv4", ipv4Map);

        Variant<UInt32> flags = apInfo.getProperties().get("Flags");
        LOG.debug("join: flags = " + flags);
        if (flags.getValue().longValue() > 0) {
            Map<String, Variant> wifiSecurity = new HashMap<>();
            wifiSecurity.put("psk", new Variant<>(psk));
            newConnection.put("802-11-wireless-security", wifiSecurity);
        }

        NetworkManager nm = getConn().getRemoteObject(NETWORK_MANAGER_NAME, NM_PATH, NetworkManager.class);
        Pair<DBusInterface, DBusInterface> result = nm.AddAndActivateConnection(newConnection, adapter.getObj(),
                apInfo.getObj());
        return buildConnectionActiveInfo(connectionName, result);
    }

    public ConnectionActiveInfo join(DeviceInfo adapter, String ssid, String psk) throws DBusException, NetworkOutOfRangeException {
        Map<String, List<AccessPointInfo>> networks = getAccessPoints(adapter.getPath());
        List<AccessPointInfo> aps = networks.get(ssid);
        if (aps == null) {
            throw new NetworkOutOfRangeException(ssid);
        }
        AccessPointInfo strongest = null;
        int best = 0;
        for (AccessPointInfo ap : aps) {
            Variant<Byte> vStrength = ap.getProperties().get("Strength");
            int strength = vStrength.getValue().byteValue();
            if (strength > best) {
                strongest = ap;
                best = strength;
            }
        }
        return join(strongest, adapter, psk);
    }

    public ConnectionActiveInfo joinAp(DeviceInfo adapter, String bssid, String psk) throws DBusException, NetworkOutOfRangeException {
        LOG.debug("joinAp [{}]", bssid);
        AccessPointInfo ap = findAp(adapter, bssid);
        if (ap != null) return join(ap, adapter, psk);
        throw new NetworkOutOfRangeException(bssid);
    }

    private ConnectionActiveInfo buildConnectionActiveInfo(String connectionName, Pair<DBusInterface, DBusInterface> result) throws DBusException {
        // todo figure out how to use result to get the ActiveConnection
        List<ConnectionActiveInfo> acs = getActiveConnections();
        for (ConnectionActiveInfo ac : acs) {
            Map<String, Variant> properties = ac.getProperties();
            Variant<String> id = properties.get("Id");
            if (id.getValue().equals(connectionName)) {
                return ac;
            }
        }

        return null;
    }

    /**
     * Disconnect from ap
     * @param ap Aconnection.Active for the ap that you want to disconnect from.
     * @throws DBusException on a bad day.
     */
    public void disconnectActiveConnection(NetworkManager.Connection.Active ap) throws DBusException {
        NetworkManager nm = getConn().getRemoteObject(NETWORK_MANAGER_NAME, NM_PATH, NetworkManager.class);
        nm.DeactivateConnection(ap);
    }

    /**
     * Disconnect from all active wifi connections (should only be one)
     * @return true if disconnected or was never connected in the first palce
     * @throws DBusException when bad things happen on the bus
     */
    public boolean disconnectActiveConnection() throws DBusException {
        int wifiStart = 0;
        List<ConnectionActiveInfo> connections = getActiveConnections();
        for (ConnectionActiveInfo connection : connections) {
            if (connection.isWifi()) {
                disconnectActiveConnection(connection.getObj());
                wifiStart++;
            }
        }

        LOG.info("Disconnecting {} wifi connections");

        long start = System.currentTimeMillis();
        int wifiCount = 0;
        do {
            wifiCount = getWifiCount(wifiCount);

        } while(wifiCount > 0 && System.currentTimeMillis() - start > TIMEOUT);

        if (wifiCount > 0) {
            LOG.error("Disconnect Timeout {}", wifiCount);
            return false;
        }

        return true;
    }


    public boolean waitForConnection() throws DBusException {
        long start = System.currentTimeMillis();
        int wifiCount = 0;
        do {
            wifiCount = getWifiCount(wifiCount);
        } while(wifiCount == 0 && System.currentTimeMillis() - start > TIMEOUT);

        if (wifiCount == 0) {
            LOG.error("Connect Timeout {}", wifiCount);
            return false;
        }
        return true;
    }

    private int getWifiCount(int wifiCount) throws DBusException {
        List<ConnectionActiveInfo> connections;
        connections = getActiveConnections();
        for (ConnectionActiveInfo connection : connections) {
            if (connection.isWifi()) {
                wifiCount++;
            }
        }
        LOG.debug("WifiCount: {}", wifiCount);
        return wifiCount;
    }

    public Variant<Path> getActiveConnectionPath() throws DBusException {
        List<ConnectionActiveInfo> connections = getActiveConnections();
        for (ConnectionActiveInfo connection : connections) {
            Map<String, Variant> properties = connection.getProperties();
            if (properties != null && connection.isWifi()) {
                return properties.get("SpecificObject");
            }
        }
        return null;
    }

    private AccessPointInfo findAp(DeviceInfo adapter, String bssid) throws DBusException {
        LOG.debug("findAp [" + bssid + "]");
        Map<String, List<AccessPointInfo>> networks = getAccessPoints(adapter.getPath());
	LOG.debug("networks: {}", networks.size());
        for (List<AccessPointInfo> accessPointInfos : networks.values()) {
            LOG.debug("accessPointInfos: {}", accessPointInfos.size());
            for (AccessPointInfo accessPointInfo : accessPointInfos) {
                Variant<String> vHwAddress = accessPointInfo.getProperties().get("HwAddress");
                LOG.debug("vHwAddress = {}", vHwAddress);
                if (vHwAddress != null) {
                    if(vHwAddress.getValue().equals(bssid)) {
                        return accessPointInfo;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the access points found by adapterPath APs with no SSID are stored under null
     * Multiple APs can have the same SSID (used for campus networks, "mesh" etc.
     * @param adapterPath Wireless Network Adapter returned by the getDevices DeviceInfo field
     * @return Map of Lists of AccessPointInfo objects indexed by the SSID
     * @throws DBusException when things go wrong
     */
    public Map<String, List<AccessPointInfo>> getAccessPoints(Path adapterPath) throws DBusException {
        LOG.info(" ----------- getAccessPoints " + adapterPath);
        Map<String, List<AccessPointInfo>> apInfoMap = new HashMap<>();
        NetworkManager.Device.Wireless wireless = getConn().getRemoteObject(NETWORK_MANAGER_NAME, adapterPath.getPath(),
                NetworkManager.Device.Wireless.class);
        List<Path> aps = wireless.GetAccessPoints();
        for (Path apPath : aps) {
            NetworkManager.AccessPoint ap = getConn().getRemoteObject(NETWORK_MANAGER_NAME, apPath.getPath(),
                    NetworkManager.AccessPoint.class);
            DBus.Properties apProps = getConn().getRemoteObject(NETWORK_MANAGER_NAME, apPath.getPath(),
                    DBus.Properties.class);
            Map<String, Variant> map = apProps.GetAll("org.freedesktop.NetworkManager.AccessPoint");
            AccessPointInfo apInfo = new AccessPointInfo(map, apPath, ap);
            Variant<byte[]> ssidVariant = map.get("Ssid");
            String ssid = new String(ssidVariant.getValue());
            List<AccessPointInfo> entry = apInfoMap.computeIfAbsent(ssid, k -> new ArrayList<>());
            entry.add(apInfo);
        }
        return apInfoMap;
    }

    private DBusConnection getConn() {
        try {
            return DBusConnection.getConnection(DBusConnection.SYSTEM);
        } catch (DBusException e) {
            LOG.error("Unable to get DBusConnection", e);
        }
        return null;
    }

    /**
     * Get the Wireless Network adapters on the DBus
     * @return List of DeviceInfo
     * @throws DBusException when there is a problem on the bus.
     */
    public List<DeviceInfo> getDevices() throws DBusException {
        List<Path> devices = nm.GetDevices();
        List<DeviceInfo> wifiDevices = new ArrayList<>();
        for (Path devicePath : devices) {
            NetworkManager.Device device = getConn().getRemoteObject(NETWORK_MANAGER_NAME, devicePath.getPath(),
                    NetworkManager.Device.class);
            DBus.Properties deviceProps = getConn().getRemoteObject(NETWORK_MANAGER_NAME,
                    devicePath.getPath(),  DBus.Properties.class);
            Map<String, Variant> props = deviceProps.GetAll("org.freedesktop.NetworkManager.Device");
            UInt32 type = (UInt32) props.get("DeviceType").getValue();
            if (type.equals(NM_DEVICE_TYPE.WIFI)) {
                wifiDevices.add(new DeviceInfo(props, devicePath, device));
            }
        }
        return wifiDevices;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Refresh the poperties for a n Interface
     * @param interfaceObjectInfo DBInterfaceObjectInfo child class object
     * @return A map of the new properties
     * @throws DBusException if something bad happens.
     */
    public Map<String, Variant> refreshProperties(DBInterfaceObjectInfo interfaceObjectInfo) throws DBusException {
        DBus.Properties acProps = getConn().getRemoteObject(NETWORK_MANAGER_NAME, interfaceObjectInfo.getPath().getPath(),
                DBus.Properties.class);
        Map<String, Variant> map = acProps.GetAll(interfaceObjectInfo.getObjectName());
        interfaceObjectInfo.setProperties(map);
        return map;
    }

    /**
     * Waits for the Active Connection to change to the desired state
     * TODO Figure out how to use the StateChanged Signal
     * @param aci ConnectionActiveInfo
     * @param timeout int time in ms
     * @param desiredState @see org.freedesktop.networkmanager.Constants.NM_ACTIVE_CONNECTION_STATE
     * @return true if state changed, false on timeout
     * @throws DBusException if something bad happens to the BUS
     */
    public UInt32 waitForState(ConnectionActiveInfo aci, int timeout, UInt32 desiredState) throws DBusException {
        long start = System.currentTimeMillis();
        int i = 0;
        UInt32 lastState = new UInt32(0);
        while (System.currentTimeMillis() - start < timeout) {
            i++;
            try {
                Thread.sleep(500);
                refreshProperties(aci);
            } catch (Exception e) {
                LOG.debug("waitForState Error: ", e);
                return new UInt32(0);
            }
            Set<Map.Entry<String, Variant>> entries = aci.getProperties().entrySet();
            LOG.trace ("=========  check " + i);
            Map<String,Variant> map = new HashMap<>(entries.size());
            for (Map.Entry<String, Variant> entry : entries) {
                LOG.trace("ACI: " + entry.getKey() + " = " + entry.getValue());
                map.put(entry.getKey(), entry.getValue());
            }
            Variant<UInt32> state = map.get("State");
            lastState = state.getValue();
            LOG.debug("lastState: {} desired: {}", lastState, desiredState);
            if (lastState.equals(desiredState)) return state.getValue();
        }
        return lastState;
    }

    /**
     * Disconnect from the DBus
     */
    public void disconnect() {
        conn.disconnect();
    }
}
