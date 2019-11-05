# ezWifiConfig BETA

Provision Wifi for headless IoT devices using a simple app Android or iOS using BLE instead of a soft AP.

ezWifiConfig uses ProtoBLE to create a connection between a Linux based IoT device without a keyboard monitor and mouse. Then the Android/iOS user can see the list of networks with signal strengths available. The user selects a Wifi network and enters the password. ezWifiConfig uses Network Manager on the Linux side to join the network.

## Projoect

This project is organized into for main directories. 

- ezWifiConfigAndroid - Android project that can be built using gradle on Android Studio
- ezWifiConfigAPI - Subproject that can be built using gardle, Android Stuio, or IntelliJ
- ezWifiConfigiOS - Subproject that can be built using XCode
- ezWifiConfigLinux - Subproject that can be built using gradle, Android Studio, or IntellJ

Note that ezWfifiConfigAPI and ezWifiConfigLinux are subprojects of this directory and ezWifiConfigAndroid and ezWifiConfigiOS are independant projects.

## Building

Install ProtoBLE libraries and Code Generator - See https://github.com/gmatrangola/ProtoBLE

### Linux

System Dependencies:
```bash
sudo apt-get update
sudo apt-get install -y openjdk-11-jdk libdbus-java libsocket-java protobuf-compiler network-manager
```
Install switch over to **NetworkManager** for wifi management
```bash
sudo systemctl disable dhcpcd
sudo systemctl stop dhcpcd
sudo apt purge -y openresolv
```

On a Raspberry Pi running Raspbian Buster **or similar** embedded Linux development environment run...


```bash
./gradlew :ezWifiConfigAPI:install :ezWifiConfigLinux:ezWifiConfigDeb
sudo dpkg --install ezWifiConfigLinux/build/distributions/ezwificonfig_1.0~SNAPSHOT-1_all.deb
```

Start the service with `sudo systemctl start ezWifiConfig` or reboot
Troubleshoot with `sudo journalctl --lines=200 -f --unit=ezWifiConfig`

Installing the deb package will do the following...
1. Create user `ezwificonfig` and group `ezwificonfig`
1. add the ezwificonfig user to the netdev and bluetooth groups
1. Install the binaries in `/opt/ezWifiConfig` owned by `ezwificonfig`
1. Install a systemd service called `ezWifiConfig`


Follow the instructions for setting up BLE on Network Manager: https://github.com/gmatrangola/ble-java

For development, You can setup the Linux permissions 

```
sudo usermod -G netdev -a $USER
sudo usermod -G bluetooth -a $USER
```

If necessary, sdd following to /etc/dbus-1/system.d/bluetooth.conf

```xml
  <!-- allow users of bluetooth group to communicate -->
  <policy group="bluetooth">
    <allow send_destination="org.bluez"/>
  </policy>
```

Set up the ezWifiConfigLinux script to run as a Linux service however it makes sense for your distro. Or use the jar file as a library in your own Linux IoT application.

### iOS

Make sure that the [ProtoBLE](https://github.com/gmatrangola/ProtoBLE/tree/master/ProtoBLEiOS) Project is at the same toplevel as the parent ezWifiConfig project in your directory structure. So that `../../ProtoBle/ProtoBLEiOS` is the path found on the Podfile's `pod` reference.

Run `pod install` in the `ezWifiConfig/ezWifiConfigiOS` directory.

Load the ProtoBLEiOS libarary into XCode, set up your Developer Certificate and Launch on a real iOS device. Note that BLE does not work in the emulator.

Use the example App or include the library into your own iOS setup App.

### Android

Load ezWifiConfigAndroid into Android Studio build with ./gradlew. Install the example program on an actual device. Note that BLE does not work on the emulator.

Use the example App or include the library in your own Android setup App.
