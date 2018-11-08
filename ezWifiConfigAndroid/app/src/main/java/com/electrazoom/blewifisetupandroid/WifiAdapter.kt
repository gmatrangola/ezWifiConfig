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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.electrazoom.rpc.WifiConfig
import kotlinx.android.synthetic.main.wifi_layout.view.*


class WifiAdapter(val networks : List<WifiConfig.SsidNetwork>,
                  private val listener: (WifiConfig.SsidNetwork) -> Unit) :
    RecyclerView.Adapter<WifiAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var layout = LayoutInflater.from(parent.context).inflate(R.layout.wifi_layout, parent, false)
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(networks[position], listener)

    override fun getItemCount(): Int = networks.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(results: WifiConfig.SsidNetwork,
                 viewListener: (WifiConfig.SsidNetwork) -> Unit) = with(view) {
            ssidTextView.text = if(results.ssid == null)"Unpublished" else results.ssid
            if (results.strength > 80) signalIcon.setImageResource(R.drawable.ic_wifi_100)
            else if (results.strength > 30) signalIcon.setImageResource(R.drawable.ic_wifi_75)
            else if (results.strength > 10) signalIcon.setImageResource(R.drawable.ic_wifi_50)
            else signalIcon.setImageResource(R.drawable.ic_wifi_25)

            if (results.isPrivate) secureIcon.setImageResource(R.drawable.ic_lock_outline_black_24dp)
            else secureIcon.setImageResource(R.drawable.ic_lock_open_black_24dp)
            if (results.active) details.visibility = VISIBLE
            else details.visibility = GONE
            setOnClickListener { listener(results)}
        }
    }
}