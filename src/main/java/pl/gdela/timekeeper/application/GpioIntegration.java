package pl.gdela.timekeeper.application;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.gdela.timekeeper.domain.RaceEvent.Countdown;
import pl.gdela.timekeeper.domain.RaceEvent.RaceFinished;
import pl.gdela.timekeeper.domain.RaceEvent.RaceStarted;
import pl.gdela.timekeeper.domain.Timekeeper;

import static com.pi4j.io.gpio.PinPullResistance.PULL_UP;
import static com.pi4j.io.gpio.PinState.LOW;
import static com.pi4j.io.gpio.RaspiBcmPin.GPIO_02;
import static com.pi4j.io.gpio.RaspiBcmPin.GPIO_28;

/**
 * Bi-directional integration between domain logic and physical devices: photocell and lights.
 */
@Component
@Profile("live")
public class GpioIntegration {

	private static final Logger log = LoggerFactory.getLogger(GpioIntegration.class);

	@Autowired
	private Timekeeper timekeeper;

	private GpioController gpio;
	private GpioPinDigitalOutput lights;
	private GpioPinDigitalInput photocell;

	@PostConstruct
	private void integrate() {
		gpio = GpioFactory.getInstance();
		lights = gpio.provisionDigitalOutputPin(GPIO_28, "Lights", LOW);
		photocell = gpio.provisionDigitalInputPin(GPIO_02, "Photocell", PULL_UP);
		photocell.setDebounce(1000);
		photocell.addListener(onPhotocellEvent());
		log.info("connected to photocell on gpio {}", photocell);
	}

	@PreDestroy
	private void disconnect() {
		gpio.shutdown();
	}

	private GpioPinListenerDigital onPhotocellEvent() {
		return event -> {
			if (event.getState() == LOW) {
				timekeeper.photocellInterrupted();
			}
		};
	}

	@EventListener
	public void onRaceEvent(Countdown event) {
		lights.low();
	}

	@EventListener
	public void onRaceEvent(RaceStarted event) {
		lights.high();
	}

	@EventListener
	public void onRaceEvent(RaceFinished event) {
		lights.low();
	}
}
