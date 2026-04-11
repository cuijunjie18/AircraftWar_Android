package edu.hitsz.aircraftwar.logic.strategy

import edu.hitsz.aircraftwar.logic.bullet.BaseBullet
import java.util.LinkedList



class NoneShoot : ShootStrategy {
  constructor() : super()
  override fun shoot(
    x: Int,
    y: Int,
    speedX: Int,
    speedY: Int,
    power: Int,
    bulletType: Int
  ): MutableList<BaseBullet?>? {
    // 不发射任何子弹，返回一个空列表（或者也可以返回 null）
    return mutableListOf()   // 返回非空空列表，类型自动推导为 MutableList<Nothing>，但可赋值给 MutableList<BaseBullet?>?
  }
}