# TODO unify numeration (mapMsgToBoard,mapBoardToMsg,game.board...)
mapMsgToBoard = (msg) ->
  board =
    start: msg.start
    meta: msg.end
    position: msg.pos
    borders: []
  x = msg.size[0]
  y = msg.size[1]
  for j in [0..y-1]
    row = []
    for i in [0..x-1]
      hh = if j==0 then false else msg.wallsH[(j-1)*x+i] in ['-','=']
      vv = if i==0 then false else msg.wallsV[j*(x-1)+i-1] in ['-','=']
      row.push({h:hh, v:vv})
    board.borders.push(row)
  board

mapBoardToMsg = (board, params) ->
  if not board.start or not board.meta
    return undefined
  wallsH = ""
  for j in [1..params.height-1]
    for i in [0..params.width-1]
      symbol = if board.borders[j][i].h then '-' else ' '
      wallsH = wallsH + symbol
  wallsV = ""
  for j in [0..params.height-1]
    for i in [1..params.width-1]
      symbol = if board.borders[j][i].v then '-' else ' '
      wallsV = wallsV + symbol
  msg =
    size: [params.width,params.height]
    start: parseInt(i) for i in board.start
    end: parseInt(i) for i in board.meta
    wallsH: wallsH
    wallsV: wallsV
  msg

initMoveButtons = () ->
  $("#move_up").click ->
    wsSend({"type":"move","dir":"n"})
  $("#move_down").click ->
    wsSend({"type":"move","dir":"s"})
  $("#move_left").click ->
    wsSend({"type":"move","dir":"w"})
  $("#move_right").click ->
    wsSend({"type":"move","dir":"e"})

onBoardSubmit = (params,boardRef) ->
  boardRef.setSubmit ->
    board = mapBoardToMsg(boardRef.getBoard(),params)
    if not board
      alert("Umieść start i metę")
      return
    board.type = "init"
    boardRef.setEditable(false)
    wsSend(board)

createBoards = (params) ->
  window.boardA = new Board("#boardA", params)
  window.boardB = new Board("#boardB", params)
  onBoardSubmit(params,window.boardA)
  onBoardSubmit(params,window.boardB)

bindArrowKeys = ->
  $(document).keydown (e) ->
    if $("#chatInput").is(":focus")
      return
    if e.keyCode == 37
      dir = "w"
    else if e.keyCode == 38
      dir = "n"
    else if e.keyCode == 39
      dir = "e"
    else if e.keyCode == 40
      dir = "s"
    else
      return
    wsSend({"type":"move","dir":dir})

makeBlinking = (selector) ->
  setInterval ->
    $(selector).animate({backgroundColor:"#a00"},400).animate({backgroundColor:"#f66"},400)
  , 400

printMessageAboutWinner = (winnerId) ->
  if winnerId==window.myPlayerId
    addChatTechnicalMessage("Wygrałeś!")
  else
    addChatTechnicalMessage("Przegrałeś")

translateStatus = (status) ->
  stateString = "Nieznany stan gry"
  playAnimation = true
  if status=="Awaiting"
    stateString = "Oczekiwanie na rozpoczęcie gry"
  else if status=="Ongoing(PlayerA)"
    stateString = "Ruch wykonuje "+$("#playerA").text()
    $("#containerB").addClass("current")
    $("#containerA").removeClass("current")
  else if status=="Ongoing(PlayerB)"
    stateString = "Ruch wykonuje "+$("#playerB").text()
    $("#containerA").addClass("current")
    $("#containerB").removeClass("current")
  else if status=="Finished(PlayerA)"
    stateString = "Grę wygrał "+$("#playerA").text()
    $("#containerA").addClass("winner")
    makeBlinking("#containerA")
    printMessageAboutWinner("A")
  else if status=="Finished(PlayerB)"
    stateString = "Grę wygrał "+$("#playerB").text()
    $("#containerB").addClass("winner")
    makeBlinking("#containerB")
    printMessageAboutWinner("B")
  else if status=="INIT_PSEUDOSTATE"
    stateString = "Oczekiwanie na rozpoczęcie gry"
    playAnimation = false
  if $("#gameState").text()==stateString
    playAnimation = false 
  $("#gameState").text(stateString)
  if playAnimation
    for color in ["#ff0","#00f","#000"]
      $("#gameState").animate({backgroundColor:color},200)

addNewMessage = (player,text) ->
  newRow = '<div class="chatRow"><span class="messageSender">'
  newRow += player
  newRow += '</span><span class="messageText">'
  newRow += text
  newRow += '</span></div>'
  chatHistory = $("#chatHistory")
  chatHistory.append(newRow)
  chatHistory.scrollTop(chatHistory.height())

addChatTechnicalMessage = (text) ->
  newRow = '<div class="technical">'+text+'</div>'
  chatHistory = $("#chatHistory")
  chatHistory.append(newRow)
  chatHistory.scrollTop(chatHistory.height())

askForName = ->
  name = ""
  while not name
    name = prompt("Podaj imię gracza")
  wsSend({"type":"sit","name":name})

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
    translateStatus(data.state)
  else if data.type == "update_players"
    $("#playerA").text(data.a)
    $("#playerB").text(data.b)
  else if data.type == "sit_ok"
    if data.player == "A"
      window.myBoard = boardA
      window.myPlayerId = "A"
      $("#containerA").addClass("my")
    else # "B"
      window.myBoard = boardB
      window.myPlayerId = "B"
      $("#containerB").addClass("my")
    window.myBoard.setEditable(true)
  else if data.type == "sit_fail"
    alert("Nie można usiąść")
  else if data.type == "update_board"
    board = if data.player=="A" then window.boardA else window.boardB
    board.setBoard(mapMsgToBoard(data.board))
  else if data.type == "chat"
    addNewMessage(data.player,data.msg)
  else if data.type == "init_result"
    if not data.ok
      alert("Niepoprawne rozmieszczenie labiryntu")
      window.myBoard.setEditable(true)

wsSend = (obj) ->
  message = JSON.stringify(obj)
  console.log("WS< "+message)
  window.gameWs.send(message)

$ ->
  wsUrl = $("body").data("ws-url")
  if location.protocol=="https:"
    wsUrl = wsUrl.replace("ws:","wss:")
  window.gameWs = new WebSocket(wsUrl)
  window.gameWs.onmessage = (msg) ->
    wsHandler(msg.data)
  window.gameWs.onclose = () ->
    console.log("Websocket closed!");
  initMoveButtons()
  translateStatus("INIT_PSEUDOSTATE")
  onChatKeydown = (e) ->
    if e.which==13
      wsSend({"type":"chat","msg":$("#chatInput").val()})
      $("#chatInput").val("")
  $("#chatInput").bind("keydown",onChatKeydown)
  bindArrowKeys()
  askForName()

