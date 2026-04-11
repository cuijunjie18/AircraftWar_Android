package edu.hitsz.aircraftwar.logic.basic

import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.logic.aircraft.AbstractAircraft
import edu.hitsz.aircraftwar.logic.utils.ImageManager


abstract class AbstractFlyingObject {

  //locationX、locationY为图片中心位置坐标
  /**
   * x 轴坐标
   */
  protected var locationX: Int = 0

  /**
   * y 轴坐标
   */
  protected var locationY: Int = 0


  /**
   * x 轴移动速度
   */
  protected var speedX: Int = 0

  /**
   * y 轴移动速度
   */
  protected var speedY: Int = 0

  /**
   * x 轴长度，根据图片尺寸获得
   * -1 表示未设置
   */
  protected var width: Int = -1

  /**
   * y 轴长度，根据图片尺寸获得
   * -1 表示未设置
   */
  protected var height: Int = -1


  /**
   * 有效（生存）标记，
   * 通常标记为 false的对象会在下次刷新时清除
   */
  protected var isValid: Boolean = true

  constructor(locationX: Int, locationY: Int, speedX: Int, speedY: Int) {
    this.locationX = locationX
    this.locationY = locationY
    this.speedX = speedX
    this.speedY = speedY
  }

  fun getLocationXVal(): Int = locationX

  fun getLocationYVal(): Int = locationY

  /**
   * 可飞行对象根据速度移动
   * 若飞行对象触碰到横向边界，横向速度反向
   */
  open fun forward() {
    locationX += speedX
    locationY += speedY
    if (locationX <= 0 || locationX >= AircraftWarApplication.SCREEN_WIDTH) {
      // 横向超出边界后反向
      speedX = -speedX
    }
  }

  /**
   * 碰撞检测，当对方坐标进入我方范围，判定我方击中<br></br>
   * 对方与我方覆盖区域有交叉即判定撞击。
   * <br></br>
   * 非飞机对象区域：
   * 横向，[x - width/2, x + width/2]
   * 纵向，[y - height/2, y + height/2]
   * <br></br>
   * 飞机对象区域：
   * 横向，[x - width/2, x + width/2]
   * 纵向，[y - height/4, y + height/4]
   *
   * @param flyingObject 撞击对方
   * @return true: 我方被击中; false 我方未被击中
   */
  fun crash(flyingObject: AbstractFlyingObject): Boolean {
    // 缩放因子，用于控制 y轴方向区域范围
    val factor = if (this is AbstractAircraft) 2 else 1 //我方
    val fFactor = if (flyingObject is AbstractAircraft) 2 else 1 //对方

    //对方坐标、宽度、高度
    val x = flyingObject.locationX
    val y = flyingObject.locationY
    val fWidth = flyingObject.width
    val fHeight = flyingObject.height

    if (fWidth == -1 || fHeight == -1) {
      this.width = getWidthVal()
      this.height = getHeightVal()
    }

    return x + (fWidth + this.width) / 2 > locationX && x - (fWidth + this.width) / 2 < locationX && y + (fHeight / fFactor + this.height / factor) / 2 > locationY && y - (fHeight / fFactor + this.height / factor) / 2 < locationY
  }

  fun setLocation(locationX: Double, locationY: Double) {
    this.locationX = locationX.toInt()
    this.locationY = locationY.toInt()
  }

  fun getWidthVal(): Int {
    if (width == -1) {
      val image = ImageManager.get(this)
      if (image != null) {
        width = image.width
      }
    }
    return width
  }

  fun getHeightVal(): Int {
    if (height == -1) {
      val image = ImageManager.get(this)
      if (image != null) {
        height = image.height
      }
    }
    return height
  }

  fun notValid(): Boolean {
    return !this.isValid
  }

  /**
   * 标记消失，
   * isValid = false.
   * notValid() => true.
   */
  fun vanish() {
    isValid = false
  }

  // 用于观察者模式
  open fun update() {
  }
}