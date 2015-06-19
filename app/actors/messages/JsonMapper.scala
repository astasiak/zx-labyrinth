package actors.messages

import play.api.libs.json._
import game._
import play.api.libs.json.Json.JsValueWrapper

object JsonMapper {
  def mapJsToMsg(js: JsValue): InboudMessage = (js \ "type").asOpt[String] match {
    case Some("sit") => mapSubMsg(js)
    case Some("chat") => mapChatMsg(js)
    case Some("init") => mapInitMsg(js)
    case Some("move") => mapMoveMsg(js)
    case Some("ask") => mapAskMsg(js)
    case Some("keep_alive") => KeepAliveIMsg()
    case _ => UnknownIMsg()
  }
  private def mapSubMsg(js: JsValue) = (js \ "name").asOpt[String] match {
    case None => ErrorIMsg("Cannot parse 'sit' message - no 'name'")
    case Some(name) => SubscriptionIMsg(name)
  }
  private def mapChatMsg(js: JsValue) = (js \ "msg").asOpt[String] match {
    case None => ErrorIMsg("Cannot parse 'chat' message - no 'msg'")
    case Some(msg) => ChatMessageIMsg(msg)
  }
  private def mapInitMsg(js: JsValue) = {
    val size = mapPair(js \ "size")
    val start = mapPair(js \ "start")
    val end = mapPair(js \ "end")
    val wallsH = (js \ "wallsH").asOpt[String]
    val wallsV = (js \ "wallsV").asOpt[String]
    (size, start, end, wallsH, wallsV) match {
      case (Some((width,height)), Some(start), Some(end), Some(wallsH), Some(wallsV)) => {
        val wallsList = (mapBorders(wallsH,false,width)++mapBorders(wallsV,true,width-1)).toList
        val board = Board.create((height,width),start.swap,end.swap,wallsList)
        println(board.toFancyString)
        InitBoardIMsg(board)
      }
      case _ => ErrorIMsg("Cannot parse 'init' message")
    }
  }
  private def mapBorders(string: String, vertical: Boolean, rowLength: Int) = string.grouped(rowLength).zipWithIndex.flatMap {
    case(w,r) => w.zipWithIndex.flatMap {
      case ('-',c) => Some(ProtoBorder(r,c,vertical))
      case _ => None
    }
  }
  private def mapPair(js: JsValue): Option[(Int, Int)] = js match {
    case JsArray(Seq(JsNumber(a),JsNumber(b))) => Some((a.toInt,b.toInt)) // FIXME > MAX_INT
    case _ => None
  }
  private def mapMoveMsg(js: JsValue) = (js \ "dir").asOpt[String] match {
    case None => ErrorIMsg("Cannot parse 'chat' message - no 'dir'")
    case Some("n") => MakeMoveIMsg(North)
    case Some("s") => MakeMoveIMsg(South)
    case Some("e") => MakeMoveIMsg(East)
    case Some("w") => MakeMoveIMsg(West)
    case Some(other) => ErrorIMsg("Cannot parse 'chat' message - bad value '%s'".format(other))
  }
  private def mapAskMsg(js: JsValue) = AskForParamsIMsg()
  
  def mapMsgToJs(msg: OutboundMessage): JsValue = msg match {
    case TechnicalMessageOMsg(msg) => Json.obj("type"->"technical","msg"->msg)
    case ErrorOMsg(msg) => Json.obj("type"->"error","msg"->msg)
    case ChatMessageOMsg(player, msg) => Json.obj("type"->"chat", "player"->player, "msg"->msg)
    case SitDownSuccessOMsg(playerId) => Json.obj("type"->"sit_ok","player"->mapPlayerId(playerId))
    case SitDownFailOMsg() => Json.obj("type"->"sit_fail")
    case UpdateBoardOMsg(playerId, board) => Json.obj("type"->"update_board","player"->mapPlayerId(playerId),"board"->mapBoard(board))
    case UpdatePlayersOMsg(playerA, playerB) => Json.obj("type"->"update_players","a"->mapPlayer(playerA),"b"->mapPlayer(playerB))
    case UpdateStateOMsg(gameState) => Json.obj("type"->"update_state","state"->mapState(gameState))
    case ParamsOMsg(GameParams((y,x),walls)) => Json.obj("type"->"params","x"->x,"y"->y,"walls"->walls)
    case InitBoardResultOMsg(success) => Json.obj("type"->"init_result","ok"->success)
    case other => Json.obj("type"->"unknown")
  }
  private def mapPlayerId(playerId: PlayerId) = if(playerId==PlayerA) "A" else "B"
  private def mapBoard(board: Board) = {
    implicit def mapPairToJsArr(a: (Int,Int)): JsValueWrapper = Json.arr(a._1, a._2)
    Json.obj("size"->board.size.swap, "start"->board.start.swap, "end"->board.meta.swap, "pos"->board.position.swap,
        "wallsH"->mapBorders(board.borders.horizontal), "wallsV"->mapBorders(board.borders.vertical))
  }
  private def mapBorders(borders: Vector[Vector[Border]]) = borders.flatten.map(_ match {
    case Border(false, false) => " "
    case Border(false, true) => "."
    case Border(true, false) => "-"
    case Border(true, true) => "="
  }).mkString
  private def mapPlayer(player: Option[String]) = player.map(JsString(_)).getOrElse(JsNull)
  private def mapState(gameState: GameState) = gameState.toString
}