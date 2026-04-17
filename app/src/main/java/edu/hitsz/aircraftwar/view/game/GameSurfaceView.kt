package edu.hitsz.aircraftwar.view.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import edu.hitsz.aircraftwar.logic.basic.AbstractFlyingObject
import edu.hitsz.aircraftwar.logic.utils.ImageManager
import edu.hitsz.aircraftwar.model.entity.EntityManager
import edu.hitsz.aircraftwar.model.state.GameState
import edu.hitsz.aircraftwar.setting.Setting

/**
 * 游戏渲染视图 - View层
 * 仅负责渲染画面和采集触屏输入，不包含任何游戏逻辑
 */
class GameSurfaceView(
    context: Context
) : SurfaceView(context), SurfaceHolder.Callback {

    companion object {
        private const val TAG = "GameSurfaceView"
    }

    // 渲染所需的Model引用
    private var entityManager: EntityManager? = null
    private var gameState: GameState? = null

    // 是否为联机模式
    private var isOnlineMode: Boolean = false

    // 背景图片
    private var backgroundBitmapScaled: Bitmap? = null
    private var backgroundTop: Int = 0

    // 画笔
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        textSize = 48f
        typeface = Typeface.DEFAULT_BOLD
    }

    // 回调接口
    /** 触屏事件回调 */
    var onTouchInput: ((Float, Float) -> Unit)? = null
    /** Surface就绪回调 */
    var onSurfaceReady: ((Int, Int) -> Unit)? = null
    /** Surface销毁回调 */
    var onSurfaceDestroyed: (() -> Unit)? = null

    init {
        holder.addCallback(this)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    /**
     * 绑定Model数据
     */
    fun bindModel(entityManager: EntityManager, gameState: GameState, isOnline: Boolean = false) {
        this.entityManager = entityManager
        this.gameState = gameState
        this.isOnlineMode = isOnline
    }

    // ======================
    // SurfaceHolder回调
    // ======================

    override fun surfaceCreated(holder: SurfaceHolder) {
        initBackground()
        onSurfaceReady?.invoke(width, height)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // 屏幕尺寸变化时重新计算缩放（可选）
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        onSurfaceDestroyed?.invoke()
    }

    // ======================
    // 渲染方法
    // ======================

    /**
     * 渲染一帧
     */
    fun render() {
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

    /**
     * 初始化背景图片
     */
    private fun initBackground() {
        when (Setting.getDifficulty()) {
            "easy" -> {
                backgroundBitmapScaled = Bitmap.createScaledBitmap(
                    ImageManager.backgroundImageEasy, width, height, false
                )
            }
            "medium" -> {
                backgroundBitmapScaled = Bitmap.createScaledBitmap(
                    ImageManager.backgroundImageNormal, width, height, false
                )
            }
            "hard" -> {
                backgroundBitmapScaled = Bitmap.createScaledBitmap(
                    ImageManager.backgroundImageHard, width, height, false
                )
            }
        }
    }

    /**
     * 绘制游戏画面
     */
    private fun drawGame(canvas: Canvas) {
        // 绘制滚动背景
        backgroundBitmapScaled?.let { bg ->
            canvas.drawBitmap(bg, 0f, (backgroundTop - height).toFloat(), paint)
            canvas.drawBitmap(bg, 0f, backgroundTop.toFloat(), paint)
            backgroundTop += 1
            if (backgroundTop >= height) {
                backgroundTop = 0
            }
        }

        val em = entityManager ?: return
        val gs = gameState ?: return

        // 绘制子弹（子弹在飞机下方）
        drawObjects(canvas, em.enemyBullets)
        drawObjects(canvas, em.heroBullets)

        // 绘制敌机
        drawObjects(canvas, em.enemyAircrafts)

        // 绘制道具
        drawObjects(canvas, em.props)

        // 绘制英雄飞机
        if (em.isHeroInitialized()) {
            val heroImg = ImageManager.heroImage
            canvas.drawBitmap(
                heroImg,
                (em.heroAircraft.getLocationXVal() - heroImg.width / 2).toFloat(),
                (em.heroAircraft.getLocationYVal() - heroImg.height / 2).toFloat(),
                paint
            )
        }

        // 绘制分数和生命值
        if (isOnlineMode) {
            drawOnlineScoreAndLife(canvas, gs, em)
        } else {
            drawScoreAndLife(canvas, gs, em)
        }
    }

    /**
     * 绘制飞行物对象列表
     */
    private fun drawObjects(canvas: Canvas, objects: List<AbstractFlyingObject>) {
        for (obj in objects) {
            val image = ImageManager.get(obj) ?: continue
            canvas.drawBitmap(
                image,
                (obj.getLocationXVal() - image.width / 2).toFloat(),
                (obj.getLocationYVal() - image.height / 2).toFloat(),
                paint
            )
        }
    }

    /**
     * 绘制单机模式分数和生命值
     */
    private fun drawScoreAndLife(canvas: Canvas, gs: GameState, em: EntityManager) {
        val x = 20f
        var y = 60f
        canvas.drawText("SCORE: ${gs.score}", x, y, textPaint)
        y += 50f
        canvas.drawText("LIFE: ${em.heroAircraft.hp}", x, y, textPaint)
    }

    /**
     * 绘制联机模式分数和生命值
     */
    private fun drawOnlineScoreAndLife(canvas: Canvas, gs: GameState, em: EntityManager) {
        val x = 20f
        var y = 60f
        canvas.drawText("我的分数: ${gs.score}", x, y, textPaint)
        y += 50f
        canvas.drawText("对手分数: ${gs.opponentScore}", x, y, textPaint)
        y += 50f
        canvas.drawText("LIFE: ${em.heroAircraft.hp}", x, y, textPaint)

        // 本方已死亡但对战未结束，显示等待提示
        if (gs.gameOverFlag && !gs.onlineAllOver) {
            drawWaitingOverlay(canvas)
        }

        // 对战全部结束，显示最终结果
        if (gs.onlineAllOver) {
            drawFinalResult(canvas, gs)
        }
    }

    /**
     * 本方已死亡，等待对手结束
     */
    private fun drawWaitingOverlay(canvas: Canvas) {
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
    private fun drawFinalResult(canvas: Canvas, gs: GameState) {
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
        canvas.drawText("我的分数: ${gs.score}", width / 2f, height / 2f - 30f, scorePaint)
        canvas.drawText("对手分数: ${gs.opponentScore}", width / 2f, height / 2f + 40f, scorePaint)

        // 显示胜负结果
        val resultText = when {
            gs.score > gs.opponentScore -> "你赢了！"
            gs.score < gs.opponentScore -> "你输了！"
            else -> "平局！"
        }
        val resultPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = when {
                gs.score > gs.opponentScore -> Color.GREEN
                gs.score < gs.opponentScore -> Color.RED
                else -> Color.CYAN
            }
            textSize = 80f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(resultText, width / 2f, height / 2f + 140f, resultPaint)
    }

    // ======================
    // 触屏控制
    // ======================

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                if (x < 0 || x > width || y < 0 || y > height) {
                    return true
                }
                onTouchInput?.invoke(x, y)
            }
        }
        return true
    }
}
