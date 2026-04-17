package edu.hitsz.aircraftwar.model.state

/**
 * 游戏状态数据类 - Model层
 * 统一管理游戏运行时的所有状态数据
 */
class GameState {
    // 基础游戏状态
    var score: Int = 0
    var gameOverFlag: Boolean = false
    var bossExistFlag: Int = 0        // 0: 无Boss, 1: 有Boss
    var bossKillCount: Int = 0
    var cycleTime: Int = 0

    // 联机模式扩展
    @Volatile
    var opponentScore: Int = 0
    @Volatile
    var onlineAllOver: Boolean = false
    var lastSentScore: Int = -1       // 上次发送的分数，避免重复发送

    /**
     * 重置所有游戏状态
     */
    fun reset() {
        score = 0
        gameOverFlag = false
        bossExistFlag = 0
        bossKillCount = 0
        cycleTime = 0
        opponentScore = 0
        onlineAllOver = false
        lastSentScore = -1
    }

    /**
     * 增加分数
     */
    fun addScore(points: Int) {
        score += points
    }

    /**
     * 标记Boss已生成
     */
    fun markBossSpawned() {
        bossExistFlag = 1
    }

    /**
     * 标记Boss已被击杀
     */
    fun markBossKilled() {
        bossExistFlag = 0
        bossKillCount += 1
    }

    /**
     * 检查是否应该生成Boss
     * @param difficultyStr 当前难度字符串
     * @return 是否应该生成Boss
     */
    fun shouldSpawnBoss(difficultyStr: String): Boolean {
        return score >= (bossKillCount + 1) * 300
                && score != 0
                && bossExistFlag == 0
                && difficultyStr != "easy"
    }
}
