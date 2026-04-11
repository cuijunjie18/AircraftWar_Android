package edu.hitsz.aircraftwar.logic.prop

import edu.hitsz.aircraftwar.logic.basic.AbstractFlyingObject


class PropBomb(locationX: Int, locationY: Int, speedX: Int, speedY: Int) :
  BaseProp(locationX, locationY, speedX, speedY) {
  private val observersFlying: MutableList<AbstractFlyingObject> = ArrayList<AbstractFlyingObject>()

  public override fun action() {
    println("BombSupply active!")
    notifyObservers()
  }

  fun registerObserver(`object`: AbstractFlyingObject?) {
    observersFlying.add(`object`!!)
  }

  fun removeObserver(`object`: AbstractFlyingObject?) {
    observersFlying.remove(`object`)
  }

  fun notifyObservers() {
    for (`object` in observersFlying) {
      `object`.update()
    }
  }
}