package com.example.feature_online

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import kotlin.jvm.java
import com.example.feature_online.OnlineClientData
import com.example.feature_online.OnlineServerData
import java.lang.Thread.sleep
import kotlin.concurrent.thread

enum class GameState {
  WAITING,
  PLAYING,
  OVER
}

/**
 *
 * OnlineGameServer
 * 注：为了提高性能，分数读取均不加锁
 */
class OnlineGameServer(val port: Int = 50001) {
  private var onlinePlayerCount: Int = 0
  private var gameState: GameState = GameState.WAITING
  @Volatile private var score: Int = 0
  private val scoreLock = Any() // 用于同步 score 的锁对象
  private val onlinePlayerCountLock = Any() // 用于同步 onlinePlayerCount 的锁对象

  init {
    try {
      val serverSocket = ServerSocket(port)
      while (true) {
        if (onlinePlayerCount < 2) {
          println("waiting client connect")
          val socket = serverSocket.accept()
          println("accept client connect $socket")
          Thread(OnlineService(socket)).start()
          onlinePlayerCount++
        } else {
          gameState = GameState.PLAYING
          println("online player count is 2. Ready to start game")
          break
        }
      }
      while (gameState == GameState.PLAYING) { }
    } catch (ex: Exception) {
      ex.printStackTrace()
    }
  }

  inner class OnlineService(private val socket: Socket) : Runnable {
    private var infoReceiver: BufferedReader? = null
    private var infoBroadcaster: PrintWriter? = null

    init {
      try {
        infoReceiver = BufferedReader(InputStreamReader(socket.getInputStream()))
        infoBroadcaster = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream(), "utf-8")), true)
      } catch (ex: IOException) {
        ex.printStackTrace()
      }
    }

    override fun run() {
      try {
        while (gameState == GameState.WAITING) { }
        while (gameState == GameState.PLAYING) {
          val content = infoReceiver!!.readLine()
          var clientData: OnlineClientData = Gson().fromJson(content, OnlineClientData::class.java)
          updateScore(clientData.getScore)
          sendServerData()
          if (!clientData.isGameOver) continue
          gameOver()
          break;
        }
        while (gameState == GameState.PLAYING && onlinePlayerCount > 0) {
          var score_now = score
          while (score_now == score) { sleep(100) } // slepp 100ms提高性能，避免频繁读取score
          sendServerData()
        }
        sendServerData()
      } catch (ex: Exception) {
        ex.printStackTrace()
      }
    }
    private fun sendServerData() {
      var serverData: OnlineServerData = OnlineServerData(score, gameState == GameState.OVER) // 读不加锁
      infoBroadcaster!!.println(Gson().toJson(serverData))
    }
  }

  private fun gameOver() {
    synchronized(onlinePlayerCountLock) {
      onlinePlayerCount--
    }
    if (onlinePlayerCount == 0) {
      gameState = GameState.OVER
    }
  }

  private fun updateScore(getScore: Int) {
    synchronized(scoreLock) {
      score += getScore
    }
  }
}