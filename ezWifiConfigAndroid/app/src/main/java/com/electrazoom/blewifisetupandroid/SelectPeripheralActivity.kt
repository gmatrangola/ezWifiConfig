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

package com.electrazoom.blewifisetupandroid

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelUuid
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.github.florent37.runtimepermission.kotlin.askPermission;
import kotlinx.android.synthetic.main.activity_select_peripheral.*

import com.electrazoom.rpc.WifiSetupConstants.SERVICE_UUID

class SelectPeripheralActivity : AppCompatActivity() {
    private val TAG = "SelectPeripheral"

    private val REQUEST_ENABLE_BT = 1
    private var scanner: BluetoothLeScanner? = null
    private val found: MutableMap<String, ScanResult> = mutableMapOf()
    private var count = 0

    private val wifiSetupServiceUuid = ParcelUuid.fromString(SERVICE_UUID)

    private val filter = listOf(ScanFilter.Builder().setServiceUuid(wifiSetupServiceUuid).build());
    private val scanSettings = ScanSettings.Builder().setReportDelay(250)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_peripheral)
        scanResultView.layoutManager = LinearLayoutManager(this)
        findAdapterWithPermission()
    }

    private fun findAdapterWithPermission() {
        askPermission(Manifest.permission.ACCESS_COARSE_LOCATION) {
            findAdapter()
        }.onDeclined { e ->
            Log.e(TAG, "Permission deined")
        }
    }

    private fun findAdapter(): Boolean {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        if (adapter?.isEnabled == false) {
            Log.d(TAG, "Adapter not enabled.")
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
            return false
        }
        startScan(adapter)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT) {
            findAdapter()
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            if (results != null) runOnUiThread {
                val size = results.size
                Log.d(TAG, "onBatchScanResults $size")
                if (size == 1) {
                    onPariferialSelected(results[0])
                }
                addScanResults(results)
            }
        }
    }

    private fun addScanResults(results: Collection<ScanResult>) {
        val updates = results.associateBy( {it.device.address}, {it})
        for (entry in updates.entries) {
            found.put(entry.key, entry.value)
        }
        statusText.text = "Scan #${count++} adapters: ${results?.size} total: ${found.size}"
        scanResultView.adapter = ScanResultAdapter(found.values.sortedBy { it.device.address }) {
            Log.d(TAG, "clicked on $it")
            onPariferialSelected(it)
        }
    }

    private fun onPariferialSelected(scanResult: ScanResult) {
        scanner?.stopScan(scanCallback);

        intent.putExtra("scanResult", scanResult)
        val uuids = scanResult.scanRecord.serviceUuids
        val intent : Intent?
        if (uuids.contains(wifiSetupServiceUuid)) {
            intent = Intent(this, WifiActivity::class.java)
        }
        else intent = null
        if (intent != null) {
            intent.putExtra("scanResult", scanResult)
            startActivity(intent)
        }
    }

    fun startScan(adapter: BluetoothAdapter) {
        scanner = adapter.bluetoothLeScanner
        if (null != scanner) {
            scanner?.startScan(filter, scanSettings, scanCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestory");
        if (null != scanner) {
            scanner?.stopScan(scanCallback);
        }
    }

}
