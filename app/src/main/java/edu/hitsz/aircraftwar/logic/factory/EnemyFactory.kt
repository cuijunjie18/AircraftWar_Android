package edu.hitsz.aircraftwar.logic.factory

import edu.hitsz.aircraftwar.logic.aircraft.AbstractAircraft


interface EnemyFactory {
  fun createEnemy(): AbstractAircraft?
}