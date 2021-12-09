package nexusvault.cli.core;

public interface EventManager {
	void postEvent(Object event);

	void registerEventHandler(Object handler);

	boolean unregisterEventHandler(Object handle);
}