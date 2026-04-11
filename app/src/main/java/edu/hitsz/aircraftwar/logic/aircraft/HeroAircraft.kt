package edu.hitsz.aircraftwar.logic.aircraft

import edu.hitsz.aircraftwar.logic.bullet.BaseBullet


class HeroAircraft: AbstractAircraft {


  /**攻击方式  */
  /**攻击方式  */
  private var shootMode: String = "NORMAL" // 默认
  private var shootTimes: Int = 0
  /**
   * 子弹一次发射数量
   */
  private var shootNum = 1

  /**
   * 子弹伤害
   */
  private var power = 30

  /**
   * 子弹射击方向 (向上发射：1，向下发射：-1)
   */
  private val direction = -1

  /**
   * @param locationX 英雄机位置x坐标
   * @param locationY 英雄机位置y坐标
   * @param speedX 英雄机射出的子弹的基准速度（英雄机无特定速度）
   * @param speedY 英雄机射出的子弹的基准速度（英雄机无特定速度）
   * @param hp    初始生命值
   */
  constructor(locationX: Int, locationY: Int, speedX: Int, speedY: Int, hp: Int) : super(locationX, locationY, speedX, speedY, hp) {
  }

  public override fun forward() {
    // 英雄机由触屏控制，不通过forward函数移动
  }

  // 改变射击模式
  fun changeShootMode(mode: String) {
    shootMode = mode
    shootTimes = 0 // 重置射击次数
    setShootStrategy(mode)
  }

  // 检查射击模式持续时间
  fun checkShootModeDuration() {
    if (shootMode != "NORMAL") {
      shootTimes++
      if (shootTimes >= 10) { // 持续200次射击后恢复普通模式
        changeShootMode("NORMAL")
        shootTimes = 0
      }
    }
  }

  /**
   * 通过射击产生子弹
   * @return 射击出的子弹List
   */
  public override fun shoot(): MutableList<BaseBullet?> {
    val x = this.locationX
    val y = this.locationY + direction * 2
    val speedX = 0
    val speedY = this.speedY + direction * 5
    return shootStrategy!!.shoot(x, y, speedX, speedY, power, 0)!!
  }
}