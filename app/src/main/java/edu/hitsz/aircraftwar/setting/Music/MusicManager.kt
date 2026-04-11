package edu.hitsz.aircraftwar.setting.Music

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.R
import edu.hitsz.aircraftwar.setting.Setting

object MusicManager {

  // ---------- BGM 相关 ----------
  private var bgmMediaPlayer: MediaPlayer? = null          // 当前正在播放的 BGM

  // ---------- 音效相关 ----------
  private lateinit var soundPool: SoundPool
  private val soundMap = mutableMapOf<SoundType, Int>()    // 存储加载后的音效 ID

  enum class SoundType {
    BULLET_SHOOT,    // 子弹发射
    BULLET_HIT,     // 子弹击中
    BOMB_EXPLOSION,  // 炸弹爆炸
    GET_PROP,       // 获取道具
    GAME_OVER,       // 游戏结束
  }

  init {
    // 初始化 SoundPool（兼容 API 21+ 和旧版本）
    val audioAttributes = AudioAttributes.Builder()
      .setUsage(AudioAttributes.USAGE_GAME)
      .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
      .build()

    soundPool = SoundPool.Builder()
        .setMaxStreams(10)          // 最多同时播放 10 个音效
        .setAudioAttributes(audioAttributes)
        .build()

    // 预加载音效资源
    soundMap[SoundType.BOMB_EXPLOSION] = soundPool.load(AircraftWarApplication.context, R.raw.bomb_explosion, 1)
    soundMap[SoundType.BULLET_SHOOT] = soundPool.load(AircraftWarApplication.context, R.raw.bullet, 1)
    soundMap[SoundType.BULLET_HIT] = soundPool.load(AircraftWarApplication.context, R.raw.bullet_hit, 1)
    soundMap[SoundType.GET_PROP] = soundPool.load(AircraftWarApplication.context, R.raw.get_supply, 1)
    soundMap[SoundType.GAME_OVER] = soundPool.load(AircraftWarApplication.context, R.raw.game_over, 1)
  }

  fun switchNormalBgm() {
    if (!Setting.musicOpen) return
    playBgm(R.raw.bgm, loop = true)
  }

  fun switchBossBgm() {
    if (!Setting.musicOpen) return
    playBgm(R.raw.bgm_boss, loop = true)
  }

  /**
   * 播放指定的 BGM
   */
  private fun playBgm(resId: Int, loop: Boolean) {
    // 停止并释放当前 BGM
    stopBgm()

    try {
      bgmMediaPlayer = MediaPlayer.create(AircraftWarApplication.context, resId).apply {
        isLooping = loop
        setOnCompletionListener {
          // 播放完成时的回调（若 loop = false 时可能用到）
          if (!loop) {
            releaseBgmPlayer()
          }
        }
        start()
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * 停止当前 BGM 并释放 MediaPlayer
   */
  fun stopBgm() {
    bgmMediaPlayer?.let {
      if (it.isPlaying) {
        it.stop()
      }
      it.release()
    }
    bgmMediaPlayer = null
  }

  /**
   * 暂停当前 BGM（不释放资源，可恢复）
   */
  fun pauseBgm() {
    bgmMediaPlayer?.let {
      if (it.isPlaying) {
        it.pause()
      }
    }
  }

  /**
   * 恢复暂停的 BGM
   */
  fun resumeBgm() {
    bgmMediaPlayer?.let {
      if (!it.isPlaying) {
        it.start()
      }
    }
  }

  /**
   * 播放单个音效
   * @param type 音效类型
   * @param leftVolume 左声道音量 (0.0 ~ 1.0)，默认 1.0
   * @param rightVolume 右声道音量 (0.0 ~ 1.0)，默认 1.0
   * @param priority 优先级（仅旧版 SoundPool 有效，现无实际作用），默认 0
   * @param loop 是否循环，0 不循环，-1 无限循环，默认 0
   * @param rate 播放速率 (0.5 ~ 2.0)，默认 1.0
   */
  fun playSound(
    type: SoundType,
    leftVolume: Float = 1.0f,
    rightVolume: Float = 1.0f,
    priority: Int = 0,
    loop: Int = 0,
    rate: Float = 1.0f
  ) {
    if (!Setting.musicOpen) return
    val soundId = soundMap[type]
    if (soundId == null) {
      println("音效 $type 未加载，请先调用 setSoundResource() 设置资源 ID")
      return
    }
    soundPool.play(soundId, leftVolume, rightVolume, priority, loop, rate)
  }

  /**
   * 释放所有音频资源（在游戏退出或不需要音频时调用）
   */
  fun release() {
    stopBgm()
    soundPool.release()
    // 注意：release 后 SoundPool 不能再使用，如需重新使用需重新初始化 MusicManager
  }

  /**
   * 释放内部的 BGM 播放器（辅助方法）
   */
  private fun releaseBgmPlayer() {
    bgmMediaPlayer?.release()
    bgmMediaPlayer = null
  }
}