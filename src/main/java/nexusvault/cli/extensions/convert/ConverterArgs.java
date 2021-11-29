package nexusvault.cli.extensions.convert;

import java.util.Collections;
import java.util.Map;

import nexusvault.cli.core.cmd.Arguments;

public final class ConverterArgs {

	private final Arguments args;
	private final Map<String, Object> fallbacks;

	public ConverterArgs() {
		this(null, null);
	}

	public ConverterArgs(Arguments args) {
		this(args, null);
	}

	public ConverterArgs(Map<String, Object> fallbacks) {
		this(null, fallbacks);
	}

	public ConverterArgs(Arguments args, Map<String, Object> fallbacks) {
		this.args = args;
		this.fallbacks = fallbacks != null ? fallbacks : Collections.emptyMap();
	}

	public boolean has(String key) {
		return (this.args != null ? this.args.isNamedArgumentSet(key) : false) || this.fallbacks.containsKey(key);
	}

	public String get(String key) {
		if (this.args != null) {
			if (this.args.isNamedArgumentSet(key)) {
				return this.args.getArgumentByName(key).getValue();
			}
		}

		final var value = this.fallbacks.get(key);
		if (value == null) {
			return null;
		}

		if (value instanceof String[]) {
			final var array = (String[]) value;
			return array.length > 0 ? array[0] : null;
		}

		return value.toString();
	}

	public String getOrElse(String key, String def) {
		final var value = get(key);
		if (value == null) {
			return def;
		}
		return value;
	}

	public String[] gets(String key) {
		if (this.args != null) {
			if (this.args.isNamedArgumentSet(key)) {
				return this.args.getArgumentByName(key).getValues();
			}
		}

		final var value = this.fallbacks.get(key);
		if (value == null) {
			return null;
		}

		if (value instanceof String[]) {
			return (String[]) value;
		}

		return new String[] { value.toString() };
	}
}
