package nexusvault.cli;

import java.util.function.Supplier;

final class BaseConsoleSystem implements ConsoleSystem {

	public static interface MsgHandle {
		void handle(Msg msg);
	}

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

	private final MsgHandle handle;
	private boolean headlessMode;
	private boolean debugMode;

	public BaseConsoleSystem(MsgHandle handle) {
		if (handle == null) {
			throw new IllegalArgumentException();
		}
		this.handle = handle;
	}

	@Override
	public boolean accepts(Level level) {
		switch (level) {
			case CONSOLE:
				return !headlessMode;
			case DEBUG:
				return debugMode && !headlessMode;
		}
		return false;
	}

	public void setHeadlessMode(boolean value) {
		this.headlessMode = value;
	}

	public boolean isHeadlessMode() {
		return headlessMode;
	}

	public void setDebugMode(boolean value) {
		this.debugMode = value;
	}

	public boolean isDebugMode() {
		return debugMode;
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
		handle.handle(msg);
	}

}
