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
package com.electrazoom.remote;

import com.electrazoom.networking.*;
import com.electrazoom.rpc.WifiConfig;
import com.electrazoom.rpc.WifiSetupRpc;
import com.electrazoom.rpc.WifiSetupRpcServer;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class WifiConfigServer implements WifiSetupRpc {
    private static final Logger LOG = LoggerFactory.getLogger(WifiConfigServer.class);
    private NetworkManagerHelper networkManagerHelper;
    private DeviceInfo adapter;

    public static void main(String... args) {
        WifiConfigServer server = new WifiConfigServer();
        try {
            server.start();
            if (args.length > 0 && args[0].equals("test")) {
                WifiConfig.Scan scan = server.getWirelessNetworks(null);
                LOG.debug("scan = {}", scan);
                server.shutdown();
            } else {
                WifiSetupRpcServer rpc = new WifiSetupRpcServer(server);
                rpc.startService();
            }
        } catch (DBusException e) {
            LOG.error("Error starting server", e);
        } catch (InterruptedException e) {
            LOG.info("Stopped", e);
        }
    }

    public void start() throws DBusException, InterruptedException {
        networkManagerHelper = new NetworkManagerHelper();
        List<DeviceInfo> wifiDevices = networkManagerHelper.getDevices();
        if (wifiDevices.size() > 0) {
            adapter = wifiDevices.get(0);
            Path devicePath = adapter.getPath();
            LOG.info("Wifi Adapter: " + devicePath);
        }
    }

    public void shutdown() {
        networkManagerHelper.disconnect();
    }


    private WifiConfig.SsidNetwork buildSsidNetwork(Variant<Path> activeConnection,
                                                    Map.Entry<String, List<AccessPointInfo>> entry) {
        WifiConfig.SsidNetwork.Builder ssidNetwork = WifiConfig.SsidNetwork.newBuilder();
        ssidNetwork.setSsid(entry.getKey());
        int strength = 0;
        for (AccessPointInfo accessPointInfo : entry.getValue()) {
            WifiConfig.AccessPoint ap = buildAp(activeConnection, accessPointInfo);
            if (ap.getActive()) {
                ssidNetwork.setActive(true);
            }
            if (ap.getIsPrivate()) ssidNetwork.setIsPrivate(true);
            ssidNetwork.addAps(ap);
            if (strength < ap.getStrength()) strength = ap.getStrength();
        }
        ssidNetwork.setStrength(strength);
        return ssidNetwork.build();
    }

    private WifiConfig.AccessPoint buildAp(Variant<Path> activeConnection, AccessPointInfo accessPointInfo) {
        WifiConfig.AccessPoint.Builder builder = WifiConfig.AccessPoint.newBuilder();
        Path path = accessPointInfo.getPath();
        builder.setActive(activeConnection != null && activeConnection.getValue().getPath().equals(path.getPath()));
        Map<String, Variant> props = accessPointInfo.getProperties();
        Variant<UInt32> vFlags = props.get("Flags");
        if (vFlags != null) {
            long flags = vFlags.getValue().longValue();
            LOG.debug("flags = " + flags);
            builder.setIsPrivate(flags > 0);
        } else {
            LOG.debug("No flags");
            builder.setIsPrivate(false);
        }
        Variant<byte[]> vssid = props.get("Ssid");
        if (vssid != null) builder.setSsid(new String(vssid.getValue()));
        Variant<UInt32> vFreq = props.get("Frequency");
        if (vFreq != null) builder.setFrequency(vFreq.getValue().intValue());
        Variant<String> vHwAddress = props.get("HwAddress");
        if (vHwAddress != null) builder.setMac(vHwAddress.getValue());
        Variant<UInt32> vMaxBitrate = props.get("MaxBitrate");
        if (vMaxBitrate != null) builder.setMaxBitrate(vMaxBitrate.getValue().intValue());
        Variant<Byte> vStrength = props.get("Strength");
        if (vStrength != null) builder.setStrength(vStrength.getValue().intValue());
        Variant<Integer> lastSeen = props.get("LastSeen");
        if (lastSeen != null) builder.setTimeLastSeen(lastSeen.getValue());
        return builder.build();
    }

    @Override
    public WifiConfig.Scan getWirelessNetworks(WifiConfig.ScanRequest in) {
        WifiConfig.Scan.Builder scanBuilder = WifiConfig.Scan.newBuilder();
        try {
            Variant<Path> activeConnection = networkManagerHelper.getActiveConnectionPath();
            List<DeviceInfo> wifiDevices = networkManagerHelper.getDevices();
            if (wifiDevices.size() > 0) {
                Path devicePath = adapter.getPath();
                Map<String, List<AccessPointInfo>> infoMap = networkManagerHelper.getAccessPoints(devicePath);
                WifiConfig.AccessPoint activeAccessPoint = null;
                WifiConfig.SsidNetwork activeNetwork = null;
                for (Map.Entry<String, List<AccessPointInfo>> entry : infoMap.entrySet()) {
                    WifiConfig.SsidNetwork ssidNetwork = buildSsidNetwork(activeConnection, entry);
                    if (ssidNetwork.getActive()) {
                        for (WifiConfig.AccessPoint accessPoint : ssidNetwork.getApsList()) {
                            if (accessPoint.getActive()) activeAccessPoint = accessPoint;
                        }
                        activeNetwork = ssidNetwork;
                    }
                    scanBuilder.addNetworks(ssidNetwork);
                }
                if (activeNetwork != null) {
                    scanBuilder.setActiveAccessPoint(activeAccessPoint);
                    scanBuilder.setActiveNetwork(activeNetwork);
                }
            }
        } catch (Exception e) {
            LOG.error("NetworkManager error", e);
        }
        networkManagerHelper.disconnect();
        LOG.debug("getAccessPoints: " + scanBuilder.toString());

        return scanBuilder.build();
    }

    @Override
    public WifiConfig.JoinResult join(WifiConfig.JoinRequest in) {
        final WifiConfig.AccessPoint joinAp;
        if (in.hasAp()) joinAp = in.getAp();
        else joinAp = null;
        String psk = in.getPsk();
        WifiConfig.JoinResult.Builder joinResult = WifiConfig.JoinResult.newBuilder();
        try {
            ConnectionActiveInfo activeInfo;
            if (joinAp != null) {
                activeInfo = networkManagerHelper.joinAp(adapter, joinAp.getMac(), psk);
            } else {
                activeInfo = networkManagerHelper.join(adapter, in.getSsid(), psk);
            }
            if (activeInfo == null) {
                joinResult.setIsSuccessful(false);
                joinResult.setErrorMessage("Unable to join " + in.getSsid());
            } else {
                if (networkManagerHelper.waitForConnection()) joinResult.setIsSuccessful(true);
                else {
                    joinResult.setIsSuccessful(false);
                    joinResult.setErrorMessage("Timeout");
                }
            }
        } catch (DBusException | NetworkOutOfRangeException e) {
            LOG.error("join: {}", in, e);
            joinResult.setIsSuccessful(false);
            joinResult.setErrorMessage("Unable to join " + in.getSsid() + ": " + e.getLocalizedMessage());
        }
        return joinResult.build();
    }

    @Override
    public WifiConfig.DisconnectResult disconnect(WifiConfig.DisconnectRequest in) {
        WifiConfig.DisconnectResult.Builder result = WifiConfig.DisconnectResult.newBuilder();
        try {
            if (networkManagerHelper.disconnectActiveConnection()) {
                result.setIsSuccessful(true);
            }
            else {
                result.setIsSuccessful(false);
                result.setErrorMessage("Unable to disconnect");
            }
        } catch (DBusException e) {
            LOG.error("Error Disconnecting from wifi", e);
            result.setIsSuccessful(false);
            result.setErrorMessage(e.getMessage());
        }
        return result.build();
    }

    @Override
    public WifiConfig.AuthenticateResult authenticate(WifiConfig.AuthenticateRequest in) {
        LOG.debug("authenticate {}", in);
        // TODO allow only authenticated users to modify settings
        return null;
    }

    @Override
    public WifiConfig.OperationModeResult changeOperationMode(WifiConfig.OperationModeRequest in) {
        LOG.debug("changeOperationMode {}", in);
        // TODO launch/stop services
        return null;
    }

    @Override
    public void onConnect(String characteristic) {
        LOG.info("Connection: {}", characteristic);
    }

    @Override
    public void onError(String source, String message) {
        LOG.error("Source: {} Message: {}", source, message);
    }
}
