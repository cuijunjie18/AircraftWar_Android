package edu.hitsz.aircraftwar.Views.fragments

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
    Thread { OnlineGameClient(ip, port) }.start()

    Toast.makeText(requireContext(), "正在连接 $ip:$port ...", Toast.LENGTH_SHORT).show()
    buttonJoinRoom.isEnabled = false
    buttonJoinRoom.text = "连接中..."
  }
}
