package edu.hitsz.aircraftwar.logic.factory

import edu.hitsz.aircraftwar.logic.prop.BaseProp


interface PropFactory {
  fun createProp(x: Int, y: Int): BaseProp?
}