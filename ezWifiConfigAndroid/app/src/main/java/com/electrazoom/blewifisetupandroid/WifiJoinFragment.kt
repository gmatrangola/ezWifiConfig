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

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_join.*


private const val ARG_SSID = "ssid"
private const val ARG_PRIVATE = "private"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [WifiJoinFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [WifiJoinFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class WifiJoinFragment : Fragment() {
    private var ssid: String? = null
    private var private: Boolean = false
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            ssid = it.getString(ARG_SSID)
            private = it.getBoolean(ARG_PRIVATE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_join, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement WifiListFragmentListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        joinButton.setOnClickListener {
            busy()
            val psk : String?
            if (password.visibility == View.VISIBLE) psk = password.text.toString()
            else psk = null
            ssid?.let { ssid -> listener?.onJoin(ssid, psk) }
        }
        title.text = activity?.getString(R.string.join_network, ssid)
        if (private) password.visibility = View.VISIBLE
        else password.visibility = View.GONE
    }

    private fun busy() {
        password.isEnabled = false
        cancelButton.isEnabled = false
        joinButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
        title.text = activity?.getString(R.string.joining_network, ssid)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun onJoinError(errorMessage: String?) {
        password.isEnabled = true
        cancelButton.isEnabled = true
        joinButton.isEnabled = true
        progressBar.visibility = View.GONE
        title.text = activity?.getString(R.string.join_error, ssid, errorMessage)
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
    interface OnFragmentInteractionListener {
        fun onJoin(ssid: String, psk: String?)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param ssid Parameter 1.
         * @return A new instance of fragment WifiJoinFragment.
         */
        @JvmStatic
        fun newInstance(ssid: String, private: Boolean) =
                WifiJoinFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_SSID, ssid)
                        putBoolean(ARG_PRIVATE, private)
                    }
                }
    }
}
