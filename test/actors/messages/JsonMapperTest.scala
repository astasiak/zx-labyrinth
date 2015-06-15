package actors

import org.scalatest.FunSuite
import actors.messages.JsonMapper
import play.api.libs.json.Json
import game._
import actors.messages._

class JsonMapperTest extends FunSuite {
  test("mapping moves") {
    val expectedMap = List("n"->North,"s"->South,"e"->East,"w"->West)
    for((key,dir) <- expectedMap) {
      val message = JsonMapper.mapJsToMsg(Json.obj("type"->"move","dir"->key))
      assert(message === MakeMoveIMsg(dir), "Direction wrongly mapped")
    }
  }
  
  test("mapping board") {
    // +-+-+-+
    // |  S  |
    // + +-+ +
    // |    M|
    // + + +-+
    // |   | |
    // +-+-+-+
    val board = Board.create((3,3),(0,1),(1,2),List(ProtoBorder(2,1,true),ProtoBorder(0,1,false),ProtoBorder(1,2,false)))
    val boardJson = Json.obj(
        "type"->"init",
        "size"->Json.arr(3,3),
        "start"->Json.arr(0,1),
        "end"->Json.arr(1,2),
        "wallsH"->".-...-",
        "wallsV"->".....-")
    val message = JsonMapper.mapJsToMsg(boardJson)
    assert(message === InitBoardIMsg(board), "Board wrongly mapped")
  }
}