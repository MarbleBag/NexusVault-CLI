package nexusvault.cli.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import nexusvault.cli.App;
import nexusvault.cli.CLISystem;
import nexusvault.cli.Command;
import nexusvault.cli.ConsoleSystem.Level;
import nexusvault.cli.EventSystem;
import nexusvault.cli.PlugIn;

public abstract class AbstPlugIn implements PlugIn {

	private List<Command> cmds;
	private List<Object> eventListeners;

	protected AbstPlugIn() {

	}

	protected void setCommands(List<Command> cmds) {
		if (this.cmds != null) {
			throw new IllegalStateException("Commands already initialized");
		}

		if (cmds == null) {
			this.cmds = Collections.emptyList();
		} else {
			this.cmds = Collections.unmodifiableList(new ArrayList<>(cmds));
		}
	}

	protected void setEventListener(List<Object> listener) {
		if (this.eventListeners != null) {
			throw new IllegalStateException("EventListener already initialized");
		}

		if (listener == null) {
			this.eventListeners = Collections.emptyList();
		} else {
			this.eventListeners = Collections.unmodifiableList(new ArrayList<>(listener));
		}
	}

	@Override
	public void initialize() {
		if (cmds == null) {
			throw new IllegalStateException("Commands are not initialised, use 'setCommands' after PlugIn construction");
		}

		final CLISystem cli = App.getInstance().getCLI();
		for (final Command cmd : cmds) {
			cli.registerCommand(cmd);
		}

		if (eventListeners != null) {
			final EventSystem eventSystem = App.getInstance().getEventSystem();
			for (final Object eventListener : eventListeners) {
				eventSystem.registerEventHandler(eventListener);
			}
		}
	}

	@Override
	public void deinitialize() {

		final CLISystem cli = App.getInstance().getCLI();
		for (final Command cmd : cmds) {
			cli.unregisterCommand(cmd);
		}

		if (eventListeners != null) {
			final EventSystem eventSystem = App.getInstance().getEventSystem();
			for (final Object eventListener : eventListeners) {
				eventSystem.unregisterEventHandler(eventListener);
			}
		}
	}

	protected final void sendMsg(String msg) {
		App.getInstance().getConsole().println(Level.CONSOLE, msg);
	}

	protected final void sendMsg(Supplier<String> msg) {
		App.getInstance().getConsole().println(Level.CONSOLE, msg);
	}

	protected final void sendDebug(Supplier<String> msg) {
		App.getInstance().getConsole().println(Level.DEBUG, msg);
	}

}
