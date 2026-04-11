package edu.hitsz.aircraftwar.logic.prop

import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.logic.basic.AbstractFlyingObject


open class BaseProp(locationX: Int, locationY: Int, speedX: Int, speedY: Int) :
  AbstractFlyingObject(locationX, locationY, speedX, speedY) {
  // 道具仅向下飞行
  public override fun forward() {
    locationX += speedX
    locationY += speedY
    if (locationX <= 0 || locationX >= AircraftWarApplication.SCREEN_WIDTH) {
      // 横向超出边界后反向
      speedX = -speedX
    }
    // 判定 y 轴向下飞行出界
    if (locationY >= AircraftWarApplication.SCREEN_HEIGHT) {
      vanish()
    }
  }

  open fun action() {}
}