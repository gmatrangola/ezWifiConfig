
# ezWifiConfigLinux

## Permissions

Add users to groups

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

