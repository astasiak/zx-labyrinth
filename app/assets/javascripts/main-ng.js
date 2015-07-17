var zxApp = angular.module('zxApp', ['ngResource']);

zxApp.controller('GamesController', ['$scope', 'GamesResource', function($scope, GamesResource) {
    $scope.games = []

    $scope.loadGames = function() {
      GamesResource.query(function(data) {
        $scope.games = data;
      });
    };
    $scope.predicate = 'id';
    $scope.reverse = false;
    $scope.order = function(predicate) {
      $scope.reverse = ($scope.predicate === predicate) ? !$scope.reverse : false;
      $scope.predicate = predicate;
    };
    var init = function(){
      $scope.loadGames();
    }();
}]);

zxApp.factory("GamesResource", function($resource) {
  return $resource('/rest/games');
});

zxApp.controller('UsersController', ['$scope', 'UsersResource', function($scope, UsersResource) {
    $scope.users = []

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