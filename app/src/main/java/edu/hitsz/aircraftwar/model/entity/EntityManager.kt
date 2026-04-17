package edu.hitsz.aircraftwar.model.entity

import edu.hitsz.aircraftwar.logic.aircraft.AbstractAircraft
import edu.hitsz.aircraftwar.logic.aircraft.HeroAircraft
import edu.hitsz.aircraftwar.logic.bullet.BaseBullet
import edu.hitsz.aircraftwar.logic.observer.BombSubject
import edu.hitsz.aircraftwar.logic.prop.BaseProp

/**
 * 游戏实体管理器 - Model层
 * 统一管理所有游戏飞行物实体的集合
 */
class EntityManager {

    lateinit var heroAircraft: HeroAircraft
    val enemyAircrafts: MutableList<AbstractAircraft> = mutableListOf()
    val heroBullets: MutableList<BaseBullet> = mutableListOf()
    val enemyBullets: MutableList<BaseBullet> = mutableListOf()
    val props: MutableList<BaseProp> = mutableListOf()
    val bombSubject: BombSubject = BombSubject()

    /**
     * 初始化英雄飞机
     */
    fun initHero(x: Int, y: Int) {
        heroAircraft = HeroAircraft(x, y, 0, 0, 100)
        heroAircraft.setShootStrategy("NORMAL")
    }

    /**
     * 检查英雄飞机是否已初始化
     */
    fun isHeroInitialized(): Boolean {
        return ::heroAircraft.isInitialized
    }

    /**
     * 移动所有实体
     */
    fun moveAllEntities() {
        enemyAircrafts.forEach { it.forward() }
        heroBullets.forEach { it.forward() }
        enemyBullets.forEach { it.forward() }
        props.forEach { it.forward() }
    }

    /**
     * 移除所有无效实体，并返回被移除的敌机列表（用于计分）
     */
    fun removeInvalidEntities(): List<AbstractAircraft> {
        // 收集无效敌机
        val removedAircrafts = enemyAircrafts.filter { it.notValid() }
        removedAircrafts.forEach { bombSubject.removeObserver(it) }

        // 收集无效敌机子弹
        val removedBullets = enemyBullets.filter { it.notValid() }
        removedBullets.forEach { bombSubject.removeObserver(it) }

        // 移除所有无效实体
        enemyAircrafts.removeAll { it.notValid() }
        heroBullets.removeAll { it.notValid() }
        enemyBullets.removeAll { it.notValid() }
        props.removeAll { it.notValid() }

        return removedAircrafts
    }

    /**
     * 清空所有实体
     */
    fun clearAll() {
        enemyAircrafts.clear()
        heroBullets.clear()
        enemyBullets.clear()
        props.clear()
    }
}
