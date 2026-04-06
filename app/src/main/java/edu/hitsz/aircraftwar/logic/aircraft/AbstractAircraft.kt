package edu.hitsz.aircraftwar.logic.aircraft

import edu.hitsz.aircraftwar.logic.basic.AbstractFlyingObject
import edu.hitsz.aircraftwar.logic.bullet.BaseBullet



abstract class AbstractAircraft : AbstractFlyingObject {
  private var maxHp: Int = 0
  var hp: Int = 0

  constructor(locationX: Int, locationY: Int, speedX: Int, speedY: Int, hp: Int) : super(locationX, locationY, speedX, speedY) {
    this.hp = hp
    this.maxHp = hp
  }

  fun decreaseHp(decrease: Int) {
    hp -= decrease
    if (hp <= 0) {
      hp = 0
      vanish()
    }
  }

  abstract fun shoot(): MutableList<BaseBullet?>?
}