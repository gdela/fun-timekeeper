package pl.gdela.timekeeper.domain;

import org.junit.Test;
import pl.gdela.timekeeper.domain.RaceEvent.RaceStarted;

import static org.assertj.core.api.Assertions.assertThat;


public class RaceEventTest {

	@Test
	public void event_name_is_derived_from_class_name() {
		RaceStarted raceStartedEvent = new RaceStarted();
		assertThat(raceStartedEvent.type()).isEqualTo("race-started");
	}

}
