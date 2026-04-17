package edu.hitsz.aircraftwar.model.repository

import edu.hitsz.aircraftwar.data.DataManager
import edu.hitsz.aircraftwar.data.SingleGameInfo
import edu.hitsz.aircraftwar.logic.utils.Utils
import edu.hitsz.aircraftwar.setting.Setting

/**
 * 分数数据仓库 - Model层
 * 封装 DataManager 的读写操作，提供统一的数据访问接口
 */
object ScoreRepository {

    /**
     * 保存单局游戏数据
     * @param score 游戏分数
     */
    fun saveGameResult(score: Int) {
        val dataInfo = SingleGameInfo().apply {
            this.score = score
            this.userName = Setting.userName
            this.date = Utils.getCurrentFormatTime()
        }
        DataManager.saveData(dataInfo)
    }

    /**
     * 加载指定难度的历史分数数据
     * @param difficulty 难度字符串
     * @return 分数列表（按分数降序排列）
     */
    fun loadScores(difficulty: String): List<SingleGameInfo> {
        return DataManager.loadData(difficulty)
    }

    /**
     * 加载当前难度的历史分数数据
     * @return 分数列表（按分数降序排列）
     */
    fun loadCurrentDifficultyScores(): List<SingleGameInfo> {
        return DataManager.loadData(Setting.getDifficulty())
    }
}
