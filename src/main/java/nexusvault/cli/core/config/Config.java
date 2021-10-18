package nexusvault.cli.core.config;

import java.util.function.Function;

public abstract class Config {

	public abstract class ConfigStore {

	}

	abstract public void

	abstract public void setValue(String key, Object value);

	abstract public Object getValue(String key, Object fallback);

	abstract public Object getValue(String key, Function<String, Object> fallback);

	abstract public <T> T getValue(String key, Class<T> clazz, T fallback);

	abstract public <T> T getValue(String key, Class<T> clazz, Function<String, T> fallback);

}
