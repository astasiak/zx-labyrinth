package com.example.game

import scala.collection.mutable.MutableList
import scala.collection.mutable.Set


sealed trait Direction
case object North extends Direction
case object South extends Direction
case object West extends Direction
case object East extends Direction

case class MoveResult(newBoard: Board, success: Boolean)

case class Coord2D(x: Int, y: Int)

/**
 * Board objects are immutable and represent the state of the player's labyrinth
 * from the given moment of the game, including size, start, meta, current position,
 * information about discovered and undiscovered borders and history of moves.
 */
case class Board(
    size: Coord2D,
    position: Coord2D,
    start: Coord2D,
    meta: Coord2D,
    borders: Borders,
    history: List[Coord2D]) {
  
  /** Creates copy of the board with the undiscovered borders hidden */
  def privatize = {
    def privatizeBorders(bordersSet: Vector[Vector[Border]]) = bordersSet.map(_.map(_ match {
      case Border(true,false) => Border(false,false)
      case otherBorder => otherBorder
    }))
    val newVBorders = privatizeBorders(borders.vertical)
    val newHBorders = privatizeBorders(borders.horizontal)
    this.copy(borders=Borders(newVBorders,newHBorders))
  }
  
  /** Verifies if the given board represents valid labyrinth */
  def isValid: Boolean = {
    def fieldWithinSize(field: Coord2D) =
      Range(0,size.x).contains(field.x) &&
      Range(0,size.y).contains(field.y)
    def isMetaReachableFromStart = {
      def neighbours(pos:Coord2D) = {
        val directions = Set(North, East, South, West)
        directions
          .filter(d=>inRange(pos,d)&&canMove(pos,d))
          .map(move(pos,_))
      }
      var visitedFields: Set[Coord2D] = Set()
      var newFields = Set(start)
      while(!newFields.isEmpty) {
        visitedFields ++= newFields
        newFields = newFields.flatMap(neighbours(_)).filter(!visitedFields.contains(_))
      }
      visitedFields.contains(meta)
    }
    if(!fieldWithinSize(position)) false
    if(!fieldWithinSize(start)) false
    if(!fieldWithinSize(meta)) return false
    if(meta==start) return false
    if(!isMetaReachableFromStart) return false
    true
  }
  
  /** Creates copy of the board with state after the attempt of the move in the given direction */
  def makeMove(dir: Direction): MoveResult = {
    if(!inRange(position,dir)) return MoveResult(this, false)
    val discoveredBoard = this.copy(borders=borders.discoverBorder(position, dir))
    if(canMove(position,dir)) {
      val newPosition = move(position,dir)
      return MoveResult(discoveredBoard.copy(position=newPosition,history=history:+newPosition), true)
    }
    else return MoveResult(discoveredBoard, false)
  }
  
  /** Returns total number of borders used in the board */
  def numberOfBorders: Int = 0
  
  /** Checks if the board represent successfully finished labyrinth */
  def isFinished: Boolean = position == meta
  
  /** Returns String containing quite pretty ASCII representation of the board */
  def toFancyString = {
    val builder = new StringBuilder
    for(row <- 0 until size.x) {
      for(col <- 0 until size.y) {
        builder ++= "+"
        builder ++= (if (row==0 || borders.horizontal(row-1)(col).wall) "-" else " ")
      }
      builder ++= "+\n"
      for(col <- 0 until size.y) {
        builder ++= (if (col==0 || borders.vertical(row)(col-1).wall) "|" else " ")
        val thisField = Coord2D(row,col)
        builder ++= (if(thisField==start && thisField==position) "S"
          else if(thisField==meta && thisField==position) "M"
          else if(thisField==start) "s"
          else if(thisField==meta) "m"
          else if(thisField==position) "!"
          else " ")
      }
      builder ++= "|\n"
    }
    for(col <- 0 until size.y) {
      builder ++= "+-"
    }
    builder ++= "+"
    builder.toString
  }
  
  // checks if the move from given position would not exceed the range of the board
  private def inRange(position: Coord2D, dir: Direction): Boolean = (dir, position) match {
    case (North, Coord2D(x, y)) => x > 0
    case (South, Coord2D(x, y)) => x+1 < size.x
    case (West, Coord2D(x, y)) => y > 0
    case (East, Coord2D(x, y)) => y+1 < size.y
  }
  // checks if the move does not cross any border
  private def canMove(position: Coord2D, dir: Direction): Boolean = (dir, position) match {
    case (North, Coord2D(x, y)) => !borders.horizontal(x-1)(y).wall
    case (South, Coord2D(x, y)) => !borders.horizontal(x)(y).wall
    case (West, Coord2D(x, y)) => !borders.vertical(x)(y-1).wall
    case (East, Coord2D(x, y)) => !borders.vertical(x)(y).wall
  }
  // for given position returns new one, after the move in the given direction
  private def move(position: Coord2D, dir: Direction): Coord2D = (dir, position) match {
    case (North, Coord2D(x, y)) => Coord2D(x-1,y)
    case (South, Coord2D(x, y)) => Coord2D(x+1,y)
    case (West, Coord2D(x, y)) => Coord2D(x,y-1)
    case (East, Coord2D(x, y)) => Coord2D(x,y+1)
  }
}
object Board {
  /**
   * Alternative (to Board parameters) way to construct a board in new initial state.
   */
  def create(size: Coord2D, start: Coord2D, meta: Coord2D, borders: List[ProtoBorder]): Board = {
    val newBorder = Border(false, false)
    val newWall = Border(true, false)
    val vBorders = Vector.fill(size.x){MutableList.fill(size.y-1){newBorder}}
    val hBorders = Vector.fill(size.x-1){MutableList.fill(size.y){newBorder}}
    for(border <- borders.filter(_.vertical)) {
      vBorders(border.y)(border.x) = newWall
    }
    for(border <- borders.filter(!_.vertical)) {
      hBorders(border.y)(border.x) = newWall
    }
    val vBordersVector = vBorders.map(_.toVector)
    val hBordersVector = hBorders.map(_.toVector)
    return Board(size, start, start, meta, Borders(vBordersVector, hBordersVector),List(start))
  }
}
case class ProtoBorder(y: Int, x: Int, vertical: Boolean)

/** Basic data about single border - if it is wall on it and if the status is discovered */
case class Border(wall: Boolean, discovered: Boolean)
/** Container for statuses of all borders in the board */
case class Borders(vertical: Vector[Vector[Border]], horizontal: Vector[Vector[Border]]) {
  private def updatedBorders(set: Vector[Vector[Border]], x: Int, y: Int): Vector[Vector[Border]] = {
    val border = set(x)(y)
    val newBorder = Border(border.wall, true)
    return set.updated(x, set(x).updated(y, newBorder))
  }
  private[game] def discoverBorder(position: Coord2D, dir: Direction) = (dir, position) match {
      case (North, Coord2D(x, y)) => Borders(vertical, updatedBorders(horizontal,x-1,y))
      case (South, Coord2D(x, y)) => Borders(vertical, updatedBorders(horizontal,x,y))
      case (West, Coord2D(x, y)) => Borders(updatedBorders(vertical,x,y-1), horizontal)
      case (East, Coord2D(x, y)) => Borders(updatedBorders(vertical,x,y), horizontal)
    }
}