# TODO unify numeration (mapMsgToBoard,mapBoardToMsg,game.board...)
mapMsgToBoard = (msg) ->
  board =
    start: msg.start
    meta: msg.end
    position: msg.pos
    borders: []
    history: msg.history
  x = msg.size[0]
  y = msg.size[1]
  for j in [0..y-1]
    row = []
    for i in [0..x-1]
      hh = if j==0 then false else msg.wallsH[(j-1)*x+i] in ['-','=']
      vv = if i==0 then false else msg.wallsV[j*(x-1)+i-1] in ['-','=']
      hdis = if j==0 then false else msg.wallsH[(j-1)*x+i] in ['.','=']
      vdis = if i==0 then false else msg.wallsV[j*(x-1)+i-1] in ['.','=']
      row.push({h:hh, v:vv, hd:hdis, vd:vdis})
    board.borders.push(row)
  board

mapBoardToMsg = (board, params) ->
  if not board.start or not board.meta
    return undefined
  wallsH = ""
  if params.height>1
    for j in [1..params.height-1]
      for i in [0..params.width-1]
        symbol = if board.borders[j][i].h then '-' else ' '
        wallsH = wallsH + symbol
  wallsV = ""
  if params.width>1
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

initButtons = () ->
  $("#move_up").click ->
    wsSend({"type":"move","dir":"n"})
  $("#move_down").click ->
    wsSend({"type":"move","dir":"s"})
  $("#move_left").click ->
    wsSend({"type":"move","dir":"w"})
  $("#move_right").click ->
    wsSend({"type":"move","dir":"e"})
  $("#playAgainButton").click ->
    window.location.href += "/again"
  $("#joinButton").click ->
    wsSend({"type":"sit"})
    $(this).hide()

onBoardSubmit = (params,boardRef) ->
  boardRef.setSubmit ->
    board = mapBoardToMsg(boardRef.getBoard(),params)
    console.log(board)
    if not board
      alert(i18nGet('placeStartAndMeta'))
      return
    board.type = "init"
    boardRef.setEditable(false)
    wsSend(board)

createBoards = (params) ->
  if window.boardAlreadyCreated
    return
  window.boardAlreadyCreated = true
  window.boardA = new Board("#boardA", params, true)
  window.boardB = new Board("#boardB", params, true)
  onBoardSubmit(params,window.boardA)
  onBoardSubmit(params,window.boardB)

bindArrowKeys = ->
  $(document).keydown (e) ->
    if $("#chatInput").is(":focus")
      return
    if e.keyCode >= 37 and e.keyCode <= 40
      e.preventDefault()
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

makeBlinking = (element) ->
  element.addClass("winner")
  setInterval ->
    element.animate({backgroundColor:"#a00"},400).animate({backgroundColor:"#f66"},400)
  , 400

printMessageAboutWinner = (winnerId) ->
  $("#playAgainButton").fadeIn(400)
  makeBlinking($("#container"+winnerId))
  defaultMsg = "{{i18n['playerWon']}} "+$("#player"+winnerId).text()
  if window.myPlayerId
    i18nWinningId = if winnerId==window.myPlayerId then 'youWon' else 'youLost'
    addChatTechnicalMessage("{{i18n['"+i18nWinningId+"']}}")
    alert(i18nGet(i18nWinningId))
  else
    addChatTechnicalMessage(defaultMsg)
  return defaultMsg

updateTitle = () ->
  unreadNote = if window.unreadNote then "! " else ""
  yourTurnNote = if window.yourTurn then "* " else ""
  $("title").text(unreadNote+yourTurnNote+window.title)

showYourTurn = (isIt) ->
  window.yourTurn = isIt
  updateTitle()

dealWithFocus = ->
  window.isFocused = true
  window.title = $("title").text()
  $(window).focus ->
    window.isFocused = true
    window.unreadNote = false
    updateTitle()
  $(window).blur ->
    window.isFocused = false

