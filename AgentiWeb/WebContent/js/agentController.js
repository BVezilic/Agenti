var app = angular.module('MyApp', []);
app.controller('AgentController', function($scope, $http) {
	
	// funkcije koje socket moze da pozove
	var getPerformative = function(){
		var msg = {
			type: 'getPerformative'
		}
		$scope.socket.send(JSON.stringify(msg));
	}
	var getActive = function(){
		var msg = {
			type: 'getActive'
		}
		$scope.socket.send(JSON.stringify(msg));
	}
	var getTypes = function() {
		var msg = {
			type: 'getTypes'
		}
		$scope.socket.send(JSON.stringify(msg));
	}
	// da li je podrzan websocket
	if (!("WebSocket" in window)) {
		$scope.podrzanWebSocket = false;
	} else {
		$scope.podrzanWebSocket = true;
		var host = "ws://localhost:8080/AgentiWeb/websocket";
		try {
			$scope.socket = new WebSocket(host);
			console.log('connect. Socket Status: ' + $scope.socket.readyState + "\n");

			$scope.socket.onopen = function() {
				console.log('onopen. Socket Status: ' + $scope.socket.readyState
						+ ' (open)\n');
				getPerformative();
				getActive();
				getTypes();
			}

			$scope.socket.onmessage = function(msg) {
				console.log(msg.data);
			}

			$scope.socket.onclose = function() {
				console.log('onclose. Socket Status: ' + $scope.socket.readyState
						+ ' (Closed)\n');
				$scope.socket = null;
			}

		} catch (exception) {
			console.log('Error' + exception + "\n");
		}
	}
	// dobavi listu perfomativa
	if (!$scope.podrzanWebSocket) {	
		$http({
		  method: 'GET',
		  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/message',
		}).then(function successCallback(response) {
			$scope.performative = response.data;
		  }, function errorCallback(response) {
		    alert('Nesto je poslo kako ne treba!');
		  });
	}
	
	// dobavi listu svih tipova agenata koji agentski centar podrazava
	$http({
	  method: 'GET',
	  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/types'
	}).then(function successCallback(response) {
		$scope.supportedAgents = response.data;
		$scope.receivers = response.data;
	  }, function errorCallback(response) {
	    alert('Nesto je poslo kako ne treba!');
	  });
	
	// dobavi listu aktivnih agenata
	$http({
	  method: 'GET',
	  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running'
	}).then(function successCallback(response) {
		$scope.activeAgents = response.data;
		$scope.sender = response.data;
	  }, function errorCallback(response) {
	    alert('Nesto je poslo kako ne treba!');
	  });
	
	// posalji koji agent treba da se aktivira 
    $scope.activate = function(agentType, agentName) {
    	$http({
		  method: 'PUT',
		  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running/'+agentType.name+'/'+agentName
		}).then(function successCallback(response) {
			// posalji zahtev da dobijem listu aktivnih agenata
			$http({
				  method: 'GET',
				  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running'
				}).then(function successCallback(response) {
					$scope.activeAgents = response.data;
					$scope.sender = response.data;
				  }, function errorCallback(response) {
				    alert('Nesto je poslo kako ne treba!');
				  });
		  }, function errorCallback(response) {
		    alert('Nesto je poslo kako ne treba!');
		  });
    };
    
    // posalji zahtev da se deaktivira odredjen agent
    $scope.deactivate = function(agentAID) {   
    	$http({
  		  method: 'DELETE',
  		  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running/'+agentAID.name+'/'+agentAID.host.alias
  		}).then(function successCallback(response) {
  			// povuci listu activnih agenata
  			$http({
				  method: 'GET',
				  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running'
				}).then(function successCallback(response) {
					$scope.activeAgents = response.data;
					$scope.sender = response.data;
				  }, function errorCallback(response) {
				    alert('Nesto je poslo kako ne treba!');
				  });
  		  }, function errorCallback(response) {
  		    alert('Nesto je poslo kako ne treba!');
  		  });
    };
    
    $scope.sendMessage = function(){
		var ACLMessage = {
			performative: $scope.performative,
			sender: $scope.sender,
			receivers: $scope.receivers,
			replyTo: $scope.replyTo,
			content: $scope.content,
			language: $scope.language,
			encoding: $scope.encoding,
			ontology: $scope.ontology,
			protocol: $scope.protocol,
			conversationId: $scope.conversatonId,
			replyWith: $scope.replyWith,
			replayBy: $scope.replyBy
		}
		
		// posalji poruku
		$http({
		  method: 'POST',
		  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/messages',
		  data: angular.toJson(ACLMessage)
		}).then(function successCallback(response) {
			alert('Uspeo sam!');
		  }, function errorCallback(response) {
		    alert('Nesto je poslo kako ne treba!');
		  });
	}
});