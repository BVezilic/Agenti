var app = angular.module('MyApp', []);
app.controller('AgentListCtrl', function($scope, $http) {
	
	$http({
	  method: 'GET',
	  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/types'
	}).then(function successCallback(response) {
		$scope.supportedAgents = response.data;
	  }, function errorCallback(response) {
	    alert('Nesto je poslo kako ne treba!');
	  });
	
	$http({
	  method: 'GET',
	  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running'
	}).then(function successCallback(response) {
		$scope.activeAgents = response.data;
	  }, function errorCallback(response) {
	    alert('Nesto je poslo kako ne treba!');
	  });
	
    $scope.activate = function(agentType, agentName) {
    	$http({
		  method: 'PUT',
		  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running/'+agentType.name+'/'+agentName
		}).then(function successCallback(response) {
			$http({
				  method: 'GET',
				  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running'
				}).then(function successCallback(response) {
					$scope.activeAgents = response.data;
				  }, function errorCallback(response) {
				    alert('Nesto je poslo kako ne treba!');
				  });
		  }, function errorCallback(response) {
		    alert('Nesto je poslo kako ne treba!');
		  });
    };
    
    $scope.deactivate = function(agentName) {   
    	$http({
  		  method: 'DELETE',
  		  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running/'+agentName
  		}).then(function successCallback(response) {
  			$http({
				  method: 'GET',
				  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running'
				}).then(function successCallback(response) {
					$scope.activeAgents = response.data;
				  }, function errorCallback(response) {
				    alert('Nesto je poslo kako ne treba!');
				  });
  		  }, function errorCallback(response) {
  		    alert('Nesto je poslo kako ne treba!');
  		  });
    };
});