"use strict";

/**
 * Visual effects for the history of races table.
 */
var History = (function () {

	var $historyTable;

	$(document).ready(function() {
		$historyTable = $('#history tbody');
		var observer = new MutationObserver(highlighAchievements);
		observer.observe($historyTable.get(0), { childList:true });
	});

	function highlighAchievements() {

		// highlight rows with best and good race times
		var $raceTimeColumn = $historyTable.find('td:nth-child(2)');
		var bestRaceTime = Number.MAX_VALUE;
		$raceTimeColumn.each(function () {
			bestRaceTime = Math.min(bestRaceTime, $(this).text());
		});
		$raceTimeColumn.each(function () {
			var $race = $(this).parent();
			var raceTime = $(this).text();
			if (raceTime == bestRaceTime) $race.addClass('best');
			if (raceTime <= bestRaceTime * 1.1) $race.addClass('good');
		});

		// highlight best values in each column
		for (var colNr = 2; colNr <= 4; colNr++) {
			var $column = $historyTable.find('td:nth-child('+colNr+')');
			var bestValue = Number.MAX_VALUE;
			$column.each(function () {
				bestValue = Math.min(bestValue, $(this).text());
			});
			$column.each(function () {
				var value = $(this).text();
				if (value == bestValue) $(this).addClass('best');
			});
		}
	}

    return {}

})();
