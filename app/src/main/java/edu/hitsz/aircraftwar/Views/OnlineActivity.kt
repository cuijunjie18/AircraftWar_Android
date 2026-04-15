package edu.hitsz.aircraftwar.Views

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.feature_online.*
import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.R

class OnlineActivity : AppCompatActivity() {
  companion object {
    const val TAG = "OnlineActivity"
  }
  private lateinit var createRoomButton: Button
  private lateinit var joinRoomButton: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_online)
    initView()
  }

  private fun initView() {
    createRoomButton = findViewById(R.id.buttonForCreateRoom)
    joinRoomButton = findViewById(R.id.buttonForJoinRoom)
    createRoomButton.setOnClickListener { createRoom() }
    joinRoomButton.setOnClickListener { joinRoom() }
  }

  private fun createRoom() {
    val port: Int = 50001
    Thread{ OnlineGameServer(port) }.start()

    val serverAddress = getServerAddress()
    Log.d(TAG, "serverAddress: $serverAddress")
    Thread{ OnlineGameClient(serverAddress,port) }.start()

    Toast.makeText(this, "创建房间成功", Toast.LENGTH_SHORT).show()

  }

  private fun joinRoom() {

  }

  private fun getServerAddress(): String {
    val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
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