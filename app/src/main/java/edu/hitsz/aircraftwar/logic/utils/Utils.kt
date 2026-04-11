package edu.hitsz.aircraftwar.logic.utils

import android.os.Build
import androidx.annotation.RequiresApi
import edu.hitsz.aircraftwar.logic.bullet.BaseBullet
import edu.hitsz.aircraftwar.logic.bullet.EnemyBullet
import edu.hitsz.aircraftwar.logic.bullet.HeroBullet
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


object Utils {
  fun getBullet(
    x: Int,
    y: Int,
    speedX: Int,
    speedY: Int,
    power: Int,
    bulletType: Int
  ): BaseBullet? {
    var bullet: BaseBullet? = null
    when (bulletType) {
      0 -> bullet = HeroBullet(x, y, speedX, speedY, power) // 发射一颗子弹
      1 -> bullet = EnemyBullet(x, y, speedX, speedY, power) // 发射一颗子弹
    }
    return bullet
  }

  @RequiresApi(Build.VERSION_CODES.O)
  fun getCurrentFormatTime(): String {
    val beijingTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
    val customFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formatted = beijingTime.format(customFormatter)
    return formatted
  }
}