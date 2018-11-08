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

import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.electrazoom.rpc.WifiConfig
import kotlinx.android.synthetic.main.fragment_wifi_list.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [WifiListFragment.WifiListFragmentListener] interface
 * to handle interaction events.
 * Use the [WifiListFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */

private const val TAG = "WListFrag"
class WifiListFragment : Fragment() {
    private var listener: WifiListFragmentListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wifi_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity != null ) {
            wifiRecyclerView.layoutManager = LinearLayoutManager(activity)
            refreshButton.setOnClickListener {
                disableButtons()
                listener?.onFindWirelessNetworksRequest()
            }
            disconnectButton.setOnClickListener {
                disableButtons()
                listener?.onDisconnectWirelessNetworks()
            }
        }

    }

    private fun disableButtons() {
        refreshButton.isEnabled = false
        disconnectButton.isEnabled = false
    }

    private fun enableButtons() {
        refreshButton.isEnabled = true
        disconnectButton.isEnabled = true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is WifiListFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement WifiListFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun onNetworksDetected(networks: List<WifiConfig.SsidNetwork>) {
        var isConnected = false
        for (network in networks) {
            if (network.active) isConnected = true
        }
        activity?.runOnUiThread {
            wifiRecyclerView.adapter = WifiAdapter(networks) {
                listener?.onNetworkSelected(it)
            }
            refreshButton.isEnabled = true
            disconnectButton.isEnabled = isConnected
        }
    }

    fun onGattConnectionStateChange(state: Int) {
        Log.d(TAG, "onConnectionStateChanged " + state)
        when(state) {
            BluetoothProfile.STATE_CONNECTED -> {
                Log.i(TAG, "GATT Connected")
                activity?.runOnUiThread { enableButtons() }
            }
            else -> activity?.runOnUiThread { disableButtons() }
        }
    }

    fun onServiceDisconnected() {
        activity?.runOnUiThread { disableButtons() }
    }

    fun onProgress(current: Int, total: Int) {
        activity?.runOnUiThread {
            if (total > 0){
                progressBar.visibility = View.VISIBLE
                progressBar.max = total
                progressBar.progress = current
            }
            else progressBar.visibility = View.GONE
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface WifiListFragmentListener {
        fun onNetworkSelected(ssidNetwork: WifiConfig.SsidNetwork)

        fun onFindWirelessNetworksRequest()
        fun onDisconnectWirelessNetworks()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment WifiListFragment.
         */
        @JvmStatic
        fun newInstance() = WifiListFragment()
    }
}
