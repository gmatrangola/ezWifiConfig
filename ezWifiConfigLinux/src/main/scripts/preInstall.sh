#!/bin/bash
# echo Creating ezwificonfig user
sudo useradd -r -s /bin/nologin ezwificonfig
sudo usermod -aG bluetooth ezwificonfig
