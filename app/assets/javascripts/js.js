
function Game(params) {
  this.params = params;
  this.board = {
    start: undefined,
    meta: undefined,
    position: undefined,
    borders: []
  };
  this.bordersUsed = 0;
  this.board.borders = [];
  for(var i=0;i<this.params.height;i++) {
    row = [];
    for(var j=0;j<this.params.width;j++)
      row.push({v:false,h:false});
    this.board.borders.push(row);
  }
}

function Board(selector,params) {
  var thisView = this;
  this.selector = selector;
  this.editable = false;
  this.onsubmit = function(){}
  
  this.initParams = function(params) {
    this.game = new Game(params);
    var TABLE_CLASS = "board";
    var FIELD_CLASS = "field";
    var CORNER_CLASS = "corner";
    var HORIZONTAL_CLASS = "borderH";
    var VERTICAL_CLASS = "borderV";
    var makeCell = function(clazz,x,y) {
      return $("<td/>").addClass(clazz).attr("data-x",x).attr("data-y",y);
    }
    var makeRow = function(classA, classB, rowId) {
      row = $("<tr/>");
      row.append(makeCell(classA,0,rowId));
      for(var i=1;i<thisView.game.params.width;i++) {
        row.append(makeCell(classB,i,rowId));
        row.append(makeCell(classA,i,rowId));
      }
      return row;
    }
    var table = $("<table/>").addClass(TABLE_CLASS);
    table.append(makeRow(FIELD_CLASS,VERTICAL_CLASS,0));
    for(var i=1;i<this.game.params.height;i++) {
      table.append(makeRow(HORIZONTAL_CLASS,CORNER_CLASS,i));
      table.append(makeRow(FIELD_CLASS,VERTICAL_CLASS,i));
    }
    table.appendTo(this.find(".board"))
    this.find(".max-walls").text(this.game.params.borders);
  }

  this.setBoard = function(board) {
    this.game.board = board
    this.redrawStart();
    this.redrawMeta();
    this.redrawBorders();
    this.redrawPosition();
  }
  this.getBoard = function() {
    return this.game.board;
  }
  
  this.setSubmit = function(callback) {
	this.onsubmit = callback;
  }
  
  this.find = function(subselector) { return $(this.selector).find(subselector); }
  this.getBorder = function(x,y,type) {return this.find(".border"+type+"[data-x="+x+"][data-y="+y+"]");}
  this.getCorner = function(x,y) {return this.find(".corner[data-x="+x+"][data-y="+y+"]");}
  this.getElem = function(x,y) {return this.find(".field[data-x="+x+"][data-y="+y+"]");}
  this.redrawBorders = function() {
    var borderCounter = 0;
    for(var i=0;i<this.game.params.height;i++) {
      for(var j=0;j<this.game.params.width;j++) {
        if(this.game.board.borders[i][j]['h']) {
          borderCounter++;
          thisView.getBorder(j,i,'H').addClass("selected");
        } else {
          thisView.getBorder(j,i,'H').removeClass("selected");
        }
        if(this.game.board.borders[i][j]['v']) {
          borderCounter++;
          thisView.getBorder(j,i,'V').addClass("selected");
        } else {
          thisView.getBorder(j,i,'V').removeClass("selected");
        }
      }
    }
    for(var i=1;i<this.game.params.height;i++) {
      for(var j=1;j<this.game.params.width;j++) {
        var neighbours = 0;
        if(this.game.board.borders[i][j]['v']) neighbours++;
        if(this.game.board.borders[i][j]['h']) neighbours++;
        if(this.game.board.borders[i-1][j]['v']) neighbours++;
        if(this.game.board.borders[i][j-1]['h']) neighbours++;
        if(neighbours>1) {
          thisView.getCorner(j,i).addClass("selected");
        } else {
          thisView.getCorner(j,i).removeClass("selected");
        }
      }
    }
    this.find(".used-walls").text(borderCounter);
  }
  this.redrawEndpoint = function(object,target,position) {
    if(position) {
      var x = position[0];
      var y = position[1];
      target = this.getElem(x,y);
    }
    object.position({
      of:target,
      using: function(css, calc) {$(this).animate(css, 200);}
    });
  }
  this.redrawStart = function() {this.redrawEndpoint(this.find(".start"),this.find(".start-box"),this.game.board.start);}
  this.redrawMeta = function() {this.redrawEndpoint(this.find(".meta"),this.find(".meta-box"),this.game.board.meta);}
  this.redrawPosition = function() {
    this.find(".field").removeClass("currentPosition");
    position = this.game.board.position;
    if(position) {
      this.getElem(position[0],position[1]).addClass("currentPosition");
    }
  }
  this.createDraggableStartAndMeta = function() {
    this.find( ".start, .meta" ).draggable();
    this.find( ".field" ).droppable({
      greedy: true,
      hoverClass: "field-hovered",
      drop: function( event, ui ) {
        window.drrr = ui.draggable;
        if(ui.draggable.parents(thisView.selector).length) {
          var x = $(this).attr("data-x");
          var y = $(this).attr("data-y");
          if(ui.draggable.hasClass("start")) {
            thisView.game.board.start = [x,y];
            thisView.redrawStart();
          } else if(ui.draggable.hasClass("meta")) {
            thisView.game.board.meta = [x,y];
            thisView.redrawMeta();
          }
        }
      }
    })
    $( this.selector ).droppable({
      drop: function( event, ui ) {
        if(ui.draggable.parents(thisView.selector).length) {
          if(ui.draggable.hasClass("start")) {
            thisView.game.board.start = undefined;
            thisView.redrawStart();
          } else if(ui.draggable.hasClass("meta")) {
            thisView.game.board.meta = undefined;
            thisView.redrawMeta();
          }
        }
      }
    });
  }
  this.createClickableBorders = function() {
    // TODO: more space for click
    this.find(".borderH, .borderV").click(function() {
      if(!thisView.editable) {
        return;
      }
      var x = parseInt($(this).attr("data-x"));
      var y = parseInt($(this).attr("data-y"));
      var type = $(this).hasClass("borderH") ? 'h' : 'v';
      var current = thisView.game.board.borders[y][x][type];
      if(current) {
        thisView.game.board.borders[y][x][type] = false;
        thisView.game.bordersUsed--;
      } else if(thisView.game.bordersUsed<thisView.game.params.borders) {
        thisView.game.board.borders[y][x][type] = true;
        thisView.game.bordersUsed++;
      }
      thisView.redrawBorders();
    });
  }
  this.makeSubmitButton = function() {
    this.find(".submit-button").click(function() {
      if(!thisView.editable) {
        return;
      }
      thisView.onsubmit()
    });
  }
  this.setEditable = function(editable) {
    if(editable) {
      this.editable = true;
      $(this.selector).addClass("creatingLabyrinth");
      this.find( ".start, .meta" ).draggable('enable');
    } else {
      this.editable = false;
      $(this.selector).removeClass("creatingLabyrinth");
      this.find( ".start, .meta" ).draggable('disable');
    }
  }
  this.initLayout = function() {
	  $(this.selector).html("<div class='start-meta'><div class='start-box'><div class='start'>S</div></div>"+
     "<div class='meta-box'><div class='meta'>M</div></div></div><div class='submit-button'>Zatwierdź</div>"+
     "<div class='board'></div>"+
     "<p>Użyte ściany: <span class='used-walls'>0</span>/<span class='max-walls'>0</span></p>");
  }
  this.initLayout();
  this.initParams(params);
  this.createDraggableStartAndMeta();
  this.createClickableBorders();
  this.makeSubmitButton();
  this.setEditable(false);
}