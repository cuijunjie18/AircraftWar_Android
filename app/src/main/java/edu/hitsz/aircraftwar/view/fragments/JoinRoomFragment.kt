package edu.hitsz.aircraftwar.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.feature_online.OnlineGameClient
import com.google.android.material.textfield.TextInputEditText
import edu.hitsz.aircraftwar.R
import edu.hitsz.aircraftwar.view.activity.OnlineGameActivity

/**
 * 加入房间 Fragment
 * 提供 IP 和端口输入框，用于连接到已创建的房间
 */
class JoinRoomFragment : Fragment() {

  companion object {
    const val TAG = "JoinRoomFragment"
  }

  private lateinit var editRoomIp: TextInputEditText
  private lateinit var editRoomPort: TextInputEditText
  private lateinit var buttonJoinRoom: Button

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_join_room, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    editRoomIp = view.findViewById(R.id.editRoomIp)
    editRoomPort = view.findViewById(R.id.editRoomPort)
    buttonJoinRoom = view.findViewById(R.id.buttonJoinRoom)

    buttonJoinRoom.setOnClickListener {
      joinRoom()
    }
  }

  private fun joinRoom() {
    val ip = editRoomIp.text?.toString()?.trim() ?: ""
    val portStr = editRoomPort.text?.toString()?.trim() ?: ""

    if (ip.isEmpty()) {
      Toast.makeText(requireContext(), "请输入房间 IP 地址", Toast.LENGTH_SHORT).show()
      return
    }
    if (portStr.isEmpty()) {
      Toast.makeText(requireContext(), "请输入端口号", Toast.LENGTH_SHORT).show()
      return
    }

    val port = portStr.toIntOrNull()
    if (port == null || port !in 1..65535) {
      Toast.makeText(requireContext(), "端口号无效，请输入 1-65535 之间的数字", Toast.LENGTH_SHORT).show()
      return
    }

    Log.d(TAG, "加入房间: $ip:$port")
    buttonJoinRoom.isEnabled = false
    buttonJoinRoom.text = "连接中..."

    val client = OnlineGameClient(ip, port)
    client.onConnected = {
      // 连接成功且收到游戏开始信号，切换到游戏界面
      Log.d(TAG, "收到游戏开始信号，准备启动游戏")
      activity?.runOnUiThread {
        (activity as? OnlineGameActivity)?.startGame(client)
      }
      // 启动接收线程，持续监听服务端数据
      client.startReceiving()
    }
    Thread({
      client.connect()
      if (!client.isConnected) {
        activity?.runOnUiThread {
          Toast.makeText(requireContext(), "连接失败，请检查 IP 和端口", Toast.LENGTH_SHORT).show()
          buttonJoinRoom.isEnabled = true
          buttonJoinRoom.text = "加入房间"
        }
      }
    }, "GameClient-Joiner").start()

    Toast.makeText(requireContext(), "正在连接 $ip:$port ...", Toast.LENGTH_SHORT).show()
  }
}
