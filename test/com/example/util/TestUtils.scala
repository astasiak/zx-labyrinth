package com.example.util

import com.example.game._

object TestUtils {
  class BoardBuilder(history: List[Coord2D]) {
    def mkBoard(string: String) = {
      val rows = string.split("\n")
      val rowLens = rows.map(_.length).toSet
      if(rowLens.size!=1) {
        throw new RuntimeException("Bad board format!")
      }
      val (height0, width0) = (rows.length, rowLens.head)
      if(height0%2!=1 || width0%2!=1) {
        throw new RuntimeException("Bad board format!")
      }
      val height = (height0+1)/2
      val width = (width0+1)/2
      var pos, start, meta: Coord2D = Coord2D(-1,-1)
      for(i<-(0 until width))
        for(j<-(0 until height)) {
          val fieldSymbol = rows(2*j)(2*i)
          if(Set('S','M','!').contains(fieldSymbol)) pos = Coord2D(j,i)
          if(Set('S','s').contains(fieldSymbol)) start = Coord2D(j,i)
          if(Set('M','m').contains(fieldSymbol)) meta = Coord2D(j,i)
        }
      def everySecond[A](l:Seq[A], even:Boolean) = l.zipWithIndex.collect {case (e,i) if (i % 2) == (if(even) 0 else 1) => e}
      def mapEdge(char: Char): Border = 
        if(char=='='||char=='I') Border(true, true)
        else if(char=='-'||char=='|') Border(true, false)
        else if(char==' ') Border(false, false)
        else if(char=='.') Border(false, true)
        else throw new RuntimeException("Bad board format at char: "+char)
      val horizontals = everySecond(rows,false).map(everySecond(_,true).map(mapEdge _).toVector).toVector
      val verticals = everySecond(rows,true).map(everySecond(_,false).map(mapEdge _).toVector).toVector
      Board(Coord2D(height,width),pos,start,meta,Borders(verticals,horizontals),history)
    }
  }
  def withHistory(list: List[Coord2D]) = new BoardBuilder(list)
  def mkBoard(string: String) = withHistory(List()).mkBoard(string)
}