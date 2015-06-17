$ ->
  $("#join_form_button").click ->
    window.location.href = $("body").data("game-url")+$("#join_form_game_id").val()