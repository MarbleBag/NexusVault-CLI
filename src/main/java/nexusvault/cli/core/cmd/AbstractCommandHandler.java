package nexusvault.cli.core.cmd;

import com.google.common.base.Supplier;

import nexusvault.cli.App;
import nexusvault.cli.ConsoleSystem.Level;

public abstract class AbstractCommandHandler implements CommandHandler {

	protected void sendMsg(String msg) {
		App.getInstance().getConsole().println(Level.CONSOLE, msg);
	}

	protected void sendMsg(Supplier<String> msg) {
		App.getInstance().getConsole().println(Level.CONSOLE, msg);
	}

	protected void sendDebug(String msg) {
		App.getInstance().getConsole().println(Level.DEBUG, msg);
	}

	protected void sendDebug(Supplier<String> msg) {
		App.getInstance().getConsole().println(Level.DEBUG, msg);
	}

}
