package edu.hitsz.aircraftwar.controller

import edu.hitsz.aircraftwar.logic.aircraft.AbstractAircraft
import edu.hitsz.aircraftwar.logic.aircraft.BossEnemy
import edu.hitsz.aircraftwar.logic.aircraft.EliteEnemy
import edu.hitsz.aircraftwar.logic.aircraft.MobEnemy
import edu.hitsz.aircraftwar.logic.aircraft.SuperEliteEnemy
import edu.hitsz.aircraftwar.model.state.GameState
import edu.hitsz.aircraftwar.setting.Music.MusicManager

/**
 * 分数控制器 - Controller层
 * 管理分数计算和游戏结束判定
 */
class ScoreController(
    private val gameState: GameState
) {

    /**
     * 处理被移除的敌机，计算分数
     * @param removedAircrafts 本帧被移除的敌机列表
     */
    fun processRemovedEntities(removedAircrafts: List<AbstractAircraft>) {
        for (aircraft in removedAircrafts) {
            when (aircraft) {
                is MobEnemy -> gameState.addScore(10)
                is EliteEnemy -> gameState.addScore(20)
                is SuperEliteEnemy -> gameState.addScore(30)
                is BossEnemy -> {
                    gameState.addScore(50)
                    MusicManager.switchNormalBgm()
                }
            }
        }
    }
}
