package edu.hitsz.aircraftwar.logic.aircraft

import edu.hitsz.aircraftwar.logic.basic.AbstractFlyingObject
import edu.hitsz.aircraftwar.logic.bullet.BaseBullet
import edu.hitsz.aircraftwar.logic.strategy.NormalShoot
import edu.hitsz.aircraftwar.logic.strategy.ScatterShoot
import edu.hitsz.aircraftwar.logic.strategy.ShootStrategy
import edu.hitsz.aircraftwar.logic.strategy.WaveShoot


abstract class AbstractAircraft : AbstractFlyingObject {
  var maxHp: Int = 0
  var hp: Int = 0

  // 射击策略接口
  protected var shootStrategy: ShootStrategy? = null

  constructor(locationX: Int, locationY: Int, speedX: Int, speedY: Int, hp: Int) : super(locationX, locationY, speedX, speedY) {
    this.hp = hp
    this.maxHp = hp
  }

  // 攻击策略选择
  fun setShootStrategy(strategy: String) {
    when (strategy) {
      "NORMAL" -> shootStrategy = NormalShoot()
      "SCATTER" -> shootStrategy = ScatterShoot()
      "WAVE" -> shootStrategy = WaveShoot()
      else -> {}
    }
  }

  fun decreaseHp(decrease: Int) {
    hp -= decrease
    if (hp <= 0) {
      hp = 0
      vanish()
    }
  }

  fun increaseHp(increase: Int) {
    hp += increase
    if (hp > maxHp) {
      hp = maxHp
    }
  }

  abstract fun shoot(): MutableList<BaseBullet?>?
}