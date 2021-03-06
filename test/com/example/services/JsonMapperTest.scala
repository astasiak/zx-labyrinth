package com.example.services

import org.scalatest.FunSuite
import play.api.libs.json.Json
import com.example.game._
import com.example.services.messages._
import com.example.util.TestUtils

class JsonMapperTest extends FunSuite {

  test("mapping moves") {
    val expectedMap = List("n"->North,"s"->South,"e"->East,"w"->West)
    for((key,dir) <- expectedMap) {
      val message = JsonMapper.mapJsToMsg(Json.obj("type"->"move","dir"->key))
      assert(message === MakeMoveIMsg(dir), "Direction wrongly mapped")
    }
  }
  
  test("mapping board") {
    val initBoard = TestUtils
      .withHistory(List(Coord2D(0,1))).mkBoard(
        "  S  \n"+
        " +-+ \n"+
        "    m\n"+
        " + +-\n"+
        "   | ")
    val boardJson = Json.obj(
        "type"->"init",
        "size"->Json.arr(3,3),
        "start"->Json.arr(1,0),
        "end"->Json.arr(2,1),
        "wallsH"->".-...-",
        "wallsV"->".....-")
    val message = JsonMapper.mapJsToMsg(boardJson)
    assert(message === InitBoardIMsg(initBoard), "Board wrongly mapped")
  }
  
  test("mapping board back") {
    val board = TestUtils
      .withHistory(List(Coord2D(0,1),Coord2D(0,0),Coord2D(1,0))).mkBoard(
        "  s  \n"+
        " +-+ \n"+
        "!   m\n"+
        " + +-\n"+
        "   | ")
    val json = JsonMapper.mapMsgToJs(UpdateBoardOMsg(PlayerA,board))
    val expectedJson = Json.obj(
        "type"->"update_board",
        "player"->"A",
        "board"->Json.obj(
          "size"->Json.arr(3,3),
          "start"->Json.arr(1,0),
          "end"->Json.arr(2,1),
          "pos"->Json.arr(0,1),
          "wallsH"->" -   -",
          "wallsV"->"     -",
          "history"->Json.arr(Json.arr(1,0),Json.arr(0,0),Json.arr(0,1)))
        )
    assert(json === expectedJson, "Board wrongly mapped")
  }
}