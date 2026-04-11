package edu.hitsz.aircraftwar.data

class SingleGameInfo {
  var userName: String? = null
  var score: Int = 0
  var date: String? = null
  constructor() {

  }
  constructor(userName: String?, score: Int, date: String?) {
    this.userName = userName
    this.score = score
    this.date = date
  }
}