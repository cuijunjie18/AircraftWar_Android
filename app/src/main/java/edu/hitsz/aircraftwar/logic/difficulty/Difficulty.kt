package edu.hitsz.aircraftwar.logic.difficulty

abstract class Difficulty {
  var eliteProbability: Double = 0.0
  var enemyCycle: Double = 0.0
  var enemyAbility: Double = 0.0
  var improve_cycle: Int = 0
  var cycle_counter: Int = 0

  init {
    eliteProbability = 0.2
    enemyCycle = 20.0
    enemyAbility = 1.0
    improve_cycle = 0
    cycle_counter = 0
  }

  open fun improve_difficulty() {}
}