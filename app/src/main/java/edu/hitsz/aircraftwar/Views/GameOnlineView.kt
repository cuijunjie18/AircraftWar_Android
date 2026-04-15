package edu.hitsz.aircraftwar.Views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.R
import edu.hitsz.aircraftwar.logic.aircraft.AbstractAircraft
import edu.hitsz.aircraftwar.logic.aircraft.BossEnemy
import edu.hitsz.aircraftwar.logic.aircraft.EliteEnemy
import edu.hitsz.aircraftwar.logic.aircraft.HeroAircraft
import edu.hitsz.aircraftwar.logic.aircraft.MobEnemy
import edu.hitsz.aircraftwar.logic.aircraft.SuperEliteEnemy
import edu.hitsz.aircraftwar.logic.basic.AbstractFlyingObject
import edu.hitsz.aircraftwar.logic.bullet.BaseBullet
import edu.hitsz.aircraftwar.logic.factory.BossEnemyFactory
import edu.hitsz.aircraftwar.logic.factory.EliteEnemyFactory
import edu.hitsz.aircraftwar.logic.factory.MobEnemyFactory
import edu.hitsz.aircraftwar.logic.factory.PropBloodFactory
import edu.hitsz.aircraftwar.logic.factory.PropBombFactory
import edu.hitsz.aircraftwar.logic.factory.PropBulletFactory
import edu.hitsz.aircraftwar.logic.factory.PropBulletPlusFactory
import edu.hitsz.aircraftwar.logic.factory.SuperEliteEnemyFactory
import edu.hitsz.aircraftwar.logic.observer.BombSubject
import edu.hitsz.aircraftwar.logic.prop.BaseProp
import edu.hitsz.aircraftwar.logic.prop.PropBlood
import edu.hitsz.aircraftwar.logic.prop.PropBomb
import edu.hitsz.aircraftwar.logic.prop.PropBullet
import edu.hitsz.aircraftwar.logic.prop.PropBulletPlus
import edu.hitsz.aircraftwar.logic.utils.ImageManager
import edu.hitsz.aircraftwar.setting.Music.MusicManager
import edu.hitsz.aircraftwar.setting.Setting
import com.example.feature_online.OnlineGameClient
import java.util.Random


/**
 * 联机游戏主视图
 * 使用 SurfaceView 支持高性能游戏渲染
 * 通过 OnlineGameClient 与服务端通信，实时同步双方分数
 */
