package pl.gdela.timekeeper.domain;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Short summary of a whole race.
 */
public class RaceSummary {

	public final int numberOfLaps;
	public final Duration raceTime;
	public final Duration bestLap;
	public final Duration medLap;

	public RaceSummary(
			@JsonProperty("numberOfLaps") int numberOfLaps,
			@JsonProperty("raceTime") Duration raceTime,
			@JsonProperty("bestLap") Duration bestLap,
			@JsonProperty("medLap") Duration medLap)
	{
		this.numberOfLaps = numberOfLaps;
		this.raceTime = checkNotNull(raceTime);
		this.bestLap = checkNotNull(bestLap);
		this.medLap = checkNotNull(medLap);
	}
}
