package edu.hitsz.aircraftwar.logic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import edu.hitsz.aircraftwar.R
import edu.hitsz.aircraftwar.logic.aircraft.HeroAircraft
import edu.hitsz.aircraftwar.logic.aircraft.MobEnemy
import edu.hitsz.aircraftwar.logic.bullet.EnemyBullet
import edu.hitsz.aircraftwar.logic.bullet.HeroBullet

object ImageManager {

  /** Class name to image mapping */
  private val classNameImageMap = HashMap<String, Bitmap>()

  lateinit var backgroundImage: Bitmap
    private set
  lateinit var heroImage: Bitmap
    private set
  lateinit var heroBulletImage: Bitmap
    private set
  lateinit var enemyBulletImage: Bitmap
    private set
  lateinit var mobEnemyImage: Bitmap
    private set

  /**
   * Initialize all images from resources.
   * Must be called before using any images.
   */
  fun init(context: Context) {
    val resources = context.resources

    backgroundImage = BitmapFactory.decodeResource(resources, R.drawable.bg)
    heroImage = BitmapFactory.decodeResource(resources, R.drawable.hero)
    mobEnemyImage = BitmapFactory.decodeResource(resources, R.drawable.mob)
    heroBulletImage = BitmapFactory.decodeResource(resources, R.drawable.bullet_hero)
    enemyBulletImage = BitmapFactory.decodeResource(resources, R.drawable.bullet_enemy)

    classNameImageMap[HeroAircraft::class.java.name] = heroImage
    classNameImageMap[MobEnemy::class.java.name] = mobEnemyImage
    classNameImageMap[HeroBullet::class.java.name] = heroBulletImage
    classNameImageMap[EnemyBullet::class.java.name] = enemyBulletImage
  }

  fun get(className: String): Bitmap? {
    return classNameImageMap[className]
  }

  fun get(obj: Any?): Bitmap? {
    if (obj == null) return null
    return get(obj::class.java.name)
  }
}