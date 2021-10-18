package nexusvault.cli.core;

import java.io.PrintWriter;
import java.util.function.Supplier;

final class BaseConsoleManager implements Console {

	public static final class Msg {
		public final Level level;
		public final String msg;
		public final boolean lineEnd;

		public Msg(Level level, String msg, boolean lineEnd) {
			super();
			this.level = level;
			this.msg = msg;
			this.lineEnd = lineEnd;
		}
	}

	private boolean headlessMode;
	private boolean debugMode;

	private final App app;
	private final PrintWriter systemConsole;

	public BaseConsoleManager(App app) {
		if (app == null) {
			throw new IllegalArgumentException();
		}
		this.app = app;

		this.systemConsole = new PrintWriter(System.out, true);
	}

	@Override
	public boolean accepts(Level level) {
		switch (level) {
			case CONSOLE:
				return !this.headlessMode;
			case DEBUG:
				return this.debugMode && !this.headlessMode;
		}
		return false;
	}

	public void setHeadlessMode(boolean value) {
		this.headlessMode = value;
	}

	public boolean isHeadlessMode() {
		return this.headlessMode;
	}

	public void setDebugMode(boolean value) {
		this.debugMode = value;
	}

	public boolean isDebugMode() {
		return this.debugMode;
	}

	@Override
	public void print(Level level, Supplier<String> msg) {
		if (accepts(level)) {
			sendMsg(new Msg(level, msg.get(), false));
		}
	}

	@Override
	public void println(Level level, Supplier<String> msg) {
		if (accepts(level)) {
			sendMsg(new Msg(level, msg.get(), true));
		}
	}

	@Override
	public void print(Level level, String msg) {
		if (accepts(level)) {
			sendMsg(new Msg(level, msg, false));
		}
	}

	@Override
	public void println(Level level, String msg) {
		if (accepts(level)) {
			sendMsg(new Msg(level, msg, true));
		}
	}

	private void sendMsg(Msg msg) {
		if (msg.lineEnd) {
			this.systemConsole.println(msg.msg);
		} else {
			this.systemConsole.print(msg.msg);
		}
	}

	@Override
	public PrintWriter getWriter(Level level) {
		return new PrintWriter(this.systemConsole);
	}

}
