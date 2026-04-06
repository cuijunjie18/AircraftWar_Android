package edu.hitsz.aircraftwar.logic.bullet

import android.health.connect.datatypes.units.Power
import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.logic.basic.AbstractFlyingObject

abstract class BaseBullet : AbstractFlyingObject {
  var power: Int = 10
  constructor(locationX: Int, locationY: Int, speedX: Int, speedY: Int, power: Int) : super(locationX, locationY, speedX, speedY) {
    this.power = power
  }

  override fun forward() {
    super.forward()
    // 判定 x 轴出界
    if (locationX <= 0 || locationX >= AircraftWarApplication.SCREEN_WIDTH) {
      vanish();
    }

    // 判定 y 轴出界
    if (speedY > 0 && locationY >= AircraftWarApplication.SCREEN_HEIGHT) {
      // 向下飞行出界
      vanish();
    }else if (locationY <= 0){
      // 向上飞行出界
      vanish();
    }
  }
}