var zxApp = angular.module('zxApp', ['ngResource']);

zxApp.controller('GamesController', ['$scope', 'GamesResource', function($scope, GamesResource) {
    $scope.games = null

    $scope.loadGames = function(user,limit) {
      params = {}
      if(limit) params.limit = limit;
      if(user) params.user = user;
      GamesResource.query(params,function(data) {
        $scope.games = data;
      });
    };
    $scope.predicate = 'lastActive';
    $scope.reverse = true;
    $scope.order = function(predicate) {
      $scope.reverse = ($scope.predicate === predicate) ? !$scope.reverse : false;
      $scope.predicate = predicate;
    };
    $scope.init = function(userId,limit){
      $scope.loadGames(userId,limit);
    };
}]);

zxApp.factory("GamesResource", function($resource) {
  return $resource('/rest/games');
});

zxApp.controller('UsersController', ['$scope', 'UsersResource', function($scope, UsersResource) {
    $scope.users = null

    $scope.loadUsers = function() {
    	UsersResource.query(function(data) {
        $scope.users = data;
      });
    };
    $scope.predicate = 'name';
    $scope.reverse = false;
    $scope.order = function(predicate) {
      $scope.reverse = ($scope.predicate === predicate) ? !$scope.reverse : false;
      $scope.predicate = predicate;
    };
    var init = function(){
      $scope.loadUsers();
    }();
}]);

zxApp.factory("UsersResource", function($resource) {
  return $resource('/rest/users');
});