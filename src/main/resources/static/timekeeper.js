"use strict";

var Timekeeper = (function () {

    var stomp;

	var $raceLogTable;
	var $historyTable;

	$(document).ready(function() {
		$raceLogTable = $('#race-log tbody');
		$historyTable = $('#history tbody');
		initiate();
	});

	function initiate() {
		stomp = Stomp.over(new SockJS('/stomp-endpoint'));
		stomp.connect({}, function () {
			$(window).on('beforeunload', disconnectStomp);
			stomp.subscribe('/topic/countdown', parseAndCall(onCountdown));
			stomp.subscribe('/topic/race-started', parseAndCall(onRaceStarted));
			stomp.subscribe('/topic/lap-finished', parseAndCall(onLapFinished));
			stomp.subscribe('/topic/race-finished', parseAndCall(onRaceFinished));
			stomp.subscribe('/topic/race-summary', parseAndCall(onRaceSummary));
			stomp.subscribe('/app/get-history', parseAndCall(showHistory));
			$('#start-race').click(startRace);
		});
		function disconnectStomp() {
			stomp.disconnect();
		}
		function parseAndCall(delegatee) {
			return function (message) {
				delegatee(JSON.parse(message.body));
			}
		}
	}

	function startRace() {
		stomp.send("/app/request-race-start");
	}

	function onCountdown(event) {
		speak(event.timeToStart);
	}

	function onRaceStarted() {
		$raceLogTable.empty();
		speak("Go!");
	}

    function onLapFinished(event) {
		$raceLogTable.append($('<tr><td>' + event.lapNr + '</td><td>' + format(event.lapTime, 3) + '</td></tr>'));
        speak(format(event.lapTime, 1));
    }

	function onRaceFinished(event) {
		$raceLogTable.append($('<tr class="total"><td>Total</td><td>' + format(event.raceTime, 1) + '</td></tr>'));
		speak('Finished ' + event.numberOfLaps + ' laps in ' + format(event.raceTime, 0) + ' seconds');
	}

	function onRaceSummary(event) {
		var $row = $('<tr></tr>');
		$row.append('<td>' + event.numberOfLaps + '</td>');
		$row.append('<td>' + format(event.raceTime, 1) + '</td>');
		$row.append('<td>' + format(event.bestLap, 3) + '</td>');
		$row.append('<td>' + format(event.medLap, 3) + '</td>');
		$historyTable.prepend($row);
	}

	function showHistory(summaries) {
		summaries.reverse().forEach(onRaceSummary);
	}

	function speak(text) {
		console.log('# ' + text);
		Sound.speak(text)
	}

	function format(value, fractionalDigits) {
		return value.toFixed(fractionalDigits).toString();
	}

    return {}

})();
