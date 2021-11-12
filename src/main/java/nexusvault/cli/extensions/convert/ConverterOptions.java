package nexusvault.cli.extensions.convert;

import java.util.HashMap;
import java.util.Map;

public final class ConverterOptions {
	private final Map<String, Object> config = new HashMap<>();

	public ConverterOptions() {

	}

	public ConverterOptions(Map<String, Object> initialValues) {
		this.config.putAll(initialValues);
	}

	public <T> T get(String key) {
		final var value = this.config.get(key);
		if (value == null) {
			throw new ConverterException(String.format("Option '%s' not available", key));
		}
		return (T) value;
	}

	public <T> T getOrElse(String key, T def) {
		final var value = this.config.get(key);
		if (value == null) {
			return def;
		}
		return (T) value;
	}

	public void set(String key, Object value) {
		this.config.put(key, value);
	}

	public boolean has(String string) {
		// TODO Auto-generated method stub
		return false;
	}
}
