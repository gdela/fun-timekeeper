"use strict";

var Timekeeper = (function () {

    var stompClient;

	var $raceLogTable;

    $(document).ready(function() {
        $('#start-race').click(startRace);
		$raceLogTable = $('#race-log tbody');
    });

	function startRace() {
		if (stompClient != null) {
			stompClient.send("/app/request-race-start");
			return;
		}
        var socket = new SockJS('/stomp-endpoint');
        stompClient = Stomp.over(socket);
		stompClient.connect({}, function () {
			stompClient.subscribe('/topic/countdown', callWithParsedBody(countdown));
			stompClient.subscribe('/topic/race-started', callWithParsedBody(raceStarted));
            stompClient.subscribe('/topic/lap-finished', callWithParsedBody(lapFinished));
			stompClient.subscribe('/topic/race-finished', callWithParsedBody(raceFinished));
			stompClient.send("/app/request-race-start");
        });
		function callWithParsedBody(delegatee) {
			return function (message) {
				delegatee(JSON.parse(message.body));
			}
		}
	}

	function countdown(event) {
		speak(event.timeToStart);
	}

	function raceStarted() {
		$raceLogTable.empty();
		speak("Go!");
	}

    function lapFinished(event) {
		$raceLogTable.append($('<tr><td>' + event.lapNr + '</td><td>' + event.lapTime.toFixed(3) + '</td></tr>'));
        speak(event.lapTime.toFixed(1).toString());
    }

	function raceFinished(event) {
		$raceLogTable.append($('<tr class="total"><td>Total</td><td>' + event.raceTime.toFixed(3) + '</td></tr>'));
		stompClient.disconnect();
		speak('Finished ' + event.numberOfLaps + ' laps in ' + event.raceTime.toFixed(0).toString() + ' seconds');
	}

	function speak(text) {
		console.log('# ' + text);
		Sound.speak(text)
	}

    return {};

})();
