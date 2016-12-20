package pl.gdela.timekeeper.domain;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TimekeeperTest {

	@InjectMocks
	private Timekeeper timekeeper;

	@Mock
	private EventPublisher eventPublisher;

	@Test
	public void smoke_test() {
		// given
		timekeeper.requestRaceStart(2);

		// when
		timekeeper.photocellInterrupted();
		timekeeper.photocellInterrupted();

		// then
		ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
		verify(eventPublisher, atLeast(1)).publishEvent(argument.capture());
		List<Object> events = argument.getAllValues();
		assertThat(events.get(0)).isInstanceOf(RaceEvent.Countdown.class);
		assertThat(events.get(1)).isInstanceOf(RaceEvent.Countdown.class);
		assertThat(events.get(2)).isInstanceOf(RaceEvent.Countdown.class);
		assertThat(events.get(3)).isInstanceOf(RaceEvent.RaceStarted.class);
		assertThat(events.get(4)).isInstanceOf(RaceEvent.LapFinished.class);
		assertThat(events.get(5)).isInstanceOf(RaceEvent.LapFinished.class);
		assertThat(events.get(6)).isInstanceOf(RaceEvent.RaceFinished.class);
	}

	@Test
	public void crossing_line_after_race_finished_is_ignored() {
		// given
		timekeeper.requestRaceStart(2);
		timekeeper.photocellInterrupted();
		timekeeper.photocellInterrupted();

		// when
		timekeeper.photocellInterrupted();

		// then - no exception
	}

	// todo: test photocell interruption: 1. before race start 2. during count down, 3. after race finished
}
