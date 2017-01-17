package pl.gdela.timekeeper.domain;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.gdela.timekeeper.domain.RaceEvent.LapFinished;
import pl.gdela.timekeeper.domain.RaceEvent.RaceFinished;
import pl.gdela.timekeeper.domain.RaceEvent.RaceStarted;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Persists lap and race times and also summarizes each race.
 * Publishes {@link RaceSummary} after a race is finished.
 */
@Service
@ThreadSafe
public class Chronicler {
	private static final Logger log = LoggerFactory.getLogger(Chronicler.class);

	/**
	 * File in which summaries of all races are kept.
	 */
	private static final Path FULL_HISTORY_FILE = Paths.get("races.json");

	/**
	 * How many race summaries to keep in memory.
	 */
	private static final int RECENT_HISTORY_SIZE = 20;

	/**
	 * The in-memory part of races history.
	 */
	private final ArrayDeque<RaceSummary> history = new ArrayDeque<>(RECENT_HISTORY_SIZE);

	/**
	 * Statistics of lap times for the race that is currently in progress.
	 */
	private final DescriptiveStatistics lapTimes = new DescriptiveStatistics();

	private final EventPublisher eventPublisher;

	private final ObjectMapper objectMapper;

	@Autowired
	public Chronicler(EventPublisher eventPublisher, ObjectMapper objectMapper) {
		this.eventPublisher = checkNotNull(eventPublisher);
		this.objectMapper = checkNotNull(objectMapper);
	}

	@EventListener
	public synchronized void handle(RaceStarted event) {
		log.info("race started");
		lapTimes.clear();
	}

	@EventListener
	public synchronized void handle(LapFinished event) {
		log.info("lap {} finished in {}", event.lapNr, format(event.lapTime));
		lapTimes.addValue(event.lapTime.toMillis());
	}

	@EventListener
	public synchronized void handle(RaceFinished event) throws IOException {
		log.info("race with {} laps finished in {}", event.numberOfLaps, format(event.raceTime));
		RaceSummary summary = summarize(event, lapTimes);
		addToHistory(summary);
		eventPublisher.publishEvent(summary);
	}

	@SuppressWarnings("NumericCastThatLosesPrecision")
	private static RaceSummary summarize(RaceFinished event, DescriptiveStatistics lapTimes) {
		Duration bestLap = Duration.ofMillis((long) lapTimes.getMin());
		Duration medLap = Duration.ofMillis((long) lapTimes.getPercentile(50));
		return new RaceSummary(event.numberOfLaps, event.raceTime, bestLap, medLap);
	}

	public List<RaceSummary> getHistory() {
		return ImmutableList.copyOf(history);
	}

	private void addToHistory(RaceSummary summary) throws IOException {
		String json = objectMapper.writeValueAsString(summary);
		Files.write(FULL_HISTORY_FILE, (json+'\n').getBytes(UTF_8), CREATE, APPEND);
		if (history.size() == RECENT_HISTORY_SIZE) history.removeLast();
		history.addFirst(summary);
	}

	@PostConstruct
	private void recoverHistory() throws IOException {
		try (ReversedLinesFileReader reader = new ReversedLinesFileReader(FULL_HISTORY_FILE.toFile(), 4096, UTF_8)) {
			String json;
			while ((json = reader.readLine()) != null && (history.size() < RECENT_HISTORY_SIZE)) {
				RaceSummary summary = objectMapper.readValue(json, RaceSummary.class);
				history.addLast(summary);
			}
		} catch (FileNotFoundException ignored) {
			// no history yet
		}
	}

	private static String format(Duration duration) {
		return String.format(Locale.US, "%.3f seconds", duration.toMillis() / 1000f);
	}
}
