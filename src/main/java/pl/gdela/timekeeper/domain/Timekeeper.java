package pl.gdela.timekeeper.domain;


import net.jcip.annotations.ThreadSafe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

@Service
@ThreadSafe
public class Timekeeper {

	private final EventPublisher eventPublisher;

	private int numberOfLaps;

	private int currentLapNr;

	private Instant lapStarted;

	private Instant raceStarted;

	@Autowired
	public Timekeeper(EventPublisher eventPublisher) {
		this.eventPublisher = checkNotNull(eventPublisher);
	}

	public synchronized void requestRaceStart(int numberOfLaps) {
		// todo: make the countdown asynchronous
		for (int i = 3; i >= 1; i--) {
			eventPublisher.publishEvent(new RaceEvent.Countdown(i));
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException ignore) {
				Thread.currentThread().interrupt();
				return;
			}
		}
		this.numberOfLaps = numberOfLaps;
		currentLapNr = 1;
		lapStarted = raceStarted = Instant.now();
		eventPublisher.publishEvent(new RaceEvent.RaceStarted());
    }

    public synchronized void photocellInterrupted() {
		if (lapStarted != null) {
			finishLap();
		}
	}

	private void finishLap() {
		int finishedLapNr = currentLapNr;
		Instant lapEnded = Instant.now();
		Duration finishedLapTime = Duration.between(lapStarted, lapEnded);
		eventPublisher.publishEvent(new RaceEvent.LapFinished(finishedLapNr, finishedLapTime));

		if (finishedLapNr < numberOfLaps) {
			currentLapNr++;
			lapStarted = lapEnded;
		} else {
			finishRace();
			currentLapNr = 0;
			lapStarted = null;
		}
	}

	private void finishRace() {
		Instant raceEnded = Instant.now();
		Duration raceTime = Duration.between(raceStarted, raceEnded);
		eventPublisher.publishEvent(new RaceEvent.RaceFinished(numberOfLaps, raceTime));
		raceStarted = null;
	}
}
