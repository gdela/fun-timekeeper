package pl.gdela.timekeeper.application;

import javax.annotation.PostConstruct;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.gdela.timekeeper.domain.Timekeeper;

import static com.pi4j.io.gpio.PinPullResistance.PULL_UP;
import static com.pi4j.io.gpio.PinState.LOW;
import static com.pi4j.io.gpio.RaspiBcmPin.GPIO_02;

@Component
@Profile("!dev")
public class GpioIntegration {

	private static final Logger log = LoggerFactory.getLogger(GpioIntegration.class);

	@Autowired
	private Timekeeper timekeeper;

	@PostConstruct
	private void integrate() {
		GpioController gpio = GpioFactory.getInstance();
		GpioPinDigitalInput photocell = gpio.provisionDigitalInputPin(GPIO_02, "Photocell", PULL_UP);
		photocell.setDebounce(1000);
		photocell.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				if (event.getState() == LOW) {
					timekeeper.photocellInterrupted();
				}
			}
		});
		log.info("connected to photocell on gpio {}", photocell);
	}
}
