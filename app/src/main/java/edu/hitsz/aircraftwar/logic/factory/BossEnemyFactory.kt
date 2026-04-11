package edu.hitsz.aircraftwar.logic.factory

import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.logic.aircraft.AbstractAircraft
import edu.hitsz.aircraftwar.logic.aircraft.BossEnemy
import edu.hitsz.aircraftwar.logic.utils.ImageManager


class BossEnemyFactory : EnemyFactory {
  override fun createEnemy(): AbstractAircraft? {
    val speedX = if (Math.random() > 0.5) 2 else -2 // 随机水平速度方向
    val speedY = 0
    val hp = 160
    return BossEnemy(
      (Math.random() * (AircraftWarApplication.SCREEN_WIDTH - ImageManager.bossEnemyImage.getWidth())).toInt(),
      (Math.random() * AircraftWarApplication.SCREEN_HEIGHT * 0.2).toInt(),
      speedX,
      speedY,
      hp
    ) // 更高的血量与拥有水平速度
  }
}