package edu.hitsz.aircraftwar.logic.aircraft

import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.logic.bullet.BaseBullet
import java.util.LinkedList

class BossEnemy: AbstractAircraft {
  private var shootMode: String = "WAVE"

  /**
   * 子弹伤害
   */
  private val power = 30

  /**
   * 子弹射击方向 (向下发射：1，向上发射：-1)
   */
  private val direction = 1

  private val shootInterval = 8 // 射击间隔
  private var shootCount = 7 // 射击计数

  constructor(locationX: Int, locationY: Int, speedX: Int, speedY: Int, hp: Int) : super(locationX, locationY, speedX, speedY, hp) {
    setShootStrategy(shootMode)
  }

  public override fun forward() {
    super.forward()
    // 判定 y 轴向下飞行出界
    if (locationY >= AircraftWarApplication.SCREEN_HEIGHT) {
      vanish()
    }
  }

  public override fun shoot(): MutableList<BaseBullet?>? { // 实现散射模式
    shootCount++
    if (shootCount % shootInterval !== 0) { // 控制射击频率
      return LinkedList<BaseBullet?>()
    }
    shootCount = 0
    val x = this.locationX
    val y = this.locationY + direction * 2
    val speedX = this.speedX
    val speedY = this.speedY + direction * 2
    return shootStrategy!!.shoot(x, y, speedX, speedY, power, 1)
  }

  public override fun update() {
    decreaseHp(40)
  }
}