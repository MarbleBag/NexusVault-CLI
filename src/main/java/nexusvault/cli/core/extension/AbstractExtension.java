package nexusvault.cli.core.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import nexusvault.cli.core.App;
import nexusvault.cli.core.CommandLineManager;
import nexusvault.cli.core.Console.Level;
import nexusvault.cli.core.EventManager;
import nexusvault.cli.core.cmd.ArgumentHandler;
import nexusvault.cli.core.cmd.CommandHandler;

public abstract class AbstractExtension implements Extension {

	public interface InitializationHelper {
		void addCommandHandler(CommandHandler cmd);

		void addArgumentHandler(ArgumentHandler arg);

		void addEventListener(Object listener);

		<T> T loadConfig(Class<T> clazz);
	}

	private final List<CommandHandler> cmds = new ArrayList<>();
	private final List<ArgumentHandler> arguments = new ArrayList<>();
	private final List<Object> eventListeners = new ArrayList<>();

	private App app;

	protected AbstractExtension() {

	}

	protected abstract void initializeExtension(InitializationHelper initializationHelper);

	protected abstract void deinitializeExtension();

	protected App getApp() {
		return this.app;
	}

	@Override
	public final void initialize(App app) {
		this.app = app;

		initializeExtension(new InitializationHelper() {
			@Override
			public void addCommandHandler(CommandHandler cmd) {
				if (cmd == null) {
					throw new IllegalArgumentException("'cmd' was null");
				}
				AbstractExtension.this.cmds.add(cmd);
			}

			@Override
			public void addArgumentHandler(ArgumentHandler arg) {
				if (arg == null) {
					throw new IllegalArgumentException("'arg' was null");
				}
				AbstractExtension.this.arguments.add(arg);
			}

			@Override
			public void addEventListener(Object listener) {
				if (listener == null) {
					throw new IllegalArgumentException("'listener' was null");
				}
				AbstractExtension.this.eventListeners.add(listener);
			}

			@Override
			public <T> T loadConfig(Class<T> clazz) {
				// TODO Auto-generated method stub
				return null;
			}
		});

		if (this.cmds == null) {
			throw new IllegalStateException("Commands are not initialized, use 'setCommands' on initialization");
		}

		if (this.arguments == null) {
			throw new IllegalStateException("Arguments are not initialized, use 'setArguments' on initialization");
		}

		final CommandLineManager cli = this.app.getCLI();

		for (final var arg : this.arguments) {
			cli.registerArgument(arg);
		}

		for (final var cmd : this.cmds) {
			cli.registerCommand(cmd);
		}

		if (this.eventListeners != null) {
			final EventManager eventSystem = this.app.getEventSystem();
			for (final Object eventListener : this.eventListeners) {
				eventSystem.registerEventHandler(eventListener);
			}
		}
	}

	@Override
	public final void deinitialize() {

		deinitializeExtension();

		final CommandLineManager cli = this.app.getCLI();
		for (final CommandHandler cmd : this.cmds) {
			cli.unregisterCommand(cmd);
		}
		this.cmds.clear();
		this.arguments.clear();

		if (this.eventListeners != null) {
			final EventManager eventSystem = this.app.getEventSystem();
			for (final Object eventListener : this.eventListeners) {
				eventSystem.unregisterEventHandler(eventListener);
			}
		}
		this.eventListeners.clear();
	}

	protected final void sendMsg(String msg) {
		this.app.getConsole().println(Level.CONSOLE, msg);
	}

	protected final void sendMsg(Supplier<String> msg) {
		this.app.getConsole().println(Level.CONSOLE, msg);
	}

	protected final void sendDebug(Supplier<String> msg) {
		this.app.getConsole().println(Level.DEBUG, msg);
	}

}
