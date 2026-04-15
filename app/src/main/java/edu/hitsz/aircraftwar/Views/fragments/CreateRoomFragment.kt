package edu.hitsz.aircraftwar.Views.fragments

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.CONNECTIVITY_SERVICE
import androidx.fragment.app.Fragment
import com.example.feature_online.OnlineGameClient
import com.example.feature_online.OnlineGameServer
import edu.hitsz.aircraftwar.R
import edu.hitsz.aircraftwar.Views.OnlineActivity

/**
 * 创建房间 Fragment
 * 显示当前房间的 IP 地址和端口号，启动服务端并自身作为客户端连接
 */
class CreateRoomFragment : Fragment() {

  companion object {
    const val TAG = "CreateRoomFragment"
  }

  private lateinit var textRoomIp: TextView
  private lateinit var textRoomPort: TextView
  private lateinit var textStatus: TextView

  private val port: Int = 50001

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_create_room, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    textRoomIp = view.findViewById(R.id.textRoomIp)
    textRoomPort = view.findViewById(R.id.textRoomPort)

    // 显示当前设备 IP 和端口
    val serverAddress = getServerAddress()
    textRoomIp.text = serverAddress.ifEmpty { "无法获取IP" }
    textRoomPort.text = port.toString()
    startRoom(serverAddress)
  }

  private fun startRoom(serverAddress: String) {
    if (serverAddress.isEmpty()) {
      Toast.makeText(requireContext(), "无法获取本机IP，请检查网络连接", Toast.LENGTH_SHORT).show()
      return
    }

    // 启动服务端（阻塞直到2人连接）
    Thread({
      OnlineGameServer(port).run()
    }, "GameServer").start()
    Log.d(TAG, "服务端已启动, serverAddress: $serverAddress, port: $port")

    // 启动客户端连接自己
    val client = OnlineGameClient(serverAddress, port)
    client.onConnected = {
      // 连接成功且收到游戏开始信号，切换到游戏界面
      Log.d(TAG, "收到游戏开始信号，准备启动游戏")
      activity?.runOnUiThread {
        (activity as? OnlineActivity)?.startGame(client)
      }
      // 启动接收线程，持续监听服务端数据
      client.startReceiving()
    }
    Thread({
      client.connect()
    }, "GameClient-Creator").start()
  }

  /**
   * 获取当前设备的局域网 IP 地址
   */
  private fun getServerAddress(): String {
    val connectivityManager =
      requireContext().getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork
    val linkProperties: LinkProperties? = connectivityManager.getLinkProperties(activeNetwork)
    linkProperties?.linkAddresses?.forEach { linkAddress ->
      val address = linkAddress.address
      // 过滤 IPv4 地址且排除回环地址
      if (address is java.net.Inet4Address && !address.isLoopbackAddress) {
        return address.hostAddress ?: ""
      }
    }
    return ""
  }
}
