package nexusvault.cli.model;

public abstract class ModelPropertyChangedEvent<T> {
	private final String eventName;
	private final T oldValue;
	private final T newValue;

	protected ModelPropertyChangedEvent(String eventName, T oldValue, T newValue) {
		if (eventName == null) {
			throw new IllegalArgumentException("'eventName' must not be null");
		}
		this.eventName = eventName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public T getOldValue() {
		return oldValue;
	}

	public T getNewValue() {
		return newValue;
	}

	public String getEventName() {
		return eventName;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(getEventName()).append("[");
		builder.append("oldValue=").append(getOldValue());
		builder.append(", newValue=").append(getNewValue());
		builder.append("]");
		return builder.toString();
	}

}