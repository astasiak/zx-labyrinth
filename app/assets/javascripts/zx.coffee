mapMsgToBoard = (msg) ->
  board =
    start: [1, 1]
    meta: [1, 2]
    borders: []
  for i in [0..3]
    row = []
    for j in [0..3]
      row.push({h:false, v:false})
    board.borders.push(row)
  board.borders[1][1] = {h:true, v:true}
  board

mapBoardToMsg = (board) ->
  msg =
    size: [3, 3]
    start: [0, 0]
    end: [0, 0]
    wallsH: " -    "
    wallsV: " -    "
  msg

wsSend = (obj) ->
  message = JSON.stringify(obj)
  console.log("WS< "+message)
  window.gameWs.send(message)

createBoards = (params) ->
  window.boardA = new Board("#boardA", params)
  window.boardB = new Board("#boardB", params)
  window.boardA.setSubmit ->
    window.boardA.setEditable(false)
    board = mapBoardToMsg(window.boardA.getBoard())
    board.type = "init"
    wsSend(board)
  window.boardB.setSubmit ->
    window.boardB.setEditable(false)
    board = mapBoardToMsg(window.boardB.getBoard())
    board.type = "init"
    wsSend(board)

wsHandler = (data) ->
  console.log("WS> "+data)
  data = JSON.parse(data)
  console.log(data)
  if data.type == "params"
    params =
      width: data.x
      height: data.y
      borders: data.walls
    createBoards(params)
  else if data.type == "update_state"
    $("#state_msg").text(data.state)
  else if data.type == "update_players"
    $("#playerA").text(data.a)
    $("#playerB").text(data.b)
  else if data.type == "sit_ok"
    if data.player == "A"
      window.boardA.setEditable(true)
      $("#containerA").addClass("my")
    else # "B"
      window.boardB.setEditable(true)
      $("#containerB").addClass("my")
  else if data.type == "sit_fail"
    alert("Nie można usiąść")
  else if data.type == "update_board"
    board = if data.player=="A" then window.boardA else window.boardB
    board.setBoard(mapMsgToBoard(data.board))

$ ->
  page = $("body").data("page-id")
  if page=="index"
    $("#join_form_button").click ->
      window.location.href = $("body").data("game-url")+$("#join_form_game_id").val()
  else if page=="game"
    wsUrl = $("body").data("ws-url")
    if location.protocol=="https:"
      wsUrl = wsUrl.replace("ws:","wss:")
    $("#messages").text(wsUrl)
    window.gameWs = new WebSocket(wsUrl)
    window.gameWs.onmessage = (msg) ->
      wsHandler(msg.data)
    window.gameWs.onclose = () ->
      console.log("Websocket closed!");
    $("#sit_button").click ->
      wsSend({"type":"sit","name":$("#name_input").val()})