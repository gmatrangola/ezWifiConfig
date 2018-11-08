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

class WifiTableViewCell: UITableViewCell {
    @IBOutlet weak var signalStrength: UIImageView!
    @IBOutlet weak var ssid: UILabel!
    @IBOutlet weak var encryption: UIImageView!
    
    var percentSignal : Int32? {
        didSet {
            if (percentSignal != nil) {
                showSignalStrength(percentSignal!)
            }
        }
    }
    
    var isEncryped = false {
        didSet {
            showEncryped(isEncryped)
        }
    }
    
    var isCurrent = false {
        didSet {
            if (isCurrent) {
                ssid.font = UIFont.boldSystemFont(ofSize: 18)
                title = "*\(title)"
            }
        }
    }
    
    var title = "<HIDDEN>" {
        didSet {
            if (isCurrent) {
                ssid.text = "*\(title)"
            }
            else {
                ssid.text = title
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
    
    private func showSignalStrength(_ percent : Int32) {
        self.signalStrength.image = UIImage(percent)
    }
    
    private func showEncryped(_ enc : Bool) {
        if (enc) {
            encryption.image = UIImage(named: "Locked.png")
        }
        else {
            encryption.image = UIImage(named: "Unlocked.png")
        }
    }
}
