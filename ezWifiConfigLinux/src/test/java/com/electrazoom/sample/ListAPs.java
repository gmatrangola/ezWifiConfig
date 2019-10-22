package com.electrazoom.sample;

import com.electrazoom.networking.AccessPointInfo;
import com.electrazoom.networking.DeviceInfo;
import com.electrazoom.networking.NetworkManagerHelper;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ListAPs {
    private static final Logger LOG = LoggerFactory.getLogger(ListAPs.class);
    private final NetworkManagerHelper networkManager;

    public static void main(String[] args) throws DBusException {
        new ListAPs();
    }


    public ListAPs() throws DBusException {
        networkManager = new NetworkManagerHelper();

        List<DeviceInfo> wifiDevices = networkManager.getDevices();
        if (wifiDevices.size() > 0) {
            listAps(wifiDevices.get(0));
            showActiveConnectionPath();
        }
        else LOG.error("No wifi Devices found");
        networkManager.disconnect();
    }

    private void listAps(DeviceInfo wifiDevice) throws DBusException {
        LOG.info("listing on " + wifiDevice.getPath().getPath());
        Map<String, List<AccessPointInfo>> aps = networkManager.getAccessPoints(wifiDevice.getPath());
        LOG.info("Found " + aps.size() + " APs");
        for (Map.Entry<String, List<AccessPointInfo>> entry : aps.entrySet()) {
            List<AccessPointInfo> infos = entry.getValue();
            for (AccessPointInfo info : infos) {
                LOG.info("{} {} {}", entry.getKey(), info.getPath().toString(), info.getProperties().get("HwAddress"));
            }
        }
    }

    private void showActiveConnectionPath() throws DBusException {
        Variant<Path> activeConnectionPath = networkManager.getActiveConnectionPath();
        if (activeConnectionPath != null) LOG.info("Active AP Path: {}", activeConnectionPath);
        else LOG.info("Not connected");
    }
}
