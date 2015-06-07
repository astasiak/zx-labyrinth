package game

class Game {
  val playerA: PlayerData = new PlayerData()
  val playerB: PlayerData = new PlayerData()
}


class PlayerData {
  var board: Board = _
  var callbacks: Callbacks = _
  var name: String = _
}

trait Callbacks {
  
}