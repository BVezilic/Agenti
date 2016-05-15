function sendMessage() {
	var to = $('#username').html();
	var content = $('#content').val();
	
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

function log(msg) {
	console.log(msg);
}

if (!("WebSocket" in window)) {
	$('Oh no, you need a browser that supports WebSockets. How about <a href="http://www.google.com/chrome">Google/a>?')
			.appendTo('body');
} else {
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
	$('#sendMsg').click(function() {
		sendMessage();
		$('#content').val("");
	});
});