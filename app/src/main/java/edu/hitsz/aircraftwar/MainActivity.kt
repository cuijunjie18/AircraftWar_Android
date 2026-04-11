package edu.hitsz.aircraftwar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import edu.hitsz.aircraftwar.Views.GameView
import edu.hitsz.aircraftwar.Views.RankActivity
import edu.hitsz.aircraftwar.data.DataManager
import edu.hitsz.aircraftwar.data.SingleGameInfo
import edu.hitsz.aircraftwar.logic.utils.ImageManager
import edu.hitsz.aircraftwar.logic.utils.Utils
import edu.hitsz.aircraftwar.setting.Setting

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

  @RequiresApi(Build.VERSION_CODES.O)
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

    // 定义游戏结束回调
    gameView.onGameOver = { score ->
      Log.d(TAG, "game over: $score")
      gameOver(score)
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  fun gameOver(score: Int) {
    var dataInfo: SingleGameInfo = SingleGameInfo()
    dataInfo.score = score
    dataInfo.userName = Setting.userName
    dataInfo.date = Utils.getCurrentFormatTime()
    DataManager.saveData(dataInfo)
    val intent = Intent(this, RankActivity::class.java)
    startActivity(intent)
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