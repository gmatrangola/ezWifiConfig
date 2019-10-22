#!/bin/bash
# Create the user, group that can't log in but has access to Bluetooth
echo Installing ezWifiConfig Service
sudo cp /opt/ezWifiConfig/scripts/ezWifiConfig.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable ezWifiConfig.service