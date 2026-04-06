package edu.hitsz.aircraftwar.Views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.logic.aircraft.AbstractAircraft
import edu.hitsz.aircraftwar.logic.aircraft.HeroAircraft
import edu.hitsz.aircraftwar.logic.aircraft.MobEnemy
import edu.hitsz.aircraftwar.logic.basic.AbstractFlyingObject
import edu.hitsz.aircraftwar.logic.bullet.BaseBullet
import edu.hitsz.aircraftwar.logic.bullet.EnemyBullet
import edu.hitsz.aircraftwar.logic.utils.ImageManager
import kotlin.compareTo
import kotlin.div
import kotlin.text.toInt


/**
 * 游戏主视图 - 替代 Swing 的 Game JPanel
 * 使用 SurfaceView 支持高性能游戏渲染
 */
class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

  companion object {
    private const val TAG = "GameView"

    // 目标帧率
    private const val TARGET_FPS = 60
    private const val FRAME_INTERVAL = 1000L / TARGET_FPS  // ~16ms
  }

  // 背景图片
  private var backgroundBitmapScaled: Bitmap? = null
  private var backgroundTop: Int = 0

  // 子弹发射间隔控制
  private val cycleDuration = 600
  private var cycleTime = 0
  private var timeInterval = 40

  // Game over标识
  private var gameOverFlag = false
  var onGameOver: ((Int) -> Unit)? = null // 游戏结束回调

  // 分数
  private var score = 0

  // 敌人数量
  private var enemyMaxNumber = 10

  //  画笔，用于绘制图像、文字
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.RED
    textSize = 48f
    typeface = Typeface.DEFAULT_BOLD
  }

  // 游戏对象
  private lateinit var heroAircraft: HeroAircraft
  private var enemyAircrafts: MutableList<AbstractAircraft> = mutableListOf()
  private var heroBullets: MutableList<BaseBullet> = mutableListOf()
  private var enemyBullets: MutableList<BaseBullet> = mutableListOf()

  // SurfaceHolder，控制surface的创建和销毁
  private val surfaceHolder: SurfaceHolder = holder
  private var gameThread: Thread? = null
  private var isRunning = false


  init {
    Log.d(TAG, "GameView created")
    surfaceHolder.addCallback(this) // 设置SurfaceHolder回调，监听surface创建和销毁
    isFocusable = true
    isFocusableInTouchMode = true
  }

  private fun initGameObjects() {
    heroAircraft = HeroAircraft(
      width / 2,
      height - ImageManager.heroImage.height,
      0, 0, 100
    )
  }

  // ======================
  // SurfaceHolder回调，监听surface创建和销毁
  // ======================

  override fun surfaceCreated(holder: SurfaceHolder) {
    assert(AircraftWarApplication.SCREEN_HEIGHT == height)
    assert(AircraftWarApplication.SCREEN_WIDTH == width)

    backgroundBitmapScaled = Bitmap.createScaledBitmap(ImageManager.backgroundImage, width, height, false)

    // 初始化游戏对象
    initGameObjects()
    isRunning = true
    startGame()
  }

  override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    // 屏幕尺寸变化时重新计算缩放（可选）
  }

  override fun surfaceDestroyed(holder: SurfaceHolder) {
    overGame()
  }


  // =======================
  // 游戏action相关
  // =======================

  fun startGame() {
    gameThread = Thread(this, "GameLoop").apply { start() }
    Log.d(TAG, "Game started")
  }
  fun pauseGame() {
    isRunning = false
  }
  fun overGame() {
    isRunning = false
    gameThread?.join()
    Log.d(TAG, "Game paused")
  }
  override fun run() {
    while (isRunning && !gameOverFlag) {
      val lastFrameTime = System.currentTimeMillis()

      // 敌机生成、子弹发射频率控制
      if (timeCountAndNewCycleJudge()) {
        // Spawn new enemy aircraft
        if (enemyAircrafts.size < enemyMaxNumber) {
          val mobWidth = ImageManager.mobEnemyImage.width
          enemyAircrafts.add(
            MobEnemy(
              (Math.random() * (width - mobWidth)).toInt(),
              (Math.random() * height * 0.05).toInt(),
              0,
              10,
              30
            )
          )
        }
        // 所有飞行器发送子弹
        shootAction()
      }

      // 子弹飞动
      bulletsMoveAction()

      // 飞行器移动
      aircraftsMoveAction()

      // 碰撞检测
      crashCheckAction()

      // 后处理
      postProcessAction()

      // 绘制帧
      drawFrame()

      // Check if hero is alive
      if (heroAircraft.hp <= 0) {
        gameOverFlag = true
        post { onGameOver?.invoke(score) }
      }


      // 帧率控制
      val currentTime = System.currentTimeMillis()
      val elapsed = currentTime - lastFrameTime
      val sleepTime = FRAME_INTERVAL - elapsed
      if (sleepTime > 0) {
        try {
          Thread.sleep(sleepTime)
        } catch (e: InterruptedException) {
          e.printStackTrace()
        }
      }
    }
  }
  private fun timeCountAndNewCycleJudge(): Boolean {
    cycleTime += timeInterval
    if (cycleTime >= cycleDuration) {
      cycleTime %= cycleDuration
      return true
    }
    return false
  }

  private fun shootAction() {
    // TODO: Enemy shooting

    // Hero shooting
    heroBullets.addAll(heroAircraft.shoot().filterNotNull())
  }

  private fun bulletsMoveAction() {
    for (bullet in heroBullets) {
      bullet.forward()
    }
    for (bullet in enemyBullets) {
      bullet.forward()
    }
  }

  private fun aircraftsMoveAction() {
    for (enemyAircraft in enemyAircrafts) {
      enemyAircraft.forward()
    }
  }

  private fun crashCheckAction() {
    // TODO: Enemy bullets attack hero

    // Hero bullets attack enemy
    for (bullet in heroBullets) {
      if (bullet.notValid()) continue
      for (enemyAircraft in enemyAircrafts) {
        if (enemyAircraft.notValid()) continue
        if (enemyAircraft.crash(bullet)) {
          // Enemy hit by hero bullet
          enemyAircraft.decreaseHp(bullet.power)
          bullet.vanish()
          if (enemyAircraft.notValid()) {
            // TODO: Score and supply drops
            score += 10
          }
        }
        // Hero and enemy collision - both destroyed
        if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
          enemyAircraft.vanish()
          heroAircraft.decreaseHp(Int.MAX_VALUE)
        }
      }
    }

    // TODO: Hero picks up supplies
  }

  private fun postProcessAction() {
    enemyBullets.removeAll { it.notValid() }
    heroBullets.removeAll { it.notValid() }
    enemyAircrafts.removeAll { it.notValid() }
  }

  // =====================
  //   Drawing methods
  // =====================

  private fun drawFrame() {
    var canvas: Canvas? = null
    try {
      canvas = holder.lockCanvas()
      if (canvas != null) {
        drawGame(canvas)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      if (canvas != null) {
        try {
          holder.unlockCanvasAndPost(canvas)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  private fun drawGame(canvas: Canvas) {
    // Draw scrolling background
    backgroundBitmapScaled?.let { bg ->
      canvas.drawBitmap(bg, 0f, (backgroundTop - height).toFloat(), paint)
      canvas.drawBitmap(bg, 0f, backgroundTop.toFloat(), paint)
      backgroundTop += 1
      if (backgroundTop >= height) {
        backgroundTop = 0
      }
    }

    // Draw bullets first, then aircraft (bullets appear below aircraft)
    drawObjects(canvas, enemyBullets)
    drawObjects(canvas, heroBullets)
    drawObjects(canvas, enemyAircrafts)

    // Draw hero aircraft
    val heroImg = ImageManager.heroImage
    canvas.drawBitmap(
      heroImg,
      (heroAircraft.getLocationXVal() - heroImg.width / 2).toFloat(),
      (heroAircraft.getLocationYVal() - heroImg.height / 2).toFloat(),
      paint
    )

    // Draw score and life
    drawScoreAndLife(canvas)
  }

  private fun drawObjects(canvas: Canvas, objects: List<AbstractFlyingObject>) {
    for (obj in objects) {
      val image = ImageManager.get(obj)?: continue
      canvas.drawBitmap(
        image,
        (obj.getLocationXVal() - image.width / 2).toFloat(),
        (obj.getLocationYVal() - image.height / 2).toFloat(),
        paint
      )
    }
  }

  private fun drawScoreAndLife(canvas: Canvas) {
    val x = 20f
    var y = 60f
    canvas.drawText("SCORE: $score", x, y, textPaint)
    y += 50f
    canvas.drawText("LIFE: ${heroAircraft.hp}", x, y, textPaint)
  }

  /**
   * 释放资源
   */
  fun release() {
    overGame()
  }

}