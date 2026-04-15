package com.example.feature_online

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.net.UnknownHostException


class OnlineGameClient(val serverIP: String, val serverPort: Int): Runnable {
  private var socket: Socket? = null
  private var infoReceiver: BufferedReader? = null
  private var infoBroadcaster: PrintWriter? = null

  override fun run() {
    try {
      socket = Socket()
      socket!!.connect(InetSocketAddress(serverIP, serverPort), 5000)
      infoReceiver = BufferedReader(InputStreamReader(socket!!.getInputStream(), "utf-8"))
      infoBroadcaster = PrintWriter(BufferedWriter(OutputStreamWriter(socket!!.getOutputStream(), "utf-8")), true)

    } catch (ex: UnknownHostException) {
      ex.printStackTrace()
    } catch (ex: IOException) {
      ex.printStackTrace()
    }
  }
}