class GameOnlineView(context: Context, private val client: OnlineGameClient) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

  companion object {
    private const val TAG = "GameOnlineView"

    // 目标帧率
    private const val TARGET_FPS = 45 // 联机模式降低帧率60 -> 45，减少延迟
    private const val FRAME_INTERVAL = 1000L / TARGET_FPS  // ~22ms
  }

  // 飞行物列表
  private lateinit var heroAircraft: HeroAircraft
  private var enemyAircrafts: MutableList<AbstractAircraft> = mutableListOf()
  private var heroBullets: MutableList<BaseBullet> = mutableListOf()
  private var enemyBullets: MutableList<BaseBullet> = mutableListOf()
  private var props: MutableList<BaseProp> = mutableListOf()

  // 工厂
  private val mobEnemyFactory = MobEnemyFactory()
  private val eliteEnemyFactory = EliteEnemyFactory()
  private val superEliteEnemyFactory = SuperEliteEnemyFactory()
  private val bossEnemyFactory = BossEnemyFactory()
  private val propBloodFactory = PropBloodFactory()
  private val propBombFactory = PropBombFactory()
  private val propBulletFactory = PropBulletFactory()
  private val propBulletPlusFactory = PropBulletPlusFactory()


  /**
   * 屏幕中出现的敌机最大数量
   */
  private var enemyMaxNumber = 5
  private var bossExistFlag = 0 // 是否已经生成Boss
  private var bossKillCount = 0 // 已击毁Boss数量
  private var randomFactory: Random = Random() // 生成随机数

  /**
   * 敌机生成概率
   * 70% 生成普通敌机
   * 20% 生成精英敌机
   * 10% 生成超级精英敌机
   */
  private val enemyRate = 10

  /**
   * 道具生成概率
   * 30% 生成血量道具
   * 30% 生成炸弹道具
   * 30% 生成子弹道具(两种)
   * 10% 不生成道具
   */
  private val propRate = 10

  // 背景图片
  private var backgroundBitmapScaled: Bitmap? = null
  private var backgroundTop: Int = 0

  // 子弹发射间隔控制
  private val cycleDuration = 1200
  private var cycleTime = 0
  private var timeInterval = 40

  // Game over标识
  private var gameOverFlag = false       // 本方英雄是否死亡
  @Volatile
  private var onlineAllOver = false      // 对战是否全部结束（双方都死亡）
  var onGameOver: ((Int) -> Unit)? = null // 游戏结束回调

  // 分数
  private var score = 0                  // 本方分数
  @Volatile
  private var opponentScore = 0          // 对手分数
  private var lastSentScore = -1         // 上次发送的分数，避免重复发送

  // 观察者
  private var bombSubject: BombSubject = BombSubject()

  //  画笔，用于绘制图像、文字
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.RED
    textSize = 48f
    typeface = Typeface.DEFAULT_BOLD
  }

  // SurfaceHolder，控制surface的创建和销毁
  private val surfaceHolder: SurfaceHolder = holder
  private var gameThread: Thread? = null
  private var isRunning = false


  init {
    Log.d(TAG, "GameOnlineView created")
    surfaceHolder.addCallback(this) // 设置SurfaceHolder回调，监听surface创建和销毁
    isFocusable = true
    isFocusableInTouchMode = true

    // 设置网络数据接收回调
    client.onServerDataReceived = { oppScore, isAllOver ->
      opponentScore = oppScore
      if (isAllOver) {
        onlineAllOver = true
        Log.d(TAG, "对战结束！本方分数: $score, 对手分数: $oppScore")
      }
    }
  }

  private fun initGameObjects() {
    heroAircraft = HeroAircraft(
      width / 2,
      height - ImageManager.heroImage.height,
      0, 0, 100
    )
    heroAircraft.setShootStrategy("NORMAL")
  }

  // ======================
  // SurfaceHolder回调，监听surface创建和销毁
  // ======================

  override fun surfaceCreated(holder: SurfaceHolder) {
    assert(AircraftWarApplication.SCREEN_HEIGHT == height)
    assert(AircraftWarApplication.SCREEN_WIDTH == width)

    when (Setting.getDifficulty()) {
      "easy" -> {
        backgroundBitmapScaled = Bitmap.createScaledBitmap(ImageManager.backgroundImageEasy, width, height, false)
      }
      "medium" -> {
        backgroundBitmapScaled = Bitmap.createScaledBitmap(ImageManager.backgroundImageNormal, width, height, false)
      }
      "hard" -> {
        backgroundBitmapScaled = Bitmap.createScaledBitmap(ImageManager.backgroundImageHard, width, height, false)
      }
    }

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
    Log.d(TAG, "Game started")
    Log.d(TAG, "当前难度：${Setting.getDifficulty()}")
    Log.d(TAG, "音效开关：${Setting.musicOpen}")
    MusicManager.switchNormalBgm()
    gameThread = Thread(this, "GameLoop").apply { start() }
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
    while (isRunning) {
      val lastFrameTime = System.currentTimeMillis()

      // 本方未死亡时，执行正常游戏逻辑
      if (!gameOverFlag) {
        // 敌机生成、子弹发射频率控制
        if (timeCountAndNewCycleJudge()) {
          // Spawn new enemy aircraft
          if (enemyAircrafts.size < enemyMaxNumber) {
            val randomNum = randomFactory.nextInt(enemyRate).toInt()

            // 根据随机数生成普通、精英敌机、超级精英敌机
            if (randomNum == 0){
              enemyAircrafts.add(superEliteEnemyFactory.createEnemy()!!);
            }else if (randomNum == 1 || randomNum == 2){
              enemyAircrafts.add(eliteEnemyFactory.createEnemy()!!);
            }
            else{
              enemyAircrafts.add(mobEnemyFactory.createEnemy()!!);
            }
          }

          // 每300分产生一个Boss, 且场上只能有一个Boss(仅普通模式、困难模式)
          if (score >= (bossKillCount + 1) * 300 && score != 0 && bossExistFlag == 0
            && Setting.getDifficulty() != "easy"){
            MusicManager.switchBossBgm()
            enemyAircrafts.add(bossEnemyFactory.createEnemy()!!);
            bossExistFlag = 1;
          }

          // 观察者注册新敌机
          bombSubject.registerObserver(enemyAircrafts.get(enemyAircrafts.size - 1));

          // 所有飞行器发送子弹
          shootAction()
        }

        // 子弹飞动
        bulletsMoveAction()

        // 飞行器移动
        aircraftsMoveAction()

        // 道具移动
        propsMoveAction()

        // 碰撞检测
        crashCheckAction()

        // 后处理
        postProcessAction()

        // 检查英雄是否存活
        if (heroAircraft.hp <= 0) {
          if (Setting.musicOpen) {
            MusicManager.stopBgm()
            MusicManager.playSound(MusicManager.SoundType.GAME_OVER)
          }
          gameOverFlag = true
          // 发送死亡通知
          client.sendData(score, true)
          lastSentScore = score
          Log.d(TAG, "本方英雄阵亡，分数: $score")
        }

        // 发送本方分数到服务端（分数变化时才发送）
        if (score != lastSentScore) {
          client.sendData(score, false)
          lastSentScore = score
        }
      }

      // 无论是否死亡，都持续绘制帧（死亡后显示等待/结果画面）
      drawFrame()

      // 如果对战全部结束（双方都死亡），退出游戏循环
      if (onlineAllOver) {
        drawFrame() // 多绘制一帧确保最终结果显示
        Log.d(TAG, "对战结束，退出游戏循环")
        break
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

    // 敌机射击
    for (enemy in enemyAircrafts) {
      if (enemy is MobEnemy) continue  // 普通敌机不射击

      val bullets = enemy.shoot()
      enemyBullets.addAll(bullets!!.filterNotNull())
      for (bullet in bullets!!) {
        bombSubject.registerObserver(bullet)
      }
    }

    // 英雄射击
//    MusicManager.playSound(MusicManager.SoundType.BULLET_SHOOT) // 太吵了，去掉
    heroBullets.addAll(heroAircraft.shoot().filterNotNull())

    // 检查火力道具时间
    heroAircraft.checkShootModeDuration()
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

  private fun propsMoveAction() {
    for (prop in props) {
      prop.forward()
    }
  }

  private fun generateProp(x: Int, y: Int) {
    val randomNum: Int = randomFactory.nextInt(propRate).toInt()
    if (randomNum in 0..<3) { // 30% 生成血量道具
      props.add(propBloodFactory.createProp(x, y)!!)
    } else if (randomNum in 3..<6) { // 30% 生成炸弹道具
      props.add(propBombFactory.createProp(x, y)!!)
    } else if (randomNum in 6..<9) { // 30% 生成子弹道具(两种)
      val bulletType: Int = randomFactory.nextInt(2)
      if (bulletType == 0) {
        props.add(propBulletFactory.createProp(x, y)!!)
      } else if (bulletType == 1) {
        props.add(propBulletPlusFactory.createProp(x, y)!!)
      }
    } else { // 10% 不生成道具
      return
    }
  }

  private fun crashCheckAction() {

    // 敌机子弹攻击英雄
    for (bullet in enemyBullets) {
      if (bullet.notValid()) continue
      if (heroAircraft.crash(bullet)) {
        heroAircraft.decreaseHp(bullet.power)
        bullet.vanish()
      }
    }

    // 英雄子弹与敌机碰撞检测
    for (bullet in heroBullets) {
      if (bullet.notValid()) continue
      for (enemyAircraft in enemyAircrafts) {
        if (enemyAircraft.notValid()) continue
        if (enemyAircraft.crash(bullet)) {
          MusicManager.playSound(MusicManager.SoundType.BULLET_HIT)
          // Enemy hit by hero bullet
          enemyAircraft.decreaseHp(bullet.power)
          bullet.vanish()
          if (enemyAircraft.notValid()) {
            val propX: Int = enemyAircraft.getLocationXVal()
            val propY: Int = enemyAircraft.getLocationYVal()
            when (enemyAircraft) {
              is MobEnemy -> {
                // Normal enemy
              }

              is EliteEnemy -> {
                generateProp(propX, propY)
              }

              is SuperEliteEnemy -> {
                generateProp(propX, propY)
              }
            }
            if (enemyAircraft is BossEnemy) { // 生成 <= 3个道具
              generateProp(propX, propY)
              generateProp((propX + 100) % AircraftWarApplication.SCREEN_WIDTH, propY + 20)
              generateProp((propX + 200) % AircraftWarApplication.SCREEN_WIDTH, propY + 50)
              bossExistFlag = 0 // Boss被击毁，标志复位
              bossKillCount += 1
            }
          }
        }
      }
    }

    // 英雄飞机与敌机碰撞检测（独立于子弹循环）
    for (enemyAircraft in enemyAircrafts) {
      if (enemyAircraft.notValid()) continue
      if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
        enemyAircraft.vanish()
        heroAircraft.decreaseHp(heroAircraft.hp)
      }
    }

    for (prop in props) {
      if (prop.notValid()) continue
      if (heroAircraft.crash(prop)) {
        MusicManager.playSound(MusicManager.SoundType.GET_PROP)
        prop.vanish()
        when (prop) {
          is PropBlood -> {
            heroAircraft.increaseHp(30)
          }

          is PropBullet -> {
            heroAircraft.changeShootMode("SCATTER")
          }

          is PropBulletPlus -> {
            heroAircraft.changeShootMode("WAVE")
          }

          is PropBomb -> {
            if (Setting.musicOpen) {
              MusicManager.playSound(MusicManager.SoundType.BOMB_EXPLOSION)
            }
            prop.action()
            bombSubject.notifyObservers()
          }
        }
      }
    }
  }

  private fun postProcessAction() {
    // 移除无效敌机
    val aircraftIter = enemyAircrafts.iterator()
    while (aircraftIter.hasNext()) {
      val aircraft = aircraftIter.next()
      if (!aircraft.notValid()) continue  // 注意：notValid() 返回 true 表示无效

      // 获得分数
      when (aircraft) {
        is MobEnemy -> score += 10
        is EliteEnemy -> score += 20
        is SuperEliteEnemy -> score += 30
        is BossEnemy -> {
          score += 50
          MusicManager.switchNormalBgm()
        }
      }


      bombSubject.removeObserver(aircraft) // 先取消注册
      aircraftIter.remove() // 再从集合移除
    }

    // 移除无效子弹
    val bulletIter = enemyBullets.iterator()
    while (bulletIter.hasNext()) {
      val bullet = bulletIter.next()
      if (!bullet.notValid()) continue
      bombSubject.removeObserver(bullet)
      bulletIter.remove()
    }

    heroBullets.removeAll { it.notValid() }
    props.removeAll { it.notValid() }
    enemyBullets.removeAll { it.notValid() }
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
    drawObjects(canvas, props)

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
    canvas.drawText("我的分数: $score", x, y, textPaint)
    y += 50f
    canvas.drawText("对手分数: $opponentScore", x, y, textPaint)
    y += 50f
    canvas.drawText("LIFE: ${heroAircraft.hp}", x, y, textPaint)

    // 本方已死亡但对战未结束，显示等待提示
    if (gameOverFlag && !onlineAllOver) {
      drawWaitingOverlay(canvas)
    }

    // 对战全部结束，显示最终结果
    if (onlineAllOver) {
      drawFinalResult(canvas)
    }
  }

  /**
   * 本方已死亡，等待对手结束
   */
  private fun drawWaitingOverlay(canvas: Canvas) {
    // 半透明遮罩
    val overlayPaint = Paint().apply { color = Color.argb(150, 0, 0, 0) }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

    val tipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      textSize = 64f
      typeface = Typeface.DEFAULT_BOLD
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText("你已阵亡", width / 2f, height / 2f - 60f, tipPaint)
    tipPaint.textSize = 40f
    canvas.drawText("等待对手结束...", width / 2f, height / 2f + 20f, tipPaint)
  }

  /**
   * 双方都死亡，显示最终对战结果
   */
  private fun drawFinalResult(canvas: Canvas) {
    // 半透明遮罩
    val overlayPaint = Paint().apply { color = Color.argb(180, 0, 0, 0) }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.YELLOW
      textSize = 72f
      typeface = Typeface.DEFAULT_BOLD
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText("对战结束", width / 2f, height / 2f - 120f, titlePaint)

    val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      textSize = 52f
      typeface = Typeface.DEFAULT_BOLD
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText("我的分数: $score", width / 2f, height / 2f - 30f, scorePaint)
    canvas.drawText("对手分数: $opponentScore", width / 2f, height / 2f + 40f, scorePaint)

    // 显示胜负结果
    val resultText = when {
      score > opponentScore -> "你赢了！"
      score < opponentScore -> "你输了！"
      else -> "平局！"
    }
    val resultPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = when {
        score > opponentScore -> Color.GREEN
        score < opponentScore -> Color.RED
        else -> Color.CYAN
      }
      textSize = 80f
      typeface = Typeface.DEFAULT_BOLD
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText(resultText, width / 2f, height / 2f + 140f, resultPaint)
  }

  // =====================
  //   Touch control
  // =====================

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (gameOverFlag) { // 游戏结束，不响应触摸事件
      return true
    }
    when (event.action) {
      MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
        val x = event.x
        val y = event.y
        if (x < 0 || x > width || y < 0 || y > height) {
          return true
        }
        if (::heroAircraft.isInitialized) {
          heroAircraft.setLocation(x.toDouble(), y.toDouble())
        }
      }
    }
    return true
  }

  /**
   * 释放资源
   */
  fun release() {
    overGame()
    client.disconnect()
  }

}