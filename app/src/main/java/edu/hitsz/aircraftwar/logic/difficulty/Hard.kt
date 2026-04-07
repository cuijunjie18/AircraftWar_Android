package edu.hitsz.aircraftwar.logic.difficulty

class Hard : Difficulty() {
  override fun improve_difficulty() {
    cycle_counter++;
    if (cycle_counter == improve_cycle){
      cycle_counter = 0;
      eliteProbability += 0.2;
      enemyCycle -= 0.3;
      enemyAbility += 0.08;
      System.out.printf("提高难度! 精英敌机概率:%f,敌机周期:%f,敌机熟悉提升倍率:%f\n",
        eliteProbability,enemyCycle,enemyAbility);
    }
  }
}