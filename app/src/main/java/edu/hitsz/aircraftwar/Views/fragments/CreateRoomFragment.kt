package edu.hitsz.aircraftwar.Views.fragments

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.CONNECTIVITY_SERVICE
import androidx.fragment.app.Fragment
import com.example.feature_online.OnlineGameClient
import com.example.feature_online.OnlineGameServer
import edu.hitsz.aircraftwar.R

/**
 * 创建房间 Fragment
 * 显示当前房间的 IP 地址和端口号
 */
class CreateRoomFragment : Fragment() {

  companion object {
    const val TAG = "CreateRoomFragment"
  }

  private lateinit var textRoomIp: TextView
  private lateinit var textRoomPort: TextView

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

    // 启动服务端
    Thread { OnlineGameServer(port) }.start()
    Log.d(TAG, "serverAddress: $serverAddress, port: $port")

    // 启动客户端连接自己
    Thread { OnlineGameClient(serverAddress, port) }.start()
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
