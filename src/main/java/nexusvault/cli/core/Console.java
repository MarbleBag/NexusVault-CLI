package nexusvault.cli.core;

import java.io.PrintWriter;
import java.util.function.Supplier;

public interface Console {
	public static enum Level {
		CONSOLE,
		DEBUG,
	}

	boolean accepts(Level level);

	void print(Level level, String msg);

	void println(Level level, String msg);

	void print(Level level, Supplier<String> msg);

	void println(Level level, Supplier<String> msg);

	PrintWriter getWriter(Level level);
}
