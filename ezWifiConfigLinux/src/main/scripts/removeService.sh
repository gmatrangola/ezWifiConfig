#!/bin/bash
# Remove user and service
echo Disable service
sudo systemctl stop ezWifiConfig
sudo systemctl disable ezWifiConfig
sudo rm -f /etc/systemd/system/ezWifiConfig.service
sudo systemctl daemon-reload
# Remvoe ezwificonfig user
sudo userdel ezwificonfig