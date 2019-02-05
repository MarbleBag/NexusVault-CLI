package nexusvault.cli;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionHandler;

final class EventBusSystem implements EventSystem {

	private final EventBus eventBus;

	public EventBusSystem(SubscriberExceptionHandler handler) {
		eventBus = new EventBus(handler);
	}

	@Override
	public void postEvent(Object event) {
		eventBus.post(event);
	}

	@Override
	public void registerEventHandler(Object handler) {
		eventBus.register(handler);
	}

	@Override
	public boolean unregisterEventHandler(Object handler) {
		try {
			eventBus.unregister(handler);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

}