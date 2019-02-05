package nexusvault.cli;

public interface EventSystem {
	void postEvent(Object event);

	void registerEventHandler(Object handler);

	boolean unregisterEventHandler(Object handle);
}