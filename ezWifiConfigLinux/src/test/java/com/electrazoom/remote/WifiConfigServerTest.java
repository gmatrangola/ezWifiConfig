package com.electrazoom.remote;

import com.ezsmartdevices.rpc.WifiConfig;
import org.freedesktop.dbus.exceptions.DBusException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

public class WifiConfigServerTest {
    private static final Logger LOG = LoggerFactory.getLogger(WifiConfigServerTest.class);

    @Test
    public void getWirelessNetworks() throws InterruptedException, DBusException {
        WifiConfigServer server = new WifiConfigServer();
        server.start();
        LOG.info("Wifi Networks: " + server.getWirelessNetworks(null));
        server.shutdown();
    }

    /**
     * Warning: Only run this if you have another way to communicate with the server. This will disconnect the current
     * Wifi connection.
     * @throws InterruptedException
     * @throws DBusException
     * @throws IOException
     */
    @Test
    public void join() throws InterruptedException, DBusException, IOException {
        WifiConfigServer server = new WifiConfigServer();
        server.start();
        WifiConfig.DisconnectRequest disconnectRequest = WifiConfig.DisconnectRequest.newBuilder().build();

        WifiConfig.JoinResult result = joinTestNetwork(server, disconnectRequest);

        server.shutdown();
        assertTrue("Join Result: " + result.getErrorMessage(), result.getIsSuccessful());
    }

    /**
     * Warning: Only run this if you have another way to communicate with the server. This will disconnect the current
     * Wifi connection.
     * @throws InterruptedException
     * @throws DBusException
     * @throws IOException
     */
    @Test
    public void disconnect() throws InterruptedException, DBusException, IOException {
        WifiConfigServer server = new WifiConfigServer();
        server.start();
        WifiConfig.DisconnectRequest disconnectRequest = WifiConfig.DisconnectRequest.newBuilder().build();
        WifiConfig.JoinResult result = joinTestNetwork(server, disconnectRequest);

        WifiConfig.DisconnectResult disconnectResult = null;
        if (result.getIsSuccessful()) {
            disconnectResult = server.disconnect(disconnectRequest);
        }

        server.shutdown();
        assertTrue("Join Result", result.getIsSuccessful());
        if (disconnectRequest != null) assertTrue(disconnectResult.getErrorMessage(), disconnectResult.getIsSuccessful());
    }

    public void quickDisconnect() throws InterruptedException, DBusException {
        WifiConfigServer server = new WifiConfigServer();
        server.start();
        WifiConfig.DisconnectRequest disconnectRequest = WifiConfig.DisconnectRequest.newBuilder().build();
        WifiConfig.DisconnectResult result = server.disconnect(disconnectRequest);
        assertTrue(result.getIsSuccessful());
    }

    private WifiConfig.JoinResult joinTestNetwork(WifiConfigServer server, WifiConfig.DisconnectRequest disconnectRequest) throws IOException {
        WifiConfig.Scan wirelessNetworks = server.getWirelessNetworks(null);
        WifiConfig.SsidNetwork activeNetwork = null;
        if (wirelessNetworks.hasActiveNetwork()) {
            activeNetwork = wirelessNetworks.getActiveNetwork();
            LOG.info("disconnecting from " + wirelessNetworks.getActiveAccessPoint() + "\non: " + activeNetwork);
            server.disconnect(disconnectRequest);
        }
        Properties prop = new Properties();
        prop.load(getClass().getResourceAsStream("/local.properties"));
        WifiConfig.JoinRequest.Builder join = WifiConfig.JoinRequest.newBuilder();
        join.setSsid(prop.getProperty("ssid"));
        join.setPsk(prop.getProperty("psk"));
        return server.join(join.build());
    }
}