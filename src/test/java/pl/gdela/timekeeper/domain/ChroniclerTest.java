package pl.gdela.timekeeper.domain;

import java.io.IOException;
import java.time.Duration;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import pl.gdela.timekeeper.domain.RaceEvent.LapFinished;
import pl.gdela.timekeeper.domain.RaceEvent.RaceFinished;
import pl.gdela.timekeeper.domain.RaceEvent.RaceStarted;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Chronicler.class)
@AutoConfigureJson
public class ChroniclerTest {

	@ClassRule
	public static TemporaryFolder folder = new TemporaryFolder();

	@Autowired
	private Chronicler chronicler;

	@MockBean
	private EventPublisher eventPublisher;

	private RaceSummary getPublishedSummary() {
		ArgumentCaptor<RaceSummary> argument = ArgumentCaptor.forClass(RaceSummary.class);
		verify(eventPublisher).publishEvent(argument.capture());
		RaceSummary summary = argument.getValue();
		assertThat(summary).overridingErrorMessage("race has not been summarized").isNotNull();
		return summary;
	}

	@Test
	public void chronicler_produces_race_summary() throws IOException {
		// when
		chronicler.handle(new RaceStarted());
		chronicler.handle(new LapFinished(1, Duration.ofMillis(2500)));
		chronicler.handle(new LapFinished(2, Duration.ofMillis(1500)));
		chronicler.handle(new LapFinished(3, Duration.ofMillis(2000)));
		chronicler.handle(new RaceFinished(3, Duration.ofMillis(5999)));

		// then
		RaceSummary summary = getPublishedSummary();
		assertThat(summary.numberOfLaps).isEqualTo(3);
		assertThat(summary.raceTime).isEqualTo(Duration.ofMillis(5999));
		assertThat(summary.bestLap).isEqualTo(Duration.ofMillis(1500));
		assertThat(summary.medLap).isEqualTo(Duration.ofMillis(2000));
	}

	@Test
	public void race_restart_clears_statistics() throws IOException {
		// when
		chronicler.handle(new RaceStarted());
		chronicler.handle(new LapFinished(1, Duration.ofMillis(111)));
		chronicler.handle(new LapFinished(2, Duration.ofMillis(222)));
		chronicler.handle(new RaceStarted());
		chronicler.handle(new LapFinished(1, Duration.ofMillis(333)));
		chronicler.handle(new RaceFinished(1, Duration.ofMillis(333)));

		// then
		RaceSummary summary = getPublishedSummary();
		assertThat(summary.numberOfLaps).isEqualTo(1);
		assertThat(summary.bestLap).isEqualTo(Duration.ofMillis(333));
		assertThat(summary.medLap).isEqualTo(Duration.ofMillis(333));
	}

	// todo: test recovery of history, move races.json file name to application.properties
	// so that here we can use a separate file to do the integration testing
}
