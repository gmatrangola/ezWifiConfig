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

class WifiTableViewController: UITableViewController, WifiSetupDelegate, ScanningDelegate {
    var scanningView: ScanningView?
    var centralManager: ProtoBLECentralManager!
    var wifiSetupService: WifiSetupBleService!
    var wifiNetworks: [SsidNetwork] = []
    var wifiSetupDelegateDispatcher: WifiSetupDelegateDispatcher
    var activeNetwork: SsidNetwork?
    
    required init?(coder aDecoder: NSCoder) {
        wifiSetupDelegateDispatcher = WifiSetupDelegateDispatcher()
        super.init(coder: aDecoder)
        centralManager = ProtoBLECentralManager.shared
        wifiSetupService = WifiSetupBleService(delegate: wifiSetupDelegateDispatcher)
    }
    
    var device : DeviceInformation? {
        didSet {
            centralManager.connectedDevice = device
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        scanningView = ScanningView()
        centralManager.connect(wifiSetupService.bleService)
        scanningView?.delegate = self
        scanningView?.frame = refreshControl!.bounds
        refreshControl?.backgroundColor = UIColor.clear
        refreshControl?.tintColor = UIColor.clear
        refreshControl?.addSubview(scanningView!)
        self.refreshControl?.addTarget(self, action: #selector(WifiTableViewController.handleRefresh(_:)), for: UIControl.Event.valueChanged)
        self.refreshControl?.beginRefreshing()
        // Do any additional setup after loading the view.
        tableView.setNeedsLayout()
        tableView.layoutIfNeeded()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        wifiSetupDelegateDispatcher.active = self
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        print("Disconnect")
        // TODO Disconnect
        
        super.viewWillDisappear(animated)
    }
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return wifiNetworks.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellIdentifier = "WifiInfo"
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? WifiTableViewCell else {
            fatalError("Wrong cell type in table")
        }
        
        let network = wifiNetworks[indexPath.row]
        cell.title = network.ssid
        cell.percentSignal = network.strength
        cell.isEncryped = network.isPrivate
        cell.isCurrent = activeNetwork?.ssid == network.ssid
        return cell
    }
    
    @objc func handleRefresh(_ refreshControl: UIRefreshControl) {
        if !centralManager.centralManager.isScanning {
            tableView.reloadData()
            scanForWifi()
        }
    }
    
    func scanForWifi() {
        scanningView?.cancelButton.isEnabled = true
        scanningView?.retryButton.isEnabled = false
        let scanRequest = ScanRequest()
        do {
            try wifiSetupService.getWirelessNetworks(scanRequest: scanRequest)
            scanningView?.cancelButton.isEnabled = false
            scanningView?.retryButton.isEnabled = true
        }
        catch {
            scanningView?.statusLabel.text = ("Error getting wireless networks \(error)")
            scanningView?.cancelButton.isEnabled = false
            scanningView?.retryButton.isEnabled = true
        }
    }

    func getWirelessNetworksDidComplete(_ scan: Scan) {
        print("Scan Done! \(scan)")
        wifiNetworks.removeAll()
        wifiNetworks.insert(contentsOf: scan.networks, at: 0)
        DispatchQueue.main.async{
            self.tableView.reloadData()
        }
        self.refreshControl?.endRefreshing()
        activeNetwork = scan.activeNetwork
    }
    
    func getWirelessNetworksMessageProgress(current: Int, total: Int) {
        scanningView?.statusLabel.text = "Scanning Wifi Networks"
        scanningView?.progressBar.setProgress(Float(total)/Float(current), animated: true)
    }
    
    func joinDidComplete(_ joinResult: JoinResult) {
        // TODO
    }
    
    func joinMessageProgress(current: Int, total: Int) {
        // TODO
    }
    
    func disconnectDidComplete(_ disconnectResult: DisconnectResult) {
        // TODO
    }
    
    func disconnectMessageProgress(current: Int, total: Int) {
        // TODO
    }
    
    func authenticateDidComplete(_ authenticateResult: AuthenticateResult) {
        // TODO
    }
    
    func authenticateMessageProgress(current: Int, total: Int) {
        // TODO
    }
    
    func changeOperationModeDidComplete(_ operationModeResult: OperationModeResult) {
        // TODO
    }
    
    func changeOperationModeMessageProgress(current: Int, total: Int) {
        // TODO
    }
    
    func bleDidDiscoverCharacteristics() {
        print("BLE Service Connected!")
        scanForWifi()
    }
    
    func bleDidError(_ error: Error) {
        print("Error connecting \(error)")
    }
    
    func cancelWasPressed(_ sender: Any) {
        refreshControl?.endRefreshing()
    }
    
    func retryWasPressed(_ sender: Any) {
        scanForWifi()
    }

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        print ("prepare for segue \(segue.destination)")
        if let selectedRow = tableView.indexPathForSelectedRow {
            if let nextScene = segue.destination as? WifiJoinViewController {
                nextScene.wifiSetupService = wifiSetupService
                nextScene.network = wifiNetworks[selectedRow.row]
                nextScene.wifiSetupDelegateDispatcher = wifiSetupDelegateDispatcher
                nextScene.isActive = wifiNetworks[selectedRow.row].ssid == activeNetwork?.ssid
            }
        }

    }
}
