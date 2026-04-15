package edu.hitsz.aircraftwar.Views

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.feature_online.OnlineGameClient
import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.R
import edu.hitsz.aircraftwar.Views.fragments.CreateRoomFragment
import edu.hitsz.aircraftwar.Views.fragments.JoinRoomFragment
import edu.hitsz.aircraftwar.logic.utils.ImageManager

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
    createRoomButton.setOnClickListener { showCreateRoomFragment() }
    joinRoomButton.setOnClickListener { showJoinRoomFragment() }
  }

  /**
   * 显示创建房间 Fragment
   */
  private fun showCreateRoomFragment() {
    supportFragmentManager.beginTransaction()
      .replace(R.id.fragmentContainer, CreateRoomFragment())
      .commit()
  }

  /**
   * 显示加入房间 Fragment
   */
  private fun showJoinRoomFragment() {
    supportFragmentManager.beginTransaction()
      .replace(R.id.fragmentContainer, JoinRoomFragment())
      .commit()
  }

  /**
   * 启动联机游戏
   * @param client 已连接的 OnlineGameClient 实例
   */
  fun startGame(client: OnlineGameClient) {
    // 初始化图片管理器
    ImageManager.init(AircraftWarApplication.context)

    // 初始化自定义游戏 View
    setContentView(R.layout.activity_main)
    val gameView = GameOnlineView(this, client)
    val container = findViewById<FrameLayout>(R.id.game_container)
    container.addView(gameView)
  }
}