zxApp.controller('I18nController', ['$scope', '$compile', function($scope, $compile) {
  $scope.i18n_pl = {'NAME':'PL',
      // layout
      'youAreLoggedAs': 'Jesteś zalogowany jako',
      'logIn': 'Zaloguj',
      'logOut': 'Wyloguj',
      'register': 'Zarejestruj',
      
      // index
      'newGame': 'Nowa gra',
      'boardWidth': 'Szerokość planszy',
      'boardHeight': 'Wysokość planszy',
      'numberOfWalls': 'Liczba ścian',
      'afterPlay': 'Z dogrywką',
      'rankingGame': 'Gra rankingowa',
      'createGame': 'Utwórz grę',

      'lastGamesList':'Ostatnie gry',
      
      // zx.coffee
      'unknownGameState': 'Nieznany stan gry',
      'awaitingState': 'Oczekiwanie na rozpoczęcie gry',
      'turnOfState': 'Ruch wykonuje',
      'playerEnteredRoom': 'dołączył do gry',
      'playerLeftRoom': 'opuścił grę',
      'connectionClosed': 'Zamknięte połączenie z serwerem',
      'playerWon': 'Grę wygrał',
      'youWon': 'Wygrałeś!',
      'rankingChange': 'Zmiana rankingu gracza',
      'youLost': 'Przegrałeś',
      'incorrectLabyrinth': 'Niepoprawne rozmieszczenie labiryntu',
      'cannotSitDown': 'Nie można usiąść',
      'placeStartAndMeta': 'Umieść start i metę',
      'nobody': 'nikt',
      
      // js.js
      'commit': 'Zatwierdź',
      'usedWalls': 'Użyte ściany',
      
      // game.html
      'up': 'Góra',
      'down': 'Dół',
      'left': 'Lewo',
      'right': 'Prawo',
      'moveComment': 'możesz też grać strzałkami na klawiaturze',
      'playAgainButton': 'Zagraj ponownie',
      'joinButton': 'Dołącz do gry',
        
      // menu
      'menu.github':'Github',
      'menu.howToPlay':'Jak grać?',
      'menu.listGames':'Lista gier',
      'menu.listPlayers':'Lista graczy',
      'menu.createGame':'Załóż nową grę',
      
      //listGames
      'listGames.header':'Lista gier',
      'listGames.id':'Id',
      'listGames.parameters':'Parametry',
      'listGames.ranking':'Ranking',
      'listGames.players':'Gracze',
      'listGames.state':'Status',
      'listGames.created':'Utworzona',
      'listGames.lastActive':'Aktywna',
      'listGames.inMemory':'W pamięci',
      
      //listUsers
      'listUsers.header':'Lista użytkowników',
      'listUsers.name':'Nazwa',
      'listUsers.lastSeen':'Ostatnio widziany',
      'listUsers.registered':'Rejestracja',
      'listUsers.allGames':'Wszystkie gry',
      'listUsers.finishedGames':'Zakończone',
      'listUsers.rating':'Ranking',
      
      //register
      'register.header':'Rejestracja',
      'register.login':'Login',
      'register.password1':'Hasło',
      'register.password2':'Potwierdzenie hasła',
      'register.register':'Rejestruj!',
      
      //user
      'user.allGames':'Wszystkich gier',
      'user.lastGame':'Ostatnia gra',
      'user.wonGames':'Wygranych',
      'user.lostGames':'Przegranych',
      'user.ongoingGames':'Trwających',
      'user.opponents':'Przeciwnicy',
      'user.opponentName':'Nazwa',
      'user.opponentGames':'Gier',
      'user.opponentLastGame':'Ostatnia',
      'user.nobodyYet':'Jeszcze nikt',
      'user.gamesList':'Gry gracza',
      
      // errors
      'errors.badParameters':'Nieprawidłowe parametry gry',
      'errors.needLogIn':'Wymagane zalogowanie',
      'errors.gameNotFound':'Nie znaleziono gry',
      'errors.wrongCredentials':'Błędne dane logowania',
      'errors.whitespacesLogin':'Login nie może zawierać białych znaków',
      'errors.passwordMismatch':'Niepasujące potwierdzenie hasła',
      'errors.cannotRegister':'Nie można zarejestrować pod tym loginem',
      
      'param.afterFinish':'Gra z dogrywką',
      'param.notAfterFinish':'Gra bez dogrywki',
      'param.ranking':'rankingowa',
      'param.notRanking':'towarzyska'
      }
  
  $scope.i18n_en = {'NAME':'EN',
      'youAreLoggedAs': 'You are logged in as',
      'logIn': 'Log in',
      'logOut': 'Log out',
      'register': 'Register',
      
      'newGame': 'New game',
      'boardWidth': 'Board width',
      'boardHeight': 'Board height',
      'numberOfWalls': 'Number of walls',
      'afterPlay': 'With after-play',
      'rankingGame': 'Ranking game',
      'createGame': 'Create game',

      'lastGamesList':'Last games',

      // zx.coffee
      'unknownGameState': 'Unknown game state',
      'awaitingState': 'Awaiting start of the game',
      'turnOfState': 'There is turn of',
      'playerEnteredRoom': 'entered the game',
      'playerLeftRoom': 'left the game',
      'connectionClosed': 'Connection with server closed',
      'playerWon': 'The winner is',
      'youWon': 'You won!',
      'youLost': 'You lost',
      'rankingChange': 'Change of the ranking of',
      'incorrectLabyrinth': 'Incorrect labyrinth shape',
      'cannotSitDown': 'Cannot sit down',
      'placeStartAndMeta': 'Place start and meta',
      'nobody': 'nobody',
      
      // js.js
      'commit': 'Confirm',
      'usedWalls': 'Used walls',
      
      // game.html
      'up': 'Up',
      'down': 'Down',
      'left': 'Left',
      'right': 'Right',
      'moveComment': 'you can also move with keyboard arrows',
      'playAgainButton': 'Play again',
      'joinButton': 'Join game',
        
      // menu
      'menu.github':'Github',
      'menu.howToPlay':'How to play?',
      'menu.listGames':'List of games',
      'menu.listPlayers':'List of players',
      'menu.createGame':'Create new game',
      
      //listGames
      'listGames.header':'List of games',
      'listGames.id':'Id',
      'listGames.parameters':'Parameters',
      'listGames.ranking':'Ranking',
      'listGames.players':'Players',
      'listGames.state':'State',
      'listGames.created':'Created',
      'listGames.lastActive':'Last active',
      'listGames.inMemory':'In memory',
      
      //listUsers
      'listUsers.header':'List of players',
      'listUsers.name':'Name',
      'listUsers.lastSeen':'Last seen',
      'listUsers.registered':'Registered',
      'listUsers.allGames':'All games',
      'listUsers.finishedGames':'Finished',
      'listUsers.rating':'Ranking',
      
      //register
      'register.header':'Registration',
      'register.login':'Login',
      'register.password1':'Password',
      'register.password2':'Password confirmation',
      'register.register':'Register!',
      
      //user
      'user.allGames':'All games',
      'user.lastGame':'Last game',
      'user.wonGames':'Won games',
      'user.lostGames':'Lost games',
      'user.ongoingGames':'Ongoing',
      'user.opponents':'Opponents',
      'user.opponentName':'Name',
      'user.opponentGames':'Number of games',
      'user.opponentLastGame':'Last game',
      'user.nobodyYet':'Nobody yet',
      'user.gamesList':'Games of the player',
      
      //errors
      'errors.badParameters':'Bad game parameters',
      'errors.needLogIn':'You need to log in',
      'errors.gameNotFound':'Game not found',
      'errors.wrongCredentials':'Wrong credentials',
      'errors.whitespacesLogin':'Login cannot contain whitespaces',
      'errors.passwordMismatch':'Password confirmation mismatch',
      'errors.cannotRegister':'Cannot register with given login',
      
      'param.afterFinish':'Game with after-play',
      'param.notAfterFinish':'Game without after-play',
      'param.ranking':'with ranking',
      'param.notRanking':'without ranking'
  }
  
  $scope.i18n_options = [$scope.i18n_pl, $scope.i18n_en]
  $scope.i18n = $scope.i18n_pl // TODO: from local storage
  var locale = localStorage["locale"];
  for(i=0;i<$scope.i18n_options.length;i++) {
    if($scope.i18n_options[i]["NAME"]===locale) {
      $scope.i18n = $scope.i18n_options[i];
    }
  }
  
  $scope.i18n_save = function() {
    localStorage["locale"] = $scope.i18n["NAME"]
  }
  
  // stuff for javascript generated messages
  $scope.compile = function(html, callback) {
    $scope.$apply(function() {
      var element = $compile(html)($scope);
      callback(element);
    });
  }
  $scope.get = function(code) {
    return $scope.i18n[code];
  }
}])

i18nCreate = function(html, callback) {
  var controllerElement = document.querySelector('body');
  var controllerScope = angular.element(controllerElement).scope(); // I18nController
  return controllerScope.compile(html, callback);
}
i18nGet = function(code) {
  var controllerElement = document.querySelector('body');
  var controllerScope = angular.element(controllerElement).scope(); // I18nController
  return controllerScope.get(code);
}