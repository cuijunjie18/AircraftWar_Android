package edu.hitsz.aircraftwar.controller

import android.util.Log
import edu.hitsz.aircraftwar.model.config.GameConfig
import edu.hitsz.aircraftwar.model.entity.EntityManager
import edu.hitsz.aircraftwar.model.state.GameState
import edu.hitsz.aircraftwar.setting.Music.MusicManager
import edu.hitsz.aircraftwar.setting.Setting

/**
 * 游戏主循环控制器 - Controller层
 * 驱动游戏循环，协调各子控制器
 */
open class GameController(
    protected val gameConfig: GameConfig,
    protected val entityManager: EntityManager,
    protected val gameState: GameState,
    private val onGameOver: (Int) -> Unit
) : Runnable {

    companion object {
        private const val TAG = "GameController"
    }

    protected val spawnController = SpawnController(gameConfig, entityManager, gameState)
    protected val collisionController = CollisionController(entityManager, gameState, spawnController)
    protected val scoreController = ScoreController(gameState)

    private var gameThread: Thread? = null
    @Volatile
    protected var isRunning = false

    /** View层渲染回调 */
    var onFrameReady: (() -> Unit)? = null

    /**
     * 启动游戏
     */
    open fun start() {
        Log.d(TAG, "Game started")
        Log.d(TAG, "当前难度：${gameConfig.difficultyStr}")
        Log.d(TAG, "音效开关：${Setting.musicOpen}")
        isRunning = true
        MusicManager.switchNormalBgm()
        gameThread = Thread(this, "GameLoop").apply { start() }
    }

    /**
     * 暂停游戏
     */
    fun pause() {
        isRunning = false
    }

    /**
     * 停止游戏
     */
    fun stop() {
        isRunning = false
        gameThread?.join()
        Log.d(TAG, "Game stopped")
    }

    /**
     * 处理触屏输入
     */
    fun handleTouchInput(x: Float, y: Float) {
        if (entityManager.isHeroInitialized()) {
            entityManager.heroAircraft.setLocation(x.toDouble(), y.toDouble())
        }
    }

    /**
     * 游戏主循环
     */
    override fun run() {
        while (isRunning && !gameState.gameOverFlag) {
            val startTime = System.currentTimeMillis()

            // 1. 敌机生成、子弹发射频率控制
            if (timeCountAndNewCycleJudge()) {
                spawnController.spawnEnemies()
                spawnController.shootAll()
            }

            // 2. 移动所有实体
            entityManager.moveAllEntities()

            // 3. 碰撞检测
            collisionController.checkAllCollisions()

            // 4. 后处理（移除无效实体 + 计分）
            val removedAircrafts = entityManager.removeInvalidEntities()
            scoreController.processRemovedEntities(removedAircrafts)

            // 5. 通知View绘制
            onFrameReady?.invoke()

            // 6. 检查游戏结束
            if (entityManager.heroAircraft.hp <= 0) {
                if (Setting.musicOpen) {
                    MusicManager.stopBgm()
                    MusicManager.playSound(MusicManager.SoundType.GAME_OVER)
                }
                gameState.gameOverFlag = true
                onGameOver(gameState.score)
            }

            // 帧率控制
            val elapsed = System.currentTimeMillis() - startTime
            val sleepTime = gameConfig.frameInterval - elapsed
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 时间计数与新周期判断
     */
    protected fun timeCountAndNewCycleJudge(): Boolean {
        gameState.cycleTime += gameConfig.timeInterval
        if (gameState.cycleTime >= gameConfig.cycleDuration) {
            gameState.cycleTime %= gameConfig.cycleDuration
            return true
        }
        return false
    }
}
