# ezWifiConfig BETA

Provision Wifi for headless IoT devices using a simple Android or iOS.

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

On a Raspberry Pi running Raspbian Jessi **or similar** embedded Linux development enviornment run...

`./gradlew :ezWifiConfigAPI:install :ezWifiConfigLinux:distTar`

un-tar the ezWifiConfigLinux/build/distributions/ezWifiConfigLinux-1.0.tar someplace (like /usr/local/opt)

Follow the instructions for setting up BLE on Network Manager: https://github.com/gmatrangola/ble-java

Setup the Linux permissions so you don't have to run as root. Add users to groups

```
sudo usermod -G bluetooth -a $USER
sudo usermod -G netdev -a $USER
sudo usermod -G bluetooth -a $USER
```

Add following to /etc/dbus-1/system.d/bluetooth.conf

```xml
  <!-- allow users of bluetooth group to communicate -->
  <policy group="bluetooth">
    <allow send_destination="org.bluez"/>
  </policy>
```

Set up the ezWifiConfigLinux script to run as a Linux service however it makes sense for your distro. Or use the jar file as a library in your own Linux IoT application.

### iOS

Load the ProtoBLEiOS libarary into XCode, set up your Developer Certificate and Launch on a real iOS device. Note that BLE does not work in the emulator.

Use the example App or include the library into your own iOS setup App.

### Android

Load ezWifiConfigAndroid into Android Studio build with ./gradlew. Install the example program on an actual device. Note that BLE does not work on the emulator.

Use the example App or include the library in your own Android setup App.
