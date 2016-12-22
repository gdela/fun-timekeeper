package pl.gdela.timekeeper.domain;

import java.time.Duration;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.gdela.timekeeper.domain.RaceEvent.LapFinished;
import pl.gdela.timekeeper.domain.RaceEvent.RaceFinished;
import pl.gdela.timekeeper.domain.RaceEvent.RaceStarted;

/**
 * Logs permanently lap and race times to a log file.
 */
@Service
public class RaceLog {
	private static final Logger log = LoggerFactory.getLogger(RaceLog.class);

	@EventListener
	public void log(RaceStarted event) {
		log.info("race started");
	}

	@EventListener
	public void log(LapFinished event) {
		log.info("lap {} finished in {}", event.lapNr, format(event.lapTime));
	}

	@EventListener
	public void log(RaceFinished event) {
		log.info("race with {} laps finished in {}", event.numberOfLaps, format(event.raceTime));
	}

	private static String format(Duration duration) {
		return String.format(Locale.US, "%.3f seconds", duration.toMillis() / 1000f);
	}
}
