package edu.hitsz.aircraftwar.logic.observer

import edu.hitsz.aircraftwar.logic.basic.AbstractFlyingObject


class BombSubject : Subject {
  private val observersFlying: MutableList<AbstractFlyingObject> = ArrayList<AbstractFlyingObject>()

  override fun registerObserver(`object`: AbstractFlyingObject?) {
    observersFlying.add(`object`!!)
  }

  override fun removeObserver(`object`: AbstractFlyingObject?) {
    observersFlying.remove(`object`)
  }

  override fun notifyObservers() {
    for (`object` in observersFlying) {
      `object`.update()
    }
  }
}