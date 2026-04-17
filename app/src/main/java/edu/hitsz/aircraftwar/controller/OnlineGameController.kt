package edu.hitsz.aircraftwar.controller

import android.util.Log
import com.example.feature_online.OnlineGameClient
import edu.hitsz.aircraftwar.model.config.GameConfig
import edu.hitsz.aircraftwar.model.entity.EntityManager
import edu.hitsz.aircraftwar.model.state.GameState
import edu.hitsz.aircraftwar.setting.Music.MusicManager
import edu.hitsz.aircraftwar.setting.Setting

/**
 * 联机游戏控制器 - Controller层
 * 继承 GameController，增加网络同步逻辑
 */
class OnlineGameController(
    gameConfig: GameConfig,
    entityManager: EntityManager,
    gameState: GameState,
    private val client: OnlineGameClient,
    private val onOnlineGameOver: (Int) -> Unit
) : GameController(gameConfig, entityManager, gameState, onOnlineGameOver) {

    companion object {
        private const val TAG = "OnlineGameController"
    }

    init {
        // 设置网络数据接收回调
        client.onServerDataReceived = { oppScore, isAllOver ->
            gameState.opponentScore = oppScore
            if (isAllOver) {
                gameState.onlineAllOver = true
                Log.d(TAG, "对战结束！本方分数: ${gameState.score}, 对手分数: $oppScore")
            }
        }
    }

    /**
     * 处理触屏输入（游戏结束后不响应）
     */
    fun handleOnlineTouchInput(x: Float, y: Float) {
        if (!gameState.gameOverFlag) {
            handleTouchInput(x, y)
        }
    }

    /**
     * 联机游戏主循环
     */
    override fun run() {
        while (isRunning) {
            val startTime = System.currentTimeMillis()

            // 本方未死亡时，执行正常游戏逻辑
            if (!gameState.gameOverFlag) {
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

                // 5. 检查英雄是否存活
                if (entityManager.heroAircraft.hp <= 0) {
                    if (Setting.musicOpen) {
                        MusicManager.stopBgm()
                        MusicManager.playSound(MusicManager.SoundType.GAME_OVER)
                    }
                    gameState.gameOverFlag = true
                    // 发送死亡通知
                    client.sendData(gameState.score, true)
                    gameState.lastSentScore = gameState.score
                    Log.d(TAG, "本方英雄阵亡，分数: ${gameState.score}")
                }

                // 发送本方分数到服务端（分数变化时才发送）
                if (gameState.score != gameState.lastSentScore) {
                    client.sendData(gameState.score, false)
                    gameState.lastSentScore = gameState.score
                }
            }

            // 6. 无论是否死亡，都持续通知View绘制
            onFrameReady?.invoke()

            // 如果对战全部结束（双方都死亡），退出游戏循环
            if (gameState.onlineAllOver) {
                onFrameReady?.invoke() // 多绘制一帧确保最终结果显示
                Log.d(TAG, "对战结束，退出游戏循环")
                break
            }

            // 帧率控制（联机模式使用较低帧率）
            val elapsed = System.currentTimeMillis() - startTime
            val sleepTime = GameConfig.ONLINE_FRAME_INTERVAL - elapsed
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
     * 释放资源
     */
    fun release() {
        stop()
        client.disconnect()
    }
}
