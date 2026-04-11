package edu.hitsz.aircraftwar.logic.strategy

import edu.hitsz.aircraftwar.logic.bullet.BaseBullet
import edu.hitsz.aircraftwar.logic.utils.Utils
import edu.hitsz.aircraftwar.logic.utils.Utils.getBullet
import java.util.LinkedList


class ScatterShoot: ShootStrategy {
  constructor() : super()
  override fun shoot(
    x: Int,
    y: Int,
    speedX: Int,
    speedY: Int,
    power: Int,
    bulletType: Int
  ): MutableList<BaseBullet?>? {
    val shootNum = 3 // 固定三连发
    val res: MutableList<BaseBullet?> = LinkedList<BaseBullet?>()
    var bullet: BaseBullet?
    var speedXVary = -2 // 初始横向速度
    for (i in 0..<shootNum) {
      bullet = Utils.getBullet(x, y, speedXVary, speedY, power, bulletType) // 横向速度补偿，提升视觉效果
      res.add(bullet)
      speedXVary += 2
    }
    return res
  }
}