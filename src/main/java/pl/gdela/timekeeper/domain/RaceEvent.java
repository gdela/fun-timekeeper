package pl.gdela.timekeeper.domain;


import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.StringUtils;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.getCommonPrefix;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

@JsonPropertyOrder("type")
public class RaceEvent {

	public static class Countdown extends RaceEvent {
		public final int timeToStart;
		public Countdown(int timeToStart) {
			this.timeToStart = timeToStart;
		}
	}

	public static class RaceStarted extends RaceEvent {

	}

	public static class FalseStart extends RaceEvent {

	}

	public static class LapFinished extends RaceEvent {
		public final int lapNr;
		public final Duration lapTime;
		public LapFinished(int lapNr, Duration lapTime) {
			this.lapNr = lapNr;
			this.lapTime = lapTime;
		}
	}

	public static class RaceFinished extends RaceEvent {
		public final int numberOfLaps;
		public final Duration raceTime;
		public RaceFinished(int numberOfLaps, Duration raceTime) {
			this.numberOfLaps = numberOfLaps;
			this.raceTime = raceTime;
		}
	}

	@JsonProperty("type")
	public String type() {
		String[] words = splitByCharacterTypeCamelCase(getClass().getSimpleName());
		return stream(words).map(String::toLowerCase).collect(joining("-"));
	}

	@Override
	public boolean equals(Object that) {
		return reflectionEquals(this, that);
	}

	@Override
	public int hashCode() {
		return reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}
}
