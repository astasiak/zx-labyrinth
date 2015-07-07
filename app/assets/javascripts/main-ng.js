var zxApp = angular.module('zxApp', ['ngResource']);

zxApp.controller('GamesController', ['$scope', 'GamesResource', function($scope, GamesResource) {
    $scope.games = []

    $scope.loadGames = function() {
      GamesResource.query(function(data) {
        $scope.games = data;
      });
    };
    var init = function(){
      $scope.loadGames();
    }();
}]);

zxApp.factory("GamesResource", function($resource) {
  return $resource('/rest/games');
});