package edu.hitsz.aircraftwar.logic.aircraft

import edu.hitsz.aircraftwar.logic.bullet.BaseBullet
import edu.hitsz.aircraftwar.logic.bullet.HeroBullet
import java.util.LinkedList


class HeroAircraft: AbstractAircraft {


  /**攻击方式  */
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

  /**
   * 通过射击产生子弹
   * @return 射击出的子弹List
   */
  public override fun shoot(): MutableList<BaseBullet?> {
    val res: MutableList<BaseBullet?> = LinkedList<BaseBullet?>()
    val x = this.locationX
    val y = this.locationY + direction * 2
    val speedX = 0
    val speedY = this.speedY + direction * 5
    var bullet: BaseBullet?
    for (i in 0..<shootNum) {
      // 子弹发射位置相对飞机位置向前偏移
      // 多个子弹横向分散
      bullet = HeroBullet(x + (i * 2 - shootNum + 1) * 10, y, speedX, speedY, power)
      res.add(bullet)
    }
    return res
  }
}