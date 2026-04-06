package edu.hitsz.aircraftwar.logic.aircraft

import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.logic.bullet.BaseBullet
import java.util.LinkedList


class MobEnemy: AbstractAircraft {

  constructor(locationX: Int, locationY: Int, speedX: Int, speedY: Int, hp: Int) : super(locationX, locationY, speedX, speedY, hp) {

  }

  public override fun forward() {
    super.forward()
    // 判定 y 轴向下飞行出界
    if (locationY >= AircraftWarApplication.SCREEN_HEIGHT) {
      vanish()
    }
  }

  public override fun shoot(): MutableList<BaseBullet?>? {
    return LinkedList<BaseBullet?>()
  }
}