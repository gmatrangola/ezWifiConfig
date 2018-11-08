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

import android.app.Activity
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.electrazoom.rpc.WifiConfig
import com.electrazoom.rpc.WifiSetupBleCentralService
import kotlinx.android.synthetic.main.activity_select_peripheral.*
import java.util.*

private const val TAG = "WifiActivity"
private const val WIFI_LIST_FRAGMENT = "WIFI_LIST_FRAGMENT"
private const val WIFI_JOIN_FRAGMENT = "WIFI_JOIN_FRAGMENT"

class WifiActivity : AppCompatActivity(), WifiListFragment.WifiListFragmentListener, WifiJoinFragment.OnFragmentInteractionListener {
    private var networks: List<WifiConfig.SsidNetwork>? = null

    inner class SetupResponseListener : WifiSetupBleCentralService.WifiSetupListener {
        override fun onConnectionStateChange(state: Int) {
            Log.d(TAG, "SetupResponseListener.onConnectionStateChange " + state)
            // if id doesn't work try tags
            val fragment = supportFragmentManager.findFragmentByTag(WIFI_LIST_FRAGMENT)
            if (fragment is WifiListFragment) {
                fragment.onGattConnectionStateChange(state)
            }
        }

        override fun onGetWirelessNetworks(output: WifiConfig.Scan?) {
            Log.d(TAG, "onGetAccessPoints $output")
            if (output != null) {
                for (ssidNetwork in output.networksList) {
                    Log.d(TAG, "ssidNetwork $ssidNetwork")
                }
                networks = output.networksList.sortedByDescending { s -> s.strength }
                val fragment = supportFragmentManager.findFragmentByTag(WIFI_LIST_FRAGMENT)
                if (fragment is WifiListFragment) {
                    fragment.onNetworksDetected(networks!!)
                }
            }
        }

        override fun onJoin(result: WifiConfig.JoinResult?) {
            Log.d(TAG, "onJoin " + result)
            if (result != null){
                Log.d(TAG, "onJoin ${result.isSuccessful} ${result.errorMessage}")
                if (result.isSuccessful) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                else {
                    runOnUiThread {
                        val joinFragment = supportFragmentManager.findFragmentByTag(WIFI_JOIN_FRAGMENT)
                        if (joinFragment is WifiJoinFragment) {
                            joinFragment.onJoinError(result.errorMessage)
                        }
                    }
                }
            }
            else {
                runOnUiThread {
                    val joinFragment = supportFragmentManager.findFragmentByTag(WIFI_JOIN_FRAGMENT)
                    if (joinFragment is WifiJoinFragment) {
                        joinFragment.onJoinError("Unknown Error")
                    }
                }
            }
        }

        override fun onDisconnect(result: WifiConfig.DisconnectResult?) {
            if (result != null) {
                Log.d(TAG, "onDisconnect ${result.isSuccessful}")
            }
            else Log.e(TAG, "onDisconnect Failed")
        }

        override fun onServiceConnected() {
            Log.d(TAG, "SetupResponseListener.onServiceConnected")
            findWirelessNetworks()
        }

        override fun onProgress(uuid : UUID, current : Int, total : Int) {
            val fragment = supportFragmentManager.findFragmentByTag(WIFI_LIST_FRAGMENT)
            if (fragment is WifiListFragment && uuid.equals(WifiSetupBleCentralService.GETWIRELESSNETWORKS_RETURN)) {
                fragment.onProgress(current, total)
            }
        }

        override fun onChangeOperationMode(output: WifiConfig.OperationModeResult?) {
            // not implemented
        }

        override fun onAuthenticate(output: WifiConfig.AuthenticateResult?) {
            // not implemented
        }


        override fun onError(error: String?) {
            Log.e(TAG, "onError $error")
        }
    }

    override fun onFindWirelessNetworksRequest() {
        findWirelessNetworks()
    }

    private var clientService: WifiSetupBleCentralService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected $name")
            val binder = service as WifiSetupBleCentralService.LocalBinder
            if (null != binder.service) {
                Log.d(TAG, "Service bound")
                clientService = binder.service
                binder.service.setWifiSetupListener(SetupResponseListener())
                connectToBleGatt()
            }
            else Log.e(TAG, "No Service Bound")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected")
            clientService = null
            val fragment = supportFragmentManager.findFragmentByTag(WIFI_LIST_FRAGMENT)
            if (fragment is WifiListFragment) {
                fragment.onServiceDisconnected()
            }
        }
    }

    private fun connectToBleGatt() {
        val result = intent.getParcelableExtra<ScanResult>("scanResult")
        Log.d(TAG, "Selected Scan result = $result")
        if (null != result) {
            val device = result.device
            clientService?.connect(device)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_wifi)
        showWifiListFragment()
    }

    private fun showWifiListFragment() {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.root_layout, WifiListFragment.newInstance(), WIFI_LIST_FRAGMENT)
                .commit()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        val intent = Intent(this, WifiSetupBleCentralService::class.java)
        val status = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        if (!status) Log.e(TAG, "Unable to bind service")
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        if (null != clientService) {
            unbindService(serviceConnection)
            clientService?.disconnect()
        }
        super.onPause()
    }

    private fun findWirelessNetworks() {
        val setup = clientService
        if (setup != null) {
            setup.getWirelessNetworks(WifiConfig.ScanRequest.newBuilder().build())
            Log.d(TAG, "gettingAccessPoints")
        }
    }

    override fun onNetworkSelected(ssidNetwork: WifiConfig.SsidNetwork) {
        Log.d(TAG, "selected $ssidNetwork")
        val setup = clientService
        if (setup != null) showJoinFragment(ssidNetwork)
    }

    private fun showJoinFragment(ssidNetwork: WifiConfig.SsidNetwork) {
        val joinFragment = WifiJoinFragment.newInstance(ssidNetwork.ssid, ssidNetwork.isPrivate)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.root_layout, joinFragment, WIFI_JOIN_FRAGMENT)
                .addToBackStack(WIFI_JOIN_FRAGMENT)
                .commit()
    }

    override fun onJoin(ssid: String, psk: String?) {
        Log.d(TAG, "onJoin $ssid with password")
        val join = WifiConfig.JoinRequest.newBuilder()
        join.ssid = ssid
        if (psk != null) join.psk = psk
        clientService?.join(join.build())
    }

    override fun onDisconnectWirelessNetworks() {
        Log.d(TAG, "Disconnect Wireless Network")
        clientService?.disconnect(WifiConfig.DisconnectRequest.newBuilder().build())
    }
}
