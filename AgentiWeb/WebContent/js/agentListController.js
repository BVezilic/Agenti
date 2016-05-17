var app = angular.module('MyApp', []);
app.controller('AgentListCtrl', function($scope, $http) {
	$scope.AgentType = 'Ping';
    $scope.activate = function() {    	
        $http.put("http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running/"+$scope.AgentType+"/"+$scope.pingName);
    };
});