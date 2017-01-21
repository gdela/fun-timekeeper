package pl.gdela.timekeeper.application;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pl.gdela.timekeeper.domain.Chronicler;
import pl.gdela.timekeeper.domain.RaceEvent;
import pl.gdela.timekeeper.domain.RaceSummary;
import pl.gdela.timekeeper.domain.Timekeeper;

/**
 * Backend controller of the application.
 */
@RestController
public class AppController {

	private static final Logger log = LoggerFactory.getLogger(AppController.class);

	@Autowired
	private Timekeeper timekeeper;

	@Autowired
	private Chronicler chronicler;

	@Autowired
	private SimpMessagingTemplate messaging;

	@MessageMapping("/request-race-start")
	private void requestRaceStart() {
		log.info("race start requested");
		timekeeper.requestRaceStart(5);
	}

	@SubscribeMapping("/get-history")
	private List<RaceSummary> getHistory() {
		return chronicler.getHistory();
	}

	/**
	 * Simulates interruption of the photocell. On production a real photocell
	 * is used, see {@link GpioIntegration}.
	 */
	@RequestMapping(path = "interrupt-photocell", method = RequestMethod.POST)
	private void interruptPhotocell() {
		log.info("photocell interruption simulated");
		timekeeper.photocellInterrupted();
	}

	/**
	 * Forwards events from domain layer to user interface.
	 */
	@EventListener
	private void forwardEvent(RaceEvent event) {
		messaging.convertAndSend("/topic/" + event.type(), event);
	}

	@EventListener
	private void forwardEvent(RaceSummary event) {
		messaging.convertAndSend("/topic/race-summary", event);
	}
}
