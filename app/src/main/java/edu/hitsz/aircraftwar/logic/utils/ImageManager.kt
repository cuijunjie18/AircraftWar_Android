package edu.hitsz.aircraftwar.logic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import edu.hitsz.aircraftwar.R
import edu.hitsz.aircraftwar.logic.aircraft.EliteEnemy
import edu.hitsz.aircraftwar.logic.aircraft.HeroAircraft
import edu.hitsz.aircraftwar.logic.aircraft.MobEnemy
import edu.hitsz.aircraftwar.logic.aircraft.*
import edu.hitsz.aircraftwar.logic.bullet.EnemyBullet
import edu.hitsz.aircraftwar.logic.bullet.HeroBullet
import edu.hitsz.aircraftwar.logic.prop.*

object ImageManager {

  /** Class name to image mapping */
  private val classNameImageMap = HashMap<String, Bitmap>()

  // 背景图片
  lateinit var backgroundImageEasy: Bitmap
  lateinit var backgroundImageNormal: Bitmap
  lateinit var backgroundImageHard: Bitmap

  // 飞机图片
  lateinit var heroImage: Bitmap
  lateinit var mobEnemyImage: Bitmap
  lateinit var eliteEnemyImage: Bitmap
  lateinit var superEliteEnemyImage: Bitmap
  lateinit var bossEnemyImage: Bitmap

  // 子弹图片
  lateinit var heroBulletImage: Bitmap
  lateinit var enemyBulletImage: Bitmap

  // 道具图片
  lateinit var propBloodImage: Bitmap
  lateinit var propBombImage: Bitmap
  lateinit var propBulletImage: Bitmap
  lateinit var propBulletPlusImage: Bitmap


  /**
   * Initialize all images from resources.
   * Must be called before using any images.
   */
  fun init(context: Context) {
    val resources = context.resources

    backgroundImageEasy = BitmapFactory.decodeResource(resources, R.drawable.bg)
    backgroundImageNormal = BitmapFactory.decodeResource(resources, R.drawable.bg3)
    backgroundImageHard = BitmapFactory.decodeResource(resources, R.drawable.bg5)
    heroImage = BitmapFactory.decodeResource(resources, R.drawable.hero)
    mobEnemyImage = BitmapFactory.decodeResource(resources, R.drawable.mob)
    eliteEnemyImage = BitmapFactory.decodeResource(resources, R.drawable.elite)
    superEliteEnemyImage = BitmapFactory.decodeResource(resources, R.drawable.elite_plus)
    bossEnemyImage = BitmapFactory.decodeResource(resources, R.drawable.boss)
    heroBulletImage = BitmapFactory.decodeResource(resources, R.drawable.bullet_hero)
    enemyBulletImage = BitmapFactory.decodeResource(resources, R.drawable.bullet_enemy)
    propBloodImage = BitmapFactory.decodeResource(resources, R.drawable.prop_blood)
    propBombImage = BitmapFactory.decodeResource(resources, R.drawable.prop_bomb)
    propBulletImage = BitmapFactory.decodeResource(resources, R.drawable.prop_bullet)
    propBulletPlusImage = BitmapFactory.decodeResource(resources, R.drawable.prop_bullet_plus)

    classNameImageMap[HeroAircraft::class.java.name] = heroImage
    classNameImageMap[MobEnemy::class.java.name] = mobEnemyImage
    classNameImageMap[HeroBullet::class.java.name] = heroBulletImage
    classNameImageMap[EnemyBullet::class.java.name] = enemyBulletImage
    classNameImageMap[EliteEnemy::class.java.name] = eliteEnemyImage
    classNameImageMap[SuperEliteEnemy::class.java.name] = superEliteEnemyImage
    classNameImageMap[BossEnemy::class.java.name] = bossEnemyImage
    classNameImageMap[PropBlood::class.java.name] = propBloodImage
    classNameImageMap[PropBomb::class.java.name] = propBombImage
    classNameImageMap[PropBullet::class.java.name] = propBulletImage
    classNameImageMap[PropBulletPlus::class.java.name] = propBulletPlusImage
  }

  fun get(className: String): Bitmap? {
    return classNameImageMap[className]
  }

  fun get(obj: Any?): Bitmap? {
    if (obj == null) return null
    return get(obj::class.java.name)
  }
}