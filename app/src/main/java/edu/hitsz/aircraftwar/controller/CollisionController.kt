package edu.hitsz.aircraftwar.controller

import edu.hitsz.aircraftwar.logic.aircraft.BossEnemy
import edu.hitsz.aircraftwar.logic.aircraft.EliteEnemy
import edu.hitsz.aircraftwar.logic.aircraft.MobEnemy
import edu.hitsz.aircraftwar.logic.aircraft.SuperEliteEnemy
import edu.hitsz.aircraftwar.logic.prop.BaseProp
import edu.hitsz.aircraftwar.logic.prop.PropBlood
import edu.hitsz.aircraftwar.logic.prop.PropBomb
import edu.hitsz.aircraftwar.logic.prop.PropBullet
import edu.hitsz.aircraftwar.logic.prop.PropBulletPlus
import edu.hitsz.aircraftwar.model.entity.EntityManager
import edu.hitsz.aircraftwar.model.state.GameState
import edu.hitsz.aircraftwar.setting.Music.MusicManager
import edu.hitsz.aircraftwar.setting.Setting

/**
 * 碰撞检测控制器 - Controller层
 * 处理所有碰撞检测逻辑
 */
class CollisionController(
    private val entityManager: EntityManager,
    private val gameState: GameState,
    private val spawnController: SpawnController
) {

    /**
     * 执行所有碰撞检测
     */
    fun checkAllCollisions() {
        checkEnemyBulletsHitHero()
        checkHeroBulletsHitEnemy()
        checkEnemyAircraftCrashHero()
        checkHeroPickupProps()
    }

    /**
     * 敌机子弹攻击英雄
     */
    private fun checkEnemyBulletsHitHero() {
        for (bullet in entityManager.enemyBullets) {
            if (bullet.notValid()) continue
            if (entityManager.heroAircraft.crash(bullet)) {
                entityManager.heroAircraft.decreaseHp(bullet.power)
                bullet.vanish()
            }
        }
    }

    /**
     * 英雄子弹与敌机碰撞检测
     */
    private fun checkHeroBulletsHitEnemy() {
        for (bullet in entityManager.heroBullets) {
            if (bullet.notValid()) continue
            for (enemyAircraft in entityManager.enemyAircrafts) {
                if (enemyAircraft.notValid()) continue
                if (enemyAircraft.crash(bullet)) {
                    MusicManager.playSound(MusicManager.SoundType.BULLET_HIT)
                    enemyAircraft.decreaseHp(bullet.power)
                    bullet.vanish()
                    if (enemyAircraft.notValid()) {
                        // 敌机被击毁，生成道具
                        spawnController.generatePropsForDestroyedEnemy(enemyAircraft)
                    }
                }
            }
        }
    }

    /**
     * 英雄飞机与敌机碰撞检测
     */
    private fun checkEnemyAircraftCrashHero() {
        for (enemyAircraft in entityManager.enemyAircrafts) {
            if (enemyAircraft.notValid()) continue
            if (enemyAircraft.crash(entityManager.heroAircraft) ||
                entityManager.heroAircraft.crash(enemyAircraft)) {
                enemyAircraft.vanish()
                entityManager.heroAircraft.decreaseHp(entityManager.heroAircraft.hp)
            }
        }
    }

    /**
     * 英雄拾取道具
     */
    private fun checkHeroPickupProps() {
        for (prop in entityManager.props) {
            if (prop.notValid()) continue
            if (entityManager.heroAircraft.crash(prop)) {
                MusicManager.playSound(MusicManager.SoundType.GET_PROP)
                prop.vanish()
                applyPropEffect(prop)
            }
        }
    }

    /**
     * 应用道具效果
     */
    private fun applyPropEffect(prop: BaseProp) {
        when (prop) {
            is PropBlood -> {
                entityManager.heroAircraft.increaseHp(30)
            }
            is PropBullet -> {
                entityManager.heroAircraft.changeShootMode("SCATTER")
            }
            is PropBulletPlus -> {
                entityManager.heroAircraft.changeShootMode("WAVE")
            }
            is PropBomb -> {
                if (Setting.musicOpen) {
                    MusicManager.playSound(MusicManager.SoundType.BOMB_EXPLOSION)
                }
                prop.action()
                entityManager.bombSubject.notifyObservers()
            }
        }
    }
}