translateStatus = (status) ->
  stateString = '{{i18n["unknownGameState"]}}'
  playAnimation = true
  showYourTurn(status == "Ongoing(Player"+window.myPlayerId+")")
  if status=="Awaiting"
    stateString = '{{i18n["awaitingState"]}}'
  else if status=="Ongoing(PlayerA)"
    stateString = "{{i18n['turnOfState']}} "+$("#playerA").text()
    $("#containerB").addClass("current")
    $("#containerA").removeClass("current")
  else if status=="Ongoing(PlayerB)"
    stateString = "{{i18n['turnOfState']}} "+$("#playerB").text()
    $("#containerA").addClass("current")
    $("#containerB").removeClass("current")
  else if status=="Finished(PlayerA)"
    stateString = printMessageAboutWinner("A")
  else if status=="Finished(PlayerB)"
    stateString = printMessageAboutWinner("B")
  else if status=="INIT_PSEUDOSTATE"
    stateString = '{{i18n["awaitingState"]}}'
    playAnimation = false
  if $("#gameState").text()==stateString
    playAnimation = false 
  i18nCreate '<span>'+stateString+'</span>'
  , (elem)->
    $("#gameState").html(elem)
  
  if playAnimation
    for color in ["#ff0","#00f","#000"]
      $("#gameState").animate({backgroundColor:color},200)

addToChat = (content) ->
  chatHistoryInner = $("#chatHistoryInner")
  chatHistoryInner.append(content)
  $("#chatHistory").scrollTop(chatHistoryInner.height())

addNewMessage = (player,text) ->
  newRow = '<div class="chatRow"><span class="messageSender">'
  newRow += player
  newRow += '</span><span class="messageText">'
  newRow += text
  newRow += '</span></div>'
  addToChat(newRow)

addChatTechnicalMessage = (text) ->
  i18nCreate('<div class="technical">'+text+'</div>', addToChat)

wsKeepAlive = ->
  setInterval ->
    wsSend({"type":"keep_alive"})
  , 20000

wsHandler = (data) ->
  console.log("WS> "+data)
  data = JSON.parse(data)
  if data.type == "params"
    params =
      width: data.x
      height: data.y
      borders: data.walls
    createBoards(params)
  else if data.type == "update_state"
    translateStatus(data.state)
  else if data.type == "update_players"
    setName = (selector, name) ->
      obj = $(selector)
      if name==null
        obj.removeClass("sitting")
        i18nCreate('<div>( {{i18n["nobody"]}} )</div>'
        , (msg)-> obj.html(msg))
      else
        obj.addClass("sitting")
        obj.text(name)
    setName("#playerA", data.a)
    setName("#playerB", data.b)
    if data.a and data.b
      $("#joinButton").hide()
  else if data.type == "sit_ok"
    $("#joinButton").hide()
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
    alert(i18nGet('cannotSitDown'))
  else if data.type == "update_board"
    board = if data.player=="A" then window.boardA else window.boardB
    board.setBoard(mapMsgToBoard(data.board))
  else if data.type == "chat"
    if not window.isFocused
      window.unreadNote = true
      updateTitle()
    addNewMessage(data.player,data.msg)
  else if data.type == "init_result"
    if not data.ok
      alert(i18nGet('incorrectLabyrinth'))
    window.myBoard.setEditable(not data.ok)
  else if data.type == "presence"
    playerName = data.user
    if data.present
      addChatTechnicalMessage(playerName+' {{i18n["playerEnteredRoom"]}}')
    else
      addChatTechnicalMessage(playerName+' {{i18n["playerLeftRoom"]}}')
  else if data.type == "rankings"
    for ranking in data.list
      addChatTechnicalMessage('{{i18n["rankingChange"]}} '+ranking.who+': '+ranking.diff)
    

wsSend = (obj) ->
  message = JSON.stringify(obj)
  console.log("WS< "+message)
  window.gameWs.send(message)

handleWholeGame =  ->
  dealWithFocus()
  wsUrl = $("#labyrinthGame").data("ws-url")
  if location.protocol=="https:"
    wsUrl = wsUrl.replace("ws:","wss:")
  window.gameWs = new WebSocket(wsUrl)
  window.gameWs.onmessage = (msg) ->
    wsHandler(msg.data)
  window.gameWs.onclose = () ->
    addChatTechnicalMessage('{{i18n["connectionClosed"]}}')
  initButtons()
  bindArrowKeys()
  translateStatus("INIT_PSEUDOSTATE")
  onChatKeydown = (e) ->
    if e.which==13
      wsSend({"type":"chat","msg":$("#chatInput").val()})
      $("#chatInput").val("")
  $("#chatInput").bind("keydown",onChatKeydown)
  wsKeepAlive()


$ ->
  if $("#labyrinthGame").size() > 0
    handleWholeGame()