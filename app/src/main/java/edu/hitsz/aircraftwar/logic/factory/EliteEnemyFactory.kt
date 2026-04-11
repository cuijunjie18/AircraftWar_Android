package edu.hitsz.aircraftwar.logic.factory

import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.logic.aircraft.AbstractAircraft
import edu.hitsz.aircraftwar.logic.aircraft.EliteEnemy
import edu.hitsz.aircraftwar.logic.utils.ImageManager


class EliteEnemyFactory : EnemyFactory {
  override fun createEnemy(): AbstractAircraft? {
    val speedX = if (Math.random() > 0.5) 2 else -2 // 随机水平速度方向
    val speedY = 5
    val hp = 60
    return EliteEnemy(
      (Math.random() * (AircraftWarApplication.SCREEN_WIDTH - ImageManager.eliteEnemyImage.getWidth())).toInt(),
      (Math.random() * AircraftWarApplication.SCREEN_HEIGHT * 0.05).toInt(),
      speedX,
      speedY,
      hp
    ) // 更高的血量与拥有水平速度
  }
}