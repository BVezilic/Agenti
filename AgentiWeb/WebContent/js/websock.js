function sendMessage() {
	var perform = $('#perform').val();
	var sender = $('#sender').val();
	var sender = $('#receivers').val();
	var sender = $('#replyTo').val();
	var sender = $('#content').val();
	var sender = $('#').val();
	var sender = $('#sender').val();
	var sender = $('#sender').val();
	var sender = $('#sender').val();
	var sender = $('#sender').val();
	
	if (content == "") {
		alert('Please enter message\n');
		return;
	}
	
	try {
		msg = "send;" + to + ";" + content;
		socket.send(msg);
		log('I have sent:' + msg);
	} catch (exception) {
		log('Error: ' + exception + "\n");
	}
}

function submitWS(){
	console.log("Submit WS");
}

function submitREST(){
	console.log("Submit REST");
}

function log(msg) {
	console.log(msg);
}

var podrzanWebSocket = false;
if (!("WebSocket" in window)) {
	podrzanWebSocket = false;
} else {
	podrzanWebSocket = true;
	var host = "ws://localhost:8080/AgentiWeb/websocket";
	try {
		socket = new WebSocket(host);
		log('connect. Socket Status: ' + socket.readyState + "\n");

		socket.onopen = function() {
			log('onopen. Socket Status: ' + socket.readyState
					+ ' (open)\n');
		}

		socket.onmessage = function(msg) {
			var action = msg.data.split(";");
			
		}

		socket.onclose = function() {
			log('onclose. Socket Status: ' + socket.readyState
					+ ' (Closed)\n');
			socket = null;
		}

	} catch (exception) {
		log('Error' + exception + "\n");
	}
}
$(document).ready(function(){
	
	$('#submit').click(function() {
			if (podrzanWebSocket){
				submitWS();
			} else {
				submitREST();
			}
	});
	
});