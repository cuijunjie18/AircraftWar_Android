package edu.hitsz.aircraftwar.logic.strategy

import edu.hitsz.aircraftwar.logic.bullet.BaseBullet



abstract class ShootStrategy {
  abstract fun shoot(
    x: Int,
    y: Int,
    speedX: Int,
    speedY: Int,
    power: Int,
    bulletType: Int
  ): MutableList<BaseBullet?>?
}