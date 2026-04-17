package edu.hitsz.aircraftwar.view.activity

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.feature_online.OnlineGameClient
import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.R
import edu.hitsz.aircraftwar.view.fragments.CreateRoomFragment
import edu.hitsz.aircraftwar.view.fragments.JoinRoomFragment
import edu.hitsz.aircraftwar.controller.OnlineGameController
import edu.hitsz.aircraftwar.logic.utils.ImageManager
import edu.hitsz.aircraftwar.model.config.GameConfig
import edu.hitsz.aircraftwar.model.entity.EntityManager
import edu.hitsz.aircraftwar.model.state.GameState
import edu.hitsz.aircraftwar.setting.Setting
import edu.hitsz.aircraftwar.view.game.GameSurfaceView

/**
 * 联机模式Activity - View层
 * 替代原有的 OnlineActivity，使用MVC架构
 */
class OnlineGameActivity : AppCompatActivity() {

    companion object {
        const val TAG = "OnlineGameActivity"
    }

    private lateinit var createRoomButton: Button
    private lateinit var joinRoomButton: Button

    // MVC 组件（游戏启动后初始化）
    private var gameState: GameState? = null
    private var entityManager: EntityManager? = null
    private var gameConfig: GameConfig? = null
    private var onlineGameController: OnlineGameController? = null
    private var gameSurfaceView: GameSurfaceView? = null

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

        // 1. 创建 Model 层
        val gs = GameState()
        val em = EntityManager()
        val gc = GameConfig(Setting.difficulty)
        this.gameState = gs
        this.entityManager = em
        this.gameConfig = gc

        // 2. 创建 Controller 层
        val controller = OnlineGameController(gc, em, gs, client) { score ->
            Log.d(TAG, "online game over: $score")
        }
        this.onlineGameController = controller

        // 3. 创建 View 层
        setContentView(R.layout.activity_main)
        val surfaceView = GameSurfaceView(this)
        surfaceView.bindModel(em, gs, isOnline = true)
        this.gameSurfaceView = surfaceView

        val container = findViewById<FrameLayout>(R.id.game_container)
        container.addView(surfaceView)

        // 4. 连接 View 和 Controller
        // View -> Controller: 触屏事件
        surfaceView.onTouchInput = { x, y ->
            controller.handleOnlineTouchInput(x, y)
        }

        // View -> Controller: Surface就绪时初始化游戏对象并启动
        surfaceView.onSurfaceReady = { width, height ->
            em.initHero(
                width / 2,
                height - ImageManager.heroImage.height
            )
            controller.start()
        }

        // View -> Controller: Surface销毁时停止游戏
        surfaceView.onSurfaceDestroyed = {
            controller.stop()
        }

        // Controller -> View: 每帧渲染回调
        controller.onFrameReady = {
            surfaceView.render()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onlineGameController?.release()
    }
}
