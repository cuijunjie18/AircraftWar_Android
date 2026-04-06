package edu.hitsz.aircraftwar

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import edu.hitsz.aircraftwar.Views.GameView
import edu.hitsz.aircraftwar.logic.utils.ImageManager

/**
 * Android 程序入口 - 替代 Swing 的 Main 类
 * @author hitsz
 */
class MainActivity : AppCompatActivity() {

  companion object {
    // 保持与原项目一致的逻辑尺寸（用于游戏坐标计算）
    const val TAG = "MainActivity"
  }

  private lateinit var gameView: GameView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    setContentView(R.layout.activity_main)

    // 初始化图片管理器
    ImageManager.init(AircraftWarApplication.context)

    // 初始化自定义游戏 View
    gameView = GameView(AircraftWarApplication.context)
    val container = findViewById<FrameLayout>(R.id.game_container)
    container.addView(gameView)
  }

  override fun onResume() {
    super.onResume()
    // 恢复游戏循环（对应原 game.action()）
    gameView.startGame()
  }

  override fun onPause() {
    super.onPause()
    // 暂停游戏，避免后台耗电
    gameView.overGame()
  }

  override fun onDestroy() {
    super.onDestroy()
    // 彻底释放资源
    gameView.release()
  }
}