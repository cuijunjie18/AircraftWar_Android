package edu.hitsz.aircraftwar.model.config

import edu.hitsz.aircraftwar.logic.difficulty.Difficulty
import edu.hitsz.aircraftwar.logic.difficulty.Easy
import edu.hitsz.aircraftwar.logic.difficulty.Hard
import edu.hitsz.aircraftwar.logic.difficulty.Medium

/**
 * 游戏配置类 - Model层
 * 根据难度整合所有游戏参数配置
 */
class GameConfig(val difficulty: Difficulty) {

    /** 屏幕中出现的敌机最大数量 */
    val enemyMaxNumber: Int

    /** 敌机生成随机数范围 */
    val enemyRate: Int = 10

    /** 道具生成随机数范围 */
    val propRate: Int = 10

    /** Boss生成分数阈值 */
    val bossScoreThreshold: Int = 300

    /** 子弹发射间隔周期（毫秒） */
    val cycleDuration: Int = 1200

    /** 时间间隔（毫秒） */
    val timeInterval: Int = 40

    /** 目标帧率 */
    val targetFps: Int

    /** 帧间隔（毫秒） */
    val frameInterval: Long

    /** 精英敌机概率 */
    val eliteProbability: Double

    /** 敌机能力倍率 */
    val enemyAbility: Double

    /** 难度字符串标识 */
    val difficultyStr: String

    init {
        when (difficulty) {
            is Easy -> {
                enemyMaxNumber = 4
                targetFps = 60
                eliteProbability = difficulty.eliteProbability
                enemyAbility = difficulty.enemyAbility
                difficultyStr = "easy"
            }
            is Medium -> {
                enemyMaxNumber = 5
                targetFps = 60
                eliteProbability = difficulty.eliteProbability
                enemyAbility = difficulty.enemyAbility
                difficultyStr = "medium"
            }
            is Hard -> {
                enemyMaxNumber = 6
                targetFps = 60
                eliteProbability = difficulty.eliteProbability
                enemyAbility = difficulty.enemyAbility
                difficultyStr = "hard"
            }
            else -> {
                enemyMaxNumber = 5
                targetFps = 60
                eliteProbability = 0.2
                enemyAbility = 1.0
                difficultyStr = "easy"
            }
        }
        frameInterval = 1000L / targetFps
    }

    /**
     * 联机模式配置：降低帧率以减少延迟
     */
    companion object {
        fun createOnlineConfig(difficulty: Difficulty): GameConfig {
            return GameConfig(difficulty).apply {
                // 联机模式使用较低帧率，通过返回新对象处理
            }
        }

        const val ONLINE_TARGET_FPS = 45
        val ONLINE_FRAME_INTERVAL = 1000L / ONLINE_TARGET_FPS
    }
}
