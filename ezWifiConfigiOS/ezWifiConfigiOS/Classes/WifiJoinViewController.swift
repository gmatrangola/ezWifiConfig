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
import CoreBluetooth
import ProtoBLEiOS

class WifiJoinViewController: UIViewController, WifiSetupDelegate {
    @IBOutlet weak var detailIcon: UIImageView!
    @IBOutlet weak var networkLabel: UILabel!
    @IBOutlet weak var pskText: UITextField!
    @IBOutlet weak var joinButton: UIButton!
    
    var isActive = false
    var wifiSetupService: WifiSetupBleService?
    var wifiSetupDelegateDispatcher: WifiSetupDelegateDispatcher?
    var network: SsidNetwork?
    
    override func viewWillAppear(_ animated: Bool) {
        wifiSetupDelegateDispatcher?.active = self
        if (isActive) {
            joinButton.setTitle( "Disconnect", for: UIControl.State.normal)
        }
        if (network?.strength != nil) {
            detailIcon.image = UIImage((network?.strength)!)
        }
        if (network?.ssid != nil) {
            networkLabel.text = network?.ssid
        }
    }

    @IBAction func joinWasClicked(_ sender: Any) {
        var request = JoinRequest()
        
        do {
            if (isActive) {
                try wifiSetupService?.disconnect(disconnectRequest:  DisconnectRequest())
            }
            else if (network != nil && network?.ssid != nil) {
                request.ssid = network!.ssid
                request.psk = pskText!.text!
                try wifiSetupService?.join( joinRequest: request)
            }
        }
        catch {
            print ("error joining \(error)")
        }
    }
    
    func getWirelessNetworksDidComplete(_ scan: Scan) {
        
    }
    
    func getWirelessNetworksMessageProgress(current: Int, total: Int) {
        
    }
    
    func joinDidComplete(_ joinResult: JoinResult) {
        navigationController?.popViewController(animated: true)
    }
    
    func joinMessageProgress(current: Int, total: Int) {
    }
    
    func disconnectDidComplete(_ disconnectResult: DisconnectResult) {
        navigationController?.popViewController(animated: true)
    }
    
    func disconnectMessageProgress(current: Int, total: Int) {
        
    }
    
    func authenticateDidComplete(_ authenticateResult: AuthenticateResult) {
        
    }
    
    func authenticateMessageProgress(current: Int, total: Int) {
        
    }
    
    func changeOperationModeDidComplete(_ operationModeResult: OperationModeResult) {
        
    }
    
    func changeOperationModeMessageProgress(current: Int, total: Int) {
        
    }
    
    func bleDidDiscoverCharacteristics() {
        // TODO
    }
    
    func bleDidError(_ error: Error) {
        // TODO
    }

}
