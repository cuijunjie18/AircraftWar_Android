package edu.hitsz.aircraftwar.logic.factory

import edu.hitsz.aircraftwar.logic.prop.BaseProp
import edu.hitsz.aircraftwar.logic.prop.PropBulletPlus


class PropBulletPlusFactory : PropFactory {
  override fun createProp(x: Int, y: Int): BaseProp? {
    val speedX = if (Math.random() > 0.5) 3 else -3 // 随机水平速度方向
    val speedY = 5
    return PropBulletPlus(
      x, y,
      speedX,
      speedY
    )
  }
}