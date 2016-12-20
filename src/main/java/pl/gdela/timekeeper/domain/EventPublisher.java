package pl.gdela.timekeeper.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Publisher of domain events. This is just a tiny wrapper over spring's {@link ApplicationEventPublisher}
 * that is perfect for implementing domain events pattern, but which name is confusing,
 * as it suggest it's just for application layer events. Could have also used guava's
 * <a href="https://github.com/google/guava/wiki/EventBusExplained">Event Bus</a>.
 */
@Service
public class EventPublisher {

	private final ApplicationEventPublisher delegatee;

	@Autowired
	public EventPublisher(ApplicationEventPublisher delegatee) {
		this.delegatee = checkNotNull(delegatee);
	}

	public void publishEvent(Object object) {
		delegatee.publishEvent(object);
	}
}
