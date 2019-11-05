#!/bin/bash
# Create the user, group that can't log in but has access to Bluetooth and network config permissions
sudo useradd -r -s /bin/nologin ezwificonfig
sudo usermod -aG bluetooth ezwificonfig
sudo usermod -aG netdev ezwificonfig
