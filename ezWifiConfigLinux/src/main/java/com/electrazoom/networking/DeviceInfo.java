/*
 * ezWifiConfig - Wifi Configuration over BLE
 * Copyright (c) 2018. Geoffrey Matrangola, electrazoom.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>
 *
 *     This program is also available under a commercial license. If you wish
 *     to redistribute this library and derivative work for commercial purposes
 *     please see ProtoBLE.com to obtain a proprietary license that will fit
 *     your needs.
 */

package com.electrazoom.networking;

import org.freedesktop.NetworkManager;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.Variant;

import java.util.Map;

public class DeviceInfo extends DBInterfaceObjectInfo<NetworkManager.Device> {
    public DeviceInfo(Map<String, Variant> properties, Path path, NetworkManager.Device obj) {
        super(properties, path, obj, NetworkManager.Device.class.getCanonicalName());
    }
}
