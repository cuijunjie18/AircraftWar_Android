package edu.hitsz.aircraftwar.controller

import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.logic.aircraft.AbstractAircraft
import edu.hitsz.aircraftwar.logic.aircraft.BossEnemy
import edu.hitsz.aircraftwar.logic.aircraft.EliteEnemy
import edu.hitsz.aircraftwar.logic.aircraft.MobEnemy
import edu.hitsz.aircraftwar.logic.aircraft.SuperEliteEnemy
import edu.hitsz.aircraftwar.logic.factory.BossEnemyFactory
import edu.hitsz.aircraftwar.logic.factory.EliteEnemyFactory
import edu.hitsz.aircraftwar.logic.factory.MobEnemyFactory
import edu.hitsz.aircraftwar.logic.factory.PropBloodFactory
import edu.hitsz.aircraftwar.logic.factory.PropBombFactory
import edu.hitsz.aircraftwar.logic.factory.PropBulletFactory
import edu.hitsz.aircraftwar.logic.factory.PropBulletPlusFactory
import edu.hitsz.aircraftwar.logic.factory.SuperEliteEnemyFactory
import edu.hitsz.aircraftwar.model.config.GameConfig
import edu.hitsz.aircraftwar.model.entity.EntityManager
import edu.hitsz.aircraftwar.model.state.GameState
import edu.hitsz.aircraftwar.setting.Music.MusicManager
import java.util.Random

/**
 * 生成控制器 - Controller层
 * 负责敌机生成、道具生成和射击调度
 */
class SpawnController(
    private val gameConfig: GameConfig,
    private val entityManager: EntityManager,
    private val gameState: GameState
) {
    // 工厂实例
    private val mobEnemyFactory = MobEnemyFactory()
    private val eliteEnemyFactory = EliteEnemyFactory()
    private val superEliteEnemyFactory = SuperEliteEnemyFactory()
    private val bossEnemyFactory = BossEnemyFactory()
    private val propBloodFactory = PropBloodFactory()
    private val propBombFactory = PropBombFactory()
    private val propBulletFactory = PropBulletFactory()
    private val propBulletPlusFactory = PropBulletPlusFactory()

    private val random = Random()

    /**
     * 生成敌机
     */
    fun spawnEnemies() {
        if (entityManager.enemyAircrafts.size < gameConfig.enemyMaxNumber) {
            val randomNum = random.nextInt(gameConfig.enemyRate)

            // 根据随机数生成普通、精英敌机、超级精英敌机
            val enemy: AbstractAircraft? = when {
                randomNum == 0 -> superEliteEnemyFactory.createEnemy()
                randomNum == 1 || randomNum == 2 -> eliteEnemyFactory.createEnemy()
                else -> mobEnemyFactory.createEnemy()
            }

            enemy?.let {
                entityManager.enemyAircrafts.add(it)
            }
        }

        // 每300分产生一个Boss, 且场上只能有一个Boss(仅普通模式、困难模式)
        if (gameState.shouldSpawnBoss(gameConfig.difficultyStr)) {
            MusicManager.switchBossBgm()
            bossEnemyFactory.createEnemy()?.let {
                entityManager.enemyAircrafts.add(it)
            }
            gameState.markBossSpawned()
        }

        // 观察者注册最新添加的敌机
        if (entityManager.enemyAircrafts.isNotEmpty()) {
            entityManager.bombSubject.registerObserver(
                entityManager.enemyAircrafts[entityManager.enemyAircrafts.size - 1]
            )
        }
    }

    /**
     * 所有飞行器发射子弹
     */
    fun shootAll() {
        // 敌机射击
        for (enemy in entityManager.enemyAircrafts) {
            if (enemy is MobEnemy) continue  // 普通敌机不射击

            val bullets = enemy.shoot()
            if (bullets != null) {
                val validBullets = bullets.filterNotNull()
                entityManager.enemyBullets.addAll(validBullets)
                for (bullet in validBullets) {
                    entityManager.bombSubject.registerObserver(bullet)
                }
            }
        }

        // 英雄射击
        entityManager.heroBullets.addAll(
            entityManager.heroAircraft.shoot().filterNotNull()
        )

        // 检查火力道具时间
        entityManager.heroAircraft.checkShootModeDuration()
    }

    /**
     * 在指定位置生成道具
     */
    fun generateProp(x: Int, y: Int) {
        val randomNum = random.nextInt(gameConfig.propRate)
        when {
            randomNum in 0..<3 -> { // 30% 生成血量道具
                propBloodFactory.createProp(x, y)?.let { entityManager.props.add(it) }
            }
            randomNum in 3..<6 -> { // 30% 生成炸弹道具
                propBombFactory.createProp(x, y)?.let { entityManager.props.add(it) }
            }
            randomNum in 6..<9 -> { // 30% 生成子弹道具(两种)
                val bulletType = random.nextInt(2)
                if (bulletType == 0) {
                    propBulletFactory.createProp(x, y)?.let { entityManager.props.add(it) }
                } else {
                    propBulletPlusFactory.createProp(x, y)?.let { entityManager.props.add(it) }
                }
            }
            // else: 10% 不生成道具
        }
    }

    /**
     * 根据敌机类型在其位置生成道具
     */
    fun generatePropsForDestroyedEnemy(enemy: AbstractAircraft) {
        val propX = enemy.getLocationXVal()
        val propY = enemy.getLocationYVal()

        when (enemy) {
            is MobEnemy -> {
                // 普通敌机不掉落道具
            }
            is BossEnemy -> {
                // Boss掉落 <= 3个道具
                generateProp(propX, propY)
                generateProp((propX + 100) % AircraftWarApplication.SCREEN_WIDTH, propY + 20)
                generateProp((propX + 200) % AircraftWarApplication.SCREEN_WIDTH, propY + 50)
                gameState.markBossKilled()
            }
            is EliteEnemy -> {
                generateProp(propX, propY)
            }
            is SuperEliteEnemy -> {
                generateProp(propX, propY)
            }
        }
    }
}
