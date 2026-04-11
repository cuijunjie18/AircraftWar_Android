package edu.hitsz.aircraftwar.logic.strategy

import edu.hitsz.aircraftwar.logic.bullet.BaseBullet
import edu.hitsz.aircraftwar.logic.utils.Utils.getBullet
import java.util.LinkedList
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


class WaveShoot: ShootStrategy {
  constructor() : super()
  override fun shoot(
    x: Int,
    y: Int,
    speedX: Int,
    speedY: Int,
    power: Int,
    bulletType: Int
  ): MutableList<BaseBullet?>? {
    val shootNum = 20 // 固定20颗子弹
    val res: MutableList<BaseBullet?> = LinkedList<BaseBullet?>()


    // 发射20颗子弹，分布在180度（π弧度）范围内：从 0 到 π
    for (i in 0..<shootNum) {
      // 角度从 0 到 π 均匀分布
      val angle = Math.PI * i / (shootNum - 1) // 当 shootNum > 1

      val speedX_divide: Int
      val speedY_divide: Int

      // 计算速度分量（屏幕坐标系：y 向下为正）
      if (bulletType === 1) {
        speedX_divide = (5 * cos(angle)).roundToInt().toInt()
        speedY_divide = (5 * sin(angle)).roundToInt().toInt()
      } else { // hero bullet
        speedX_divide = (10 * cos(angle)).roundToInt().toInt()
        speedY_divide = (-10 * sin(angle)).roundToInt().toInt()
      }

      val bullet = getBullet(x, y, speedX_divide, speedY_divide, power, bulletType)
      res.add(bullet)
    }
    return res
  }
}