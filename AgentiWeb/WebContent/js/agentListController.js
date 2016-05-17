var app = angular.module('MyApp', []);
app.controller('AgentListCtrl', function($scope, $http) {
	$scope.switch = true;
	$scope.AgentType = 'Ping';
    $scope.activate = function() {    	
        $http.put("http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running/"+$scope.AgentType+"/"+$scope.pingName);
        $scope.switch = false;
    };
    $scope.deactivate = function() {    	
        $http.delete("http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running/"+$scope.pingName);
        $scope.switch = true;
    };
});