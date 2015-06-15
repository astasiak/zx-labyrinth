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
      console.log("WS> "+msg.data);
    window.gameWs.onclose = () ->
      console.log("Websocket closed!");