package nexusvault.cli;

import java.util.function.Supplier;

public interface ConsoleSystem {
	public static enum Level {
		CONSOLE,
		DEBUG,
		ERROR
	}

	boolean accepts(Level level);

	void print(Level level, String msg);

	void println(Level level, String msg);

	void print(Level level, Supplier<String> msg);

	void println(Level level, Supplier<String> msg);
}
