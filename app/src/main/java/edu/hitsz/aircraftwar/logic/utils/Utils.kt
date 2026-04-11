package edu.hitsz.aircraftwar.logic.utils

import edu.hitsz.aircraftwar.logic.bullet.BaseBullet
import edu.hitsz.aircraftwar.logic.bullet.EnemyBullet
import edu.hitsz.aircraftwar.logic.bullet.HeroBullet


object Utils {
  fun getBullet(
    x: Int,
    y: Int,
    speedX: Int,
    speedY: Int,
    power: Int,
    bulletType: Int
  ): BaseBullet? {
    var bullet: BaseBullet? = null
    when (bulletType) {
      0 -> bullet = HeroBullet(x, y, speedX, speedY, power) // 发射一颗子弹
      1 -> bullet = EnemyBullet(x, y, speedX, speedY, power) // 发射一颗子弹
    }
    return bullet
  }
}