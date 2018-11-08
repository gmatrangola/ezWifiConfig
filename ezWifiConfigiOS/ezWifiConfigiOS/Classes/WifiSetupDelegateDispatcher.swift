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

import Foundation
import ProtoBLEiOS

class WifiSetupDelegateDispatcher: WifiSetupDelegate {
    var active: WifiSetupDelegate?

    func getWirelessNetworksDidComplete(_ scan: Scan) {
        active?.getWirelessNetworksDidComplete(scan)
    }
    
    func getWirelessNetworksMessageProgress(current: Int, total: Int) {
        active?.getWirelessNetworksMessageProgress(current: current, total: total)
    }
    
    func joinDidComplete(_ joinResult: JoinResult) {
        active?.joinDidComplete(joinResult)
    }
    
    func joinMessageProgress(current: Int, total: Int) {
        active?.joinMessageProgress(current: current, total: total)
    }
    
    func disconnectDidComplete(_ disconnectResult: DisconnectResult) {
        active?.disconnectDidComplete(disconnectResult)
    }
    
    func disconnectMessageProgress(current: Int, total: Int) {
        active?.disconnectMessageProgress(current: current, total: total)
    }
    
    func authenticateDidComplete(_ authenticateResult: AuthenticateResult) {
        active?.authenticateDidComplete(authenticateResult)
    }
    
    func authenticateMessageProgress(current: Int, total: Int) {
        active?.authenticateMessageProgress(current: current, total: total)
    }
    
    func changeOperationModeDidComplete(_ operationModeResult: OperationModeResult) {
        active?.changeOperationModeDidComplete(operationModeResult)
    }
    
    func changeOperationModeMessageProgress(current: Int, total: Int) {
        active?.changeOperationModeMessageProgress(current: current, total: total)
    }
    
    func bleDidDiscoverCharacteristics() {
        active?.bleDidDiscoverCharacteristics()
    }
    
    func bleDidError(_ error: Error) {
        active?.bleDidError(error)
    }
    }
