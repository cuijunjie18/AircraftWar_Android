package edu.hitsz.aircraftwar.logic.factory

import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.logic.aircraft.AbstractAircraft
import edu.hitsz.aircraftwar.logic.aircraft.MobEnemy
import edu.hitsz.aircraftwar.logic.utils.ImageManager


class MobEnemyFactory : EnemyFactory {
  override fun createEnemy(): AbstractAircraft? {
    val speedX = 0
    val speedY = 10
    val hp = 30
    return MobEnemy(
      (Math.random() * (AircraftWarApplication.SCREEN_WIDTH - ImageManager.mobEnemyImage.getWidth())).toInt(),
      (Math.random() * AircraftWarApplication.SCREEN_HEIGHT * 0.05).toInt(),
      speedX,
      speedY,
      hp
    )
  }
}