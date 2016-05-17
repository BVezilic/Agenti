var app = angular.module('myApp', []);
app.controller('AgentListCtrl', function($scope, $http) {
    $scope.activate = function() {
        $http.put("http://localhost:8080/AgentiWeb/rest/AgentskiCentar/agents/running/"+$scope.AgentType+"/"+$scope.pingName);
        console.log("http://localhost:8080/AgentiWeb/rest/AgentskiCentar/agents/running/"+$scope.AgentType+"/"+$scope.pingName);
    };
});