package com.electrazoom.sample;

import com.electrazoom.networking.AccessPointInfo;
import com.electrazoom.networking.ConnectionActiveInfo;
import com.electrazoom.networking.DeviceInfo;
import com.electrazoom.networking.NetworkManagerHelper;
import org.freedesktop.dbus.*;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.freedesktop.networkmanager.Constants.NM_ACTIVE_CONNECTION_STATE.*;

/**
 * Created by geoff on 3/19/18.
 * Sample program that lists the APs and their signal strength. Joins the SSID specified on the command line
 */
public class TestNetworkManager {
    private static final Logger LOG = LoggerFactory.getLogger(TestNetworkManager.class);

    public static void main(String[] args) throws DBusException {
        if (args.length < 1) {
            LOG.error("Usage main ssid [psk]");
            System.exit(1);
        }
        final String ssid = args[0];
	final String psk;
        if (args.length > 1) psk = args[1];
	else psk = null;

        NetworkManagerHelper nm = new NetworkManagerHelper();

        try {
            List<DeviceInfo> wifiDevices = nm.getDevices();
            if (wifiDevices.size() > 0) {
                Path devicePath = wifiDevices.get(0).getPath();
                LOG.info("Device: " + devicePath);
                Map<String, List<AccessPointInfo>> infoMap = nm.getAccessPoints(devicePath);
                for (Map.Entry<String, List<AccessPointInfo>> entry : infoMap.entrySet()) {
                    LOG.info(entry.getKey() + ":");
                    for (AccessPointInfo accessPointInfo : entry.getValue()) {
                        Map<String, Variant> apProps = accessPointInfo.getProperties();
                        LOG.info("   " + apProps.get("HwAddress") + " " + apProps.get("Strength") + "%");
                    }
                }
                boolean deactived = nm.disconnectActiveConnection();

                if (!deactived) {
                    LOG.error("Unable to deactive previous connection");
                    System.exit(2);
                }
                List<AccessPointInfo> ap = infoMap.get(ssid);
                if (ap != null) {
                    // boolean success = join(nm, wifiDevices, ap, psk);
                    // if (success) LOG.info("Joined " + ssid);
                    // else LOG.error("Unable to join " + ssid);
		    ConnectionActiveInfo aci = nm.join(wifiDevices.get(0), ssid, psk);
		    boolean success = nm.waitForState(aci, 1000, ACTIVATED).equals(ACTIVATED);
		    if (success) LOG.info("SUCCESS!");
		    else LOG.error("Unable to connect to " + ssid);
                }
                else LOG.error("No ssid " + ssid);

            }
        }
        catch (Exception e) {
            LOG.error("NetworkManager error", e);
        }
        nm.disconnect();
    }

    private static boolean join(NetworkManagerHelper nm, List<DeviceInfo> wifiDevices, List<AccessPointInfo> ap, String psk) throws DBusException {
        ConnectionActiveInfo aci = nm.join(ap, wifiDevices.get(0), psk);
        return nm.waitForState(aci, 10000, ACTIVATED).equals(ACTIVATED);
    }

}
