package com.example.feature_online

import android.util.Log
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

enum class GameState {
  WAITING,
  PLAYING,
  OVER
}

/**
 * OnlineGameServer
 *
 * 服务端逻辑：
 * - 等待 2 个玩家连接
 * - 为每个玩家维护独立分数
 * - 收到某玩家数据后，将对手的分数发送给该玩家
 * - 双方都死亡后，标记对战结束，通知所有客户端
 */
class OnlineGameServer(val port: Int = 50001) : Runnable {
  companion object {
    const val TAG = "OnlineGameServer"
  }

  @Volatile
  private var gameState: GameState = GameState.WAITING

  // 每个玩家的分数，索引 0 和 1 分别对应两个玩家
  private val playerScores = IntArray(2) { 0 }
  private val playerGameOver = BooleanArray(2) { false }

  private lateinit var connSockets: MutableList<Socket>
  private var infoReceivers: MutableList<BufferedReader?> = MutableList(2) { null }
  private var infoBroadcasters: MutableList<PrintWriter?> = MutableList(2) { null }
  private val lock = Any()

  override fun run() {
    prepare()
    if (connSockets.size == 2) {
      gameState = GameState.PLAYING
      Log.d(TAG, "两个玩家已连接，启动游戏服务")
      startGame()
    }
  }

  private fun prepare() {
    try {
      val serverSocket = ServerSocket(port)
      connSockets = mutableListOf()
      var count = 0
      while (count < 2) {
        Log.d(TAG, "等待客户端连接... 当前已连接: $count")
        val socket = serverSocket.accept()
        Log.d(TAG, "接受客户端连接: $socket")
        connSockets.add(socket)
        count++
      }
    } catch (ex: Exception) {
      ex.printStackTrace()
    }
  }

  private fun startGame() {
    for (i in 0 until connSockets.size) {
      Thread(OnlineService(connSockets[i], i), "OnlineService-$i").start()
    }
  }

  /**
   * 为每个客户端提供服务的内部类
   * @param playerIndex 该玩家的索引（0 或 1），用于区分两个玩家
   */
  inner class OnlineService(private val socket: Socket, private val playerIndex: Int) : Runnable {
    private val gson = Gson()

    init {
      try {
        infoReceivers[playerIndex] = BufferedReader(InputStreamReader(socket.getInputStream(), "utf-8"))
        infoBroadcasters[playerIndex] = PrintWriter(
          BufferedWriter(OutputStreamWriter(socket.getOutputStream(), "utf-8")), true
        )
        // 发送第一条数据，通知客户端游戏开始（对手分数为0，游戏未结束）
        sendServerData()
      } catch (ex: IOException) {
        ex.printStackTrace()
      }
    }

    override fun run() {
      try {
        while (gameState == GameState.PLAYING) {
          val content = infoReceivers[playerIndex]?.readLine() ?: break
          val clientData = gson.fromJson(content, OnlineClientData::class.java)

          synchronized(lock) {
            // 更新该玩家的分数
            playerScores[playerIndex] = clientData.myScore

            // 如果该玩家死亡
            if (clientData.isGameOver) {
              playerGameOver[playerIndex] = true
              Log.d(TAG, "玩家 $playerIndex 已死亡，分数: ${clientData.myScore}")

              // 检查是否双方都死亡
              if (playerGameOver[0] && playerGameOver[1]) {
                gameState = GameState.OVER
                Log.d(TAG, "双方都已死亡，对战结束")
              }
            }
          }

          // 回复该玩家：对手的分数 + 对战是否结束
          sendServerData()
        }
      } catch (ex: Exception) {
        Log.e(TAG, "OnlineService-$playerIndex 异常: ${ex.message}")
        ex.printStackTrace()
      } finally {
        try {
          socket.close()
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }

    private fun sendServerData() {
      val opponentIndex = 1 - playerIndex
      val opponentScore: Int
      val isAllOver: Boolean
      synchronized(lock) {
        opponentScore = playerScores[opponentIndex]
        isAllOver = gameState == GameState.OVER
      }
      when (isAllOver) {
        false -> {
          val serverData = OnlineServerData(opponentScore, false)
          infoBroadcasters[playerIndex]?.println(gson.toJson(serverData))
        }
        true -> {
          sendAllOverInfo()
        }
      }
    }
  }

  private fun sendAllOverInfo() {
    for (playerIndex in 0 until connSockets.size) {
      val opponentIndex = 1 - playerIndex
      val opponentScore: Int = playerScores[opponentIndex]
      val serverData = OnlineServerData(opponentScore, true)
      infoBroadcasters[playerIndex]?.println(Gson().toJson(serverData))
    }
  }
}