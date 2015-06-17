package game

import scala.collection.mutable.MutableList

sealed trait Direction
case object North extends Direction
case object South extends Direction
case object West extends Direction
case object East extends Direction

case class MoveResult(newBoard: Board, success: Boolean)

case class Border(wall: Boolean, discovered: Boolean)
case class Borders(vertical: Vector[Vector[Border]], horizontal: Vector[Vector[Border]]) {
  def updatedBorders(set: Vector[Vector[Border]], x: Int, y: Int): Vector[Vector[Border]] = {
    val border = set(x)(y)
    val newBorder = Border(border.wall, true)
    return set.updated(x, set(x).updated(y, newBorder))
  }
  def discoverBorder(position: (Int, Int), dir: Direction) = (dir, position) match {
      case (North, (x, y)) => Borders(vertical, updatedBorders(horizontal,x-1,y))
      case (South, (x, y)) => Borders(vertical, updatedBorders(horizontal,x,y))
      case (West, (x, y)) => Borders(updatedBorders(vertical,x,y-1), horizontal)
      case (East, (x, y)) => Borders(updatedBorders(vertical,x,y), horizontal)
    }
}
case class Board(
    size: (Int, Int),
    position: (Int, Int),
    start: (Int, Int),
    meta: (Int, Int),
    borders: Borders) {
  
  def privatize = {
    def privatizeBorders(bordersSet: Vector[Vector[Border]]) = bordersSet.map(_.map(_ match {
      case Border(true,false) => Border(false,false)
      case otherBorder => otherBorder
    }))
    val newVBorders = privatizeBorders(borders.vertical)
    val newHBorders = privatizeBorders(borders.horizontal)
    Board(size,position,start,meta,Borders(newVBorders,newHBorders))
  }
  
  def makeMove(dir: Direction): MoveResult = {
    def move: (Int, Int) = (dir, position) match {
      case (North, (x, y)) => (x-1,y)
      case (South, (x, y)) => (x+1,y)
      case (West, (x, y)) => (x,y-1)
      case (East, (x, y)) => (x,y+1)
    }
    def inRange: Boolean = (dir, position) match {
      case (North, (x, y)) => x > 0
      case (South, (x, y)) => x+1 < size._1
      case (West, (x, y)) => y > 0
      case (East, (x, y)) => y+1 < size._2
    }
    def canMove: Boolean = (dir, position) match {
      case (North, (x, y)) => !borders.horizontal(x-1)(y).wall
      case (South, (x, y)) => !borders.horizontal(x)(y).wall
      case (West, (x, y)) => !borders.vertical(x)(y-1).wall
      case (East, (x, y)) => !borders.vertical(x)(y).wall
    }
    if(!inRange) MoveResult(this, false)
    else if(canMove) MoveResult(Board(size,move,start,meta,borders.discoverBorder(position, dir)), true)
    else MoveResult(Board(size,position,start,meta,borders.discoverBorder(position, dir)), false)
  }
  
  def isFinished: Boolean = position == meta
  def toFancyString = {
    val builder = new StringBuilder
    for(row <- 0 until size._1) {
      for(col <- 0 until size._2) {
        builder ++= "+"
        builder ++= (if (row==0 || borders.horizontal(row-1)(col).wall) "-" else " ")
      }
      builder ++= "+\n"
      for(col <- 0 until size._2) {
        builder ++= (if (col==0 || borders.vertical(row)(col-1).wall) "|" else " ")
        val thisField = (row,col)
        builder ++= (if(thisField==start && thisField==position) "S"
          else if(thisField==meta && thisField==position) "M"
          else if(thisField==start) "s"
          else if(thisField==meta) "m"
          else if(thisField==position) "!"
          else " ")
      }
      builder ++= "|\n"
    }
    for(col <- 0 until size._2) {
      builder ++= "+-"
    }
    builder ++= "+"
  }
}


case class ProtoBorder(y: Int, x: Int, vertical: Boolean)
object Board {
  def create(size: (Int, Int), start: (Int, Int), meta: (Int, Int), borders: List[ProtoBorder]): Board = {
    val newBorder = Border(false, false)
    val newWall = Border(true, false)
    val vBorders = Vector.fill(size._1){MutableList.fill(size._2-1){newBorder}}
    val hBorders = Vector.fill(size._1-1){MutableList.fill(size._2){newBorder}}
    for(border <- borders.filter(_.vertical)) {
      vBorders(border.y)(border.x) = newWall
    }
    for(border <- borders.filter(!_.vertical)) {
      hBorders(border.y)(border.x) = newWall
    }
    val vBordersVector = vBorders.map(_.toVector)
    val hBordersVector = hBorders.map(_.toVector)
    return Board(size, start, start, meta, Borders(vBordersVector, hBordersVector))
  }
}