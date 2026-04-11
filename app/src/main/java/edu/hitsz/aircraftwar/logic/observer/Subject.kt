package edu.hitsz.aircraftwar.logic.observer

import edu.hitsz.aircraftwar.logic.basic.AbstractFlyingObject


interface Subject {
  fun registerObserver(`object`: AbstractFlyingObject?)

  fun removeObserver(`object`: AbstractFlyingObject?)

  fun notifyObservers()
}