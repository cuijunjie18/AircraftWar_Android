package edu.hitsz.aircraftwar.view.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.R
import edu.hitsz.aircraftwar.Views.BaseActivity
import edu.hitsz.aircraftwar.Views.RankActivity
import edu.hitsz.aircraftwar.controller.GameController
import edu.hitsz.aircraftwar.logic.utils.ImageManager
import edu.hitsz.aircraftwar.model.config.GameConfig
import edu.hitsz.aircraftwar.model.entity.EntityManager
import edu.hitsz.aircraftwar.model.repository.ScoreRepository
import edu.hitsz.aircraftwar.model.state.GameState
import edu.hitsz.aircraftwar.setting.Setting
import edu.hitsz.aircraftwar.view.game.GameSurfaceView

/**
 * 游戏Activity - View层
 * 替代原有的 MainActivity，负责创建 Model、Controller、View 并连接
 */
class GameActivity : BaseActivity() {

    companion object {
        const val TAG = "GameActivity"
    }

    // MVC 组件
    private lateinit var gameState: GameState
    private lateinit var entityManager: EntityManager
    private lateinit var gameConfig: GameConfig
    private lateinit var gameController: GameController
    private lateinit var gameSurfaceView: GameSurfaceView

    private var isGameOver = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        // 初始化图片管理器
        ImageManager.init(AircraftWarApplication.context)

        // 1. 创建 Model 层
        gameState = GameState()
        entityManager = EntityManager()
        gameConfig = GameConfig(Setting.difficulty)

        // 2. 创建 Controller 层
        gameController = GameController(gameConfig, entityManager, gameState) { score ->
            Log.d(TAG, "game over: $score")
            runOnUiThread { gameOver(score) }
        }

        // 3. 创建 View 层
        gameSurfaceView = GameSurfaceView(AircraftWarApplication.context)
        gameSurfaceView.bindModel(entityManager, gameState, isOnline = false)

        val container = findViewById<FrameLayout>(R.id.game_container)
        container.addView(gameSurfaceView)

        // 4. 连接 View 和 Controller
        // View -> Controller: 触屏事件
        gameSurfaceView.onTouchInput = { x, y ->
            gameController.handleTouchInput(x, y)
        }

        // View -> Controller: Surface就绪时初始化游戏对象并启动
        gameSurfaceView.onSurfaceReady = { width, height ->
            entityManager.initHero(
                width / 2,
                height - ImageManager.heroImage.height
            )
            if (!isGameOver) {
                gameController.start()
            }
        }

        // View -> Controller: Surface销毁时停止游戏
        gameSurfaceView.onSurfaceDestroyed = {
            gameController.stop()
        }

        // Controller -> View: 每帧渲染回调
        gameController.onFrameReady = {
            gameSurfaceView.render()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun gameOver(score: Int) {
        isGameOver = true
        // 通过 ScoreRepository 保存数据
        ScoreRepository.saveGameResult(score)
        // 先停止游戏线程
        gameController.stop()
        // 跳转到排行榜
        val intent = Intent(this, RankActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if (!isGameOver && ::gameController.isInitialized) {
            // Surface重新创建时会自动启动游戏
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isGameOver && ::gameController.isInitialized) {
            gameController.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::gameController.isInitialized) {
            gameController.stop()
        }
    }
}
