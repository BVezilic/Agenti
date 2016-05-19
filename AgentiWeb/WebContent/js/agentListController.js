var app = angular.module('MyApp', []);
app.controller('AgentListCtrl', function($scope, $http) {
	$scope.switch = true;
	$scope.supportedAgents = ['Ping','Pong','Map'];
	//$http.get("http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents").then(function (response) {$scope.supportedAgents = response.data;});
    $scope.activate = function(agentType, agentName) {   
    	alert(agentName +' ' + agentType);
        //$http.put("http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running/"+$scope.AgentType+"/"+$scope.pingName);
        //$scope.switch = false;
    	//dodao sam komentar
    };
    $scope.deactivate = function(agentType, agentNames) {    	
        $http.delete("http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running/"+$scope.pingName);
        $scope.switch = true;
    };
});