package nexusvault.cli.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import nexusvault.cli.App;
import nexusvault.cli.CLISystem;
import nexusvault.cli.ConsoleSystem.Level;
import nexusvault.cli.EventSystem;
import nexusvault.cli.PlugIn;
import nexusvault.cli.core.cmd.ArgumentHandler;
import nexusvault.cli.core.cmd.CommandHandler;

public abstract class AbstractPlugIn implements PlugIn {

	private List<CommandHandler> cmds;
	private List<ArgumentHandler> arguments;
	private List<Object> eventListeners;

	protected AbstractPlugIn() {

	}

	protected void setNoCommands() {
		setCommands((List<CommandHandler>) null);
	}

	protected void setNoArguments() {
		setArguments((List<ArgumentHandler>) null);
	}

	protected void setArguments(ArgumentHandler... arguments) {
		setArguments(Arrays.asList(arguments));
	}

	protected void setArguments(List<ArgumentHandler> arguments) {
		if (this.arguments != null) {
			throw new IllegalStateException("Arguments already initialized");
		}

		if (arguments == null) {
			this.arguments = Collections.emptyList();
		} else {
			this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
		}
	}

	protected void setCommands(CommandHandler... cmds) {
		setCommands(Arrays.asList(cmds));
	}

	protected void setCommands(List<CommandHandler> cmds) {
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
		if (this.cmds == null) {
			throw new IllegalStateException("Commands are not initialised, use 'setCommands' after PlugIn construction");
		}

		if (this.arguments == null) {
			throw new IllegalStateException("Arguments are not initialised, use 'setArguments' after PlugIn construction");
		}

		final CLISystem cli = App.getInstance().getCLI();

		for (final var arg : this.arguments) {
			cli.registerStartArgumentHandler(arg);
		}

		for (final var cmd : this.cmds) {
			cli.registerCommand(cmd);
		}

		if (this.eventListeners != null) {
			final EventSystem eventSystem = App.getInstance().getEventSystem();
			for (final Object eventListener : this.eventListeners) {
				eventSystem.registerEventHandler(eventListener);
			}
		}
	}

	@Override
	public void deinitialize() {

		final CLISystem cli = App.getInstance().getCLI();
		for (final CommandHandler cmd : this.cmds) {
			cli.unregisterCommand(cmd);
		}

		if (this.eventListeners != null) {
			final EventSystem eventSystem = App.getInstance().getEventSystem();
			for (final Object eventListener : this.eventListeners) {
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
