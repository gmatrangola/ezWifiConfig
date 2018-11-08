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

class CenteralTableViewController: UITableViewController, CBCentralManagerDelegate {
    
    var timer = Timer()
    var peripherals: [UUID: DeviceInformation] = [:]
    var deviceArray: [DeviceInformation] = []
    var centralManager: ProtoBLECentralManager!
    var selected: DeviceInformation?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        centralManager = ProtoBLECentralManager.shared
        centralManager.serviceUUIDs = [WifiSetupService_UUID]
        centralManager.listener = self
        self.refreshControl?.addTarget(self, action: #selector(CenteralTableViewController.handleRefresh(_:)), for: UIControl.Event.valueChanged)
        self.refreshControl?.beginRefreshing()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        print("Stop Scanning")
        cancelScan()
        selected = nil
        super.viewWillDisappear(animated)
    }
    
    @objc func handleRefresh(_ refreshControl: UIRefreshControl) {
        if !centralManager.centralManager.isScanning {
            startScan()
        }
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == CBManagerState.poweredOn {
            print("Bluetooth Enabled")
            startScan()
        }
        else {
            print("Bluetooth Disabled - Turn it on, please")
            let alertVC = UIAlertController(title: "Bluetooth is not enabled", message: "Make sure that your bluetooth is turned on", preferredStyle: UIAlertController.Style.alert)
            let action = UIAlertAction(title: "ok", style: UIAlertAction.Style.default, handler: { (action: UIAlertAction) -> Void in
                self.dismiss(animated: true, completion: nil)
            })
            alertVC.addAction(action)
            self.present(alertVC, animated: true, completion: nil)
        }
    }

    func startScan() {
        print("Scanning for BLE...")
        self.timer.invalidate()
        centralManager.scanForServices()
        Timer.scheduledTimer(timeInterval: 17, target: self, selector: #selector(self.cancelScan), userInfo: nil, repeats: false)
    }
    
    @objc func cancelScan() {
        centralManager.stopScan()
        self.refreshControl?.endRefreshing()
        print("Scan Stopped Found: \(peripherals.count)")
    }
    
    /*
     Called when the central manager discovers a peripheral while scanning. Also, once peripheral is connected, cancel scanning.
     */
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral,advertisementData: [String : Any], rssi RSSI: NSNumber) {
        
        self.peripherals[peripheral.identifier] = DeviceInformation(peripheral: peripheral, rssi: RSSI, advertisementData: advertisementData)
        deviceArray = peripherals.values.sorted(by: {$0.rssi.floatValue > $1.rssi.floatValue})
        self.tableView.reloadData()
    }
    
    // MARK: - Table view data source
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return peripherals.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellIdentifier = "PeripheralTableViewCell"
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? PeripheralTableViewCell else {
            fatalError("Wrong cell type in table")
        }
        
        // Configure the cell...
        let device = deviceArray[indexPath.row]
        cell.textLabel?.text = device.peripheral.name
        cell.rssi = device.rssi.intValue
        return cell
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        print ("prepare for segue \(segue.destination)")
        cancelScan()
        
        if segue.identifier == "wifiSetup" {
            let dest = segue.destination
            print ("dest = \(String(describing: dest.title))")
            if let selectedRow = tableView.indexPathForSelectedRow {
                if let nextScene = dest as? WifiTableViewController {
                    nextScene.device = deviceArray[selectedRow.row]
                }
            }
        }
    }

}

