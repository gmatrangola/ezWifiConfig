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

class PeripheralTableViewCell: UITableViewCell {
    
    var rssi : Int? {
        didSet {
            if rssi != nil {
                showSignalStrength(rssi!)
            }
        }
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
    // From https://community.estimote.com/hc/en-us/articles/201636913-What-are-Broadcasting-Power-RSSI-and-other-characteristics-of-a-beacon-s-signal-
    // The signal strength depends on distance and Broadcasting Power value. At maximum Broadcasting Power (+4 dBm) the RSSI ranges from -26 (a few inches) to -100 (40-50 m distance).
    private func showSignalStrength(_ rssi : Int) {
        if (rssi > -30) {
            self.imageView!.image = UIImage(named: "Peripheral-100.png")
        }
        else if (rssi > -40) {
            self.imageView!.image = UIImage(named: "Peripheral-75.png")
        }
        else if (rssi > -70) {
            self.imageView!.image = UIImage(named: "Peripheral-50.png")
        }
        else {
            self.imageView!.image = UIImage(named: "Peripheral-25.png")
        }
    }
}
