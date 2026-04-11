package edu.hitsz.aircraftwar.logic.prop

class PropBullet(locationX: Int, locationY: Int, speedX: Int, speedY: Int) :
  BaseProp(locationX, locationY, speedX, speedY) {
  public override fun action() {
    println("FireSupply active!")
  }
}