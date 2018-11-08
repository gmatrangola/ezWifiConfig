/*
 * ezWifiConfig - Wifi Configuraiton over BLE
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

import UIKit

extension UIImage {
    convenience init?(_ percent: Int32) {
        var filename: String
        if (percent > 90) {
             filename = "Wifi-100.png"
        }
        else if (percent > 75) {
            filename = "Wifi-75.png"
        }
        else if (percent > 30) {
            filename = "Wifi-50.png"
        }
        else {
            filename = "Wifi-25.png"
        }
        self.init(named: filename)
    }
}
