package com.example.feature_online

import android.util.Log
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.net.UnknownHostException

/**
 * OnlineGameClient
 *
 * 客户端逻辑：
 * - 连接到服务端
 * - 提供 sendData() 方法供 GameView 发送本方分数
 * - 在后台线程持续接收服务端数据，通过回调通知 GameView 对手分数变化和对战结束
 */
class OnlineGameClient(val serverIP: String, val serverPort: Int) {
  companion object {
    const val TAG = "OnlineGameClient"
  }

  private var socket: Socket? = null
  private var infoReceiver: BufferedReader? = null
  private var infoBroadcaster: PrintWriter? = null
  private val gson = Gson()

  @Volatile
  var isConnected = false
    private set

  /**
   * 回调：收到服务端数据时触发
   * @param opponentScore 对手当前总分
   * @param isAllOver 对战是否全部结束
   */
  var onServerDataReceived: ((opponentScore: Int, isAllOver: Boolean) -> Unit)? = null

  /**
   * 回调：连接成功时触发
   */
  var onConnected: (() -> Unit)? = null

  /**
   * 连接到服务端（阻塞方法，需在子线程调用）
   */
  fun connect() {
    try {
      socket = Socket()
      socket!!.connect(InetSocketAddress(serverIP, serverPort), 5000)
      Log.d(TAG, "已连接到 $serverIP:$serverPort")
      infoReceiver = BufferedReader(InputStreamReader(socket!!.getInputStream(), "utf-8"))
      infoBroadcaster = PrintWriter(
        BufferedWriter(OutputStreamWriter(socket!!.getOutputStream(), "utf-8")), true
      )
      isConnected = true

      // 等待服务端发来的第一条数据（游戏开始信号）
      val firstLine = infoReceiver?.readLine()
      if (firstLine != null) {
        Log.d(TAG, "收到游戏开始信号: $firstLine")
        onConnected?.invoke()
      }

    } catch (ex: UnknownHostException) {
      Log.e(TAG, "未知主机: ${ex.message}")
      ex.printStackTrace()
    } catch (ex: IOException) {
      Log.e(TAG, "连接失败: ${ex.message}")
      ex.printStackTrace()
    }
  }

  /**
   * 发送本方数据到服务端
   * @param myScore 本方当前总分
   * @param isGameOver 本方是否已死亡
   */
  fun sendData(myScore: Int, isGameOver: Boolean) {
    try {
      val data = OnlineClientData(myScore, isGameOver)
      infoBroadcaster?.println(gson.toJson(data))
    } catch (ex: Exception) {
      Log.e(TAG, "发送数据失败: ${ex.message}")
    }
  }

  /**
   * 启动接收线程，持续监听服务端数据
   * 需在连接成功后调用
   */
  fun startReceiving() {
    Thread({
      try {
        while (isConnected) {
          val content = infoReceiver?.readLine() ?: break
          val serverData = gson.fromJson(content, OnlineServerData::class.java)
          onServerDataReceived?.invoke(serverData.opponentScore, serverData.isAllOver)
        }
      } catch (ex: Exception) {
        Log.e(TAG, "接收数据异常: ${ex.message}")
        ex.printStackTrace()
      } finally {
        disconnect()
      }
    }, "ClientReceiver").start()
  }

  /**
   * 断开连接
   */
  fun disconnect() {
    isConnected = false
    try {
      socket?.close()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}