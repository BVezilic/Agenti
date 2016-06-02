var app = angular.module('MyApp', []);
app.controller('AgentController', function($scope, $http, $timeout, $interval) {
	// da li je podrzan websocket
	if ("WebSocket" in window) {
		$scope.haveWS = true;
	} else {
		$scope.haveWS = false;
	} 
	
	// inicijalno se radi preko REST-a
	$scope.podrzanWebSocket = false;
	
	// getteri za promenljive preko WSMenadzera
	var getPerformative = function(){
		if ($scope.podrzanWebSocket) {
			var msg = {
				type: 'getPerformative'
			}
			$scope.socket.send(JSON.stringify(msg));
		} else {
			$http({
			  method: 'GET',
			  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/message',
			}).then(function successCallback(response) {
				$scope.performative = response.data;
			  }, function errorCallback(response) {
			    alert('Nesto je poslo kako ne treba!');
			  });
		}
	}
	var getActive = function(){
		if ($scope.podrzanWebSocket) {
			var msg = {
				type: 'getActive'
			}
			$scope.socket.send(JSON.stringify(msg));
		} else {
			$http({
			  method: 'GET',
			  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running'
			}).then(function successCallback(response) {
				setActive(response.data);
				setSender(response.data);
				setReceivers(response.data);
			  }, function errorCallback(response) {
			    alert('Nesto je poslo kako ne treba!');
			  });
		}
	}
	var getTypes = function() {
		if ($scope.podrzanWebSocket) {
			var msg = {
				type: 'getTypes'
			}
			$scope.socket.send(JSON.stringify(msg));
		} else {
			$http({
			  method: 'GET',
			  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/types'
			}).then(function successCallback(response) {
				setTypes(response.data);
			  }, function errorCallback(response) {
			    alert('Nesto je poslo kako ne treba!');
			  });
		}
	}
	
	// inicijalizuj podatke i startuj socket ako moze
	if ("WebSocket" in window) {
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
				var action = angular.fromJson(msg.data);
				switch(action.type) {
				case 'performative':
					setPerformative(action.data);
					break;
				case 'types':
					setTypes(action.data);
					break;
				case 'active':	
					console.log(action.data);
					setActive(action.data);
					setSender(action.data);
					setReceivers(action.data);
					break;
				case 'messagesPoll':
					addMessage(formatMessages(action.data));
					break;
				}					
			}
	
			$scope.socket.onclose = function() {
				console.log('onclose. Socket Status: ' + $scope.socket.readyState
						+ ' (Closed)\n');
				$scope.socket = null;
			}
	
		} catch (exception) {
			console.log('Error' + exception + "\n");
		}
	} else {
		getPerformative();
		getActive();
		getTypes();
	}
	
	var activateAgent = function(agentType, agentName) {
		var msg = {
			type: 'activateAgent',
			name: agentName,
			agentType: agentType
		}
		$scope.socket.send(JSON.stringify(msg));
	}
	var deactivateAgent = function(name, alias) {
		var msg = {
			type: 'deactivateAgent',
			name: name,
			alias: alias
		}
		$scope.socket.send(JSON.stringify(msg));
	}
    
	var sendACLMessage = function(aclMessage) {
		aclMessage = removeHash(aclMessage);
		var msg = {
			type: 'aclMessage',
			data: JSON.stringify(aclMessage)
		}
		$scope.socket.send(JSON.stringify(msg));
	}
	
    var pollMessages = function() {
		$http({
		  method: 'GET',
		  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/messages',
		}).then(function successCallback(response) {
			addMessage(formatMessages(response.data));
		  }, function errorCallback(response) {
		    alert('Nesto je poslo kako ne treba!');
		  });
    }
    
    var formatMessages = function(messages) {
    	var retVal = [];
    	for (var i=0; i<messages.length; i++) {	
			var sender = '';
			var receivers = '';
			
			if (messages[i].sender == null) {
				sender = 'Klijent';
			}
			else {
				sender = messages[i].sender.name +'('+ messages[i].sender.type.name +')';
			}
			
			if (messages[i].receivers == null) {
				receivers = 'Niko';
			}
			else {
				for (var j=0; j<messages[i].receivers.length; j++) {
					receivers += messages[i].receivers[j].name +'('+ messages[i].receivers[j].type.name +')'+ (j<messages[i].receivers.length-1?', ':'');
				}
			}
						
			retVal.push('Message performative: '+messages[i].performative+', from: '+sender+', to: '+receivers+' content: '+messages[i].content);
		}
    	return retVal;
    }
    
    var removeHash = function(aclMessage) {
    	for (var i=0; i<aclMessage.receivers.length; i++)
    		delete aclMessage.receivers[i].$$hashKey;
    	if(aclMessage.sender)
    		delete aclMessage.sender.$$hashKey;
    	return aclMessage;
    }
    
    var refresh = function() {
    	if ($scope.podrzanWebSocket)
    		$scope.$apply();
    }
	// posalji koji agent treba da se aktivira 
    $scope.activate = function(agentType, agentName) {
    	if ($scope.podrzanWebSocket) {	
    		activateAgent(agentType.name, agentName);
    		getActive();
    	} else {
	    	$http({
			  method: 'PUT',
			  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running/'+agentType.name+'/'+agentName
			}).then(function successCallback(response) {
				// posalji zahtev da dobijem listu aktivnih agenata
				$http({
					  method: 'GET',
					  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running'
					}).then(function successCallback(response) {
						setActive(response.data);
						setSender(response.data);
						setReceivers(response.data);
					  }, function errorCallback(response) {
					    alert('Nesto je poslo kako ne treba!');
					  });
			  }, function errorCallback(response) {
			    alert('Nesto je poslo kako ne treba!');
			  });    		    		
    	}
    };
    
    // posalji zahtev da se deaktivira odredjen agent
    $scope.deactivate = function(agentAID) { 
    	if ($scope.podrzanWebSocket) {
    		deactivateAgent(agentAID.name, agentAID.host.alias);
    		getActive();	  		 
    	} else {
    		$http({
  	  		  method: 'DELETE',
  	  		  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running/'+agentAID.name+'/'+agentAID.host.alias
  	  		}).then(function successCallback(response) {
  	  			// povuci listu activnih agenata
  	  			$http({
  					  method: 'GET',
  					  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/agents/running'
  					}).then(function successCallback(response) {
  						setActive(response.data);
  						setSender(response.data);
  					  }, function errorCallback(response) {
  					    alert('Nesto je poslo kako ne treba prilikom dodavanja liste aktivnih agenata!');
  					  });
  	  		  }, function errorCallback(response) {
  	  		    alert('Nesto je poslo kako ne treba!');
  	  		});
    	}
    };
    
    $scope.sendMessage = function(){
		var ACLMessage = {
			performative: $scope.selectedPerform,
			sender: $scope.selectedSender,
			receivers: $scope.selectedReceiver,
			replyTo: $scope.selectedReplyTo,
			content: $scope.selectedContent,
			language: $scope.selectedLanguage,
			encoding: $scope.selectedEncoding,
			ontology: $scope.selectedOntology,
			protocol: $scope.selectedProtocol,
			conversationId: $scope.selectedConversatonId,
			replyWith: $scope.selectedReplyWith,
			replayBy: $scope.selectedReplyBy
		}
		// posalji poruku
		if ($scope.podrzanWebSocket) {
    		sendACLMessage(ACLMessage);	  		 
    	} else {
			$http({
			  method: 'POST',
			  url: 'http://localhost:8080/AgentiWeb/rest/agentskiCentar/messages',
			  data: ACLMessage
			}).then(function successCallback(response) {
				// uspesno poslao poruku
			  }, function errorCallback(response) {
			    alert('Nesto je poslo kako ne treba!');
			  });
    	}
	}
    
    $scope.reset = function(){
    	$scope.selectedPerform = undefined;
    	$scope.selectedSender = undefined;
    	$scope.selectedReceiver = undefined;
    	$scope.selectedReplyTo = undefined;
    	$scope.selectedContent = undefined;
    	$scope.selectedLanguage = undefined;
    	$scope.selectedEncoding = undefined;
    	$scope.selectedOntology = undefined;
    	$scope.selectedProtocol = undefined;
    	$scope.selectedConversationId = undefined;
    	$scope.selectedReplyWith = undefined;
    	$scope.selectedReplyBy = undefined;
    }

    //setteri
	var setPerformative = function(data){
		$scope.performative = data;
		refresh();
	}
	var setActive = function(data){
		$scope.activeAgents = data;
		refresh();
	}
	var setTypes = function(data) {
		$scope.supportedAgents = data;
		refresh();
	}
	var setReceivers = function(data) {
		if (!$scope.selectedReceiver)
			$scope.receivers = data;
		else if ($scope.selectedReceiver.length == 0)
			$scope.receivers = data;
		refresh();
	}
	var setSender = function(data) {
		if (!$scope.selectedSender)
			$scope.sender = data;
		refresh();
	}
	var addMessage = function(data) {
		$scope.consoleLog = data;
		refresh();
	}
    
    //polling
	var promiseMsg = $interval(pollMessages, 5000);
	var promiseAct = $interval(getActive, 2000);
	$scope.changeWS = function() {
		if ($scope.podrzanWebSocket) {
			$interval.cancel(promiseMsg);
			$interval.cancel(promiseAct);
		} else {
			promiseMsg = $interval(pollMessages, 5000);
			promiseAct = $interval(getActive, 2000);
		}
		
	}
});