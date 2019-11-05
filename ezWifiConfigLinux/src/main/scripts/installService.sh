#!/bin/bash
# Create the user, group that can't log in but has access to Bluetooth
echo Installing ezWifiConfig Service
sudo cp /opt/ezWifiConfig/scripts/ezWifiConfig.service /etc/systemd/system/
sudo mkdir -p /var/lib/polkit-1/localauthority/30-com.electrazoom.d
sudo cp /opt/ezWifiConfig/scripts/com.electrazoom.ezwificonfig.pkla /var/lib/polkit-1/localauthority/30-com.electrazoom.d
sudo systemctl daemon-reload
sudo systemctl restart polkit
sudo systemctl enable ezWifiConfig.service
sudo systemctl start ezWifiConfig.service
