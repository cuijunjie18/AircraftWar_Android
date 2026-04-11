package edu.hitsz.aircraftwar.logic.strategy

import edu.hitsz.aircraftwar.logic.bullet.BaseBullet
import edu.hitsz.aircraftwar.logic.utils.Utils
import java.util.LinkedList

class NormalShoot: ShootStrategy {
  constructor() : super()
  override fun shoot(
    x: Int,
    y: Int,
    speedX: Int,
    speedY: Int,
    power: Int,
    bulletType: Int
  ): MutableList<BaseBullet?>? {
    val res: MutableList<BaseBullet?> = LinkedList<BaseBullet?>();
    val bullet: BaseBullet? = Utils.getBullet(x, y, speedX, speedY, power, bulletType);
    res.add(bullet);
    return res;
  }
}