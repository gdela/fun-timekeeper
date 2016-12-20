package pl.gdela.timekeeper.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pl.gdela.timekeeper.domain.RaceEvent;
import pl.gdela.timekeeper.domain.Timekeeper;

@RestController
public class TimekeeperController {

	private static final Logger log = LoggerFactory.getLogger(TimekeeperController.class);

	@Autowired
	private Timekeeper timekeeper;

	@Autowired
	private SimpMessagingTemplate messaging;

	@MessageMapping("/request-race-start")
	private void requestRaceStart() {
		log.info("race start requested");
		timekeeper.requestRaceStart(5);
	}

	/**
	 * Forwards events from domain layer to user interface.
	 */
	@EventListener
	private void forwardEvent(RaceEvent event) {
		messaging.convertAndSend("/topic/" + event.type(), event);
	}

	/**
	 * Simulates interruption of the photocell. On production a real photocell
	 * is used, see {@link GpioIntegration}.
	 */
	@RequestMapping(path = "interrupt-photocell", method = RequestMethod.POST)
	private void interruptPhotocell() {
		timekeeper.photocellInterrupted();
	}
}
