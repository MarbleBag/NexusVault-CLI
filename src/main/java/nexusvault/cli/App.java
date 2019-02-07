package nexusvault.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.common.reflect.Reflection;

import nexusvault.cli.ConsoleSystem.Level;
import nexusvault.cli.model.ModelPropertyChangedEvent;
import nexusvault.cli.plugin.config.AppConfigModel;

public final class App {

	private static Path getProjectLocation() {
		try {
			final Path currentLocation = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			return currentLocation.getParent();
		} catch (final URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

	private static App app;

	public static App getInstance() {
		return app;
	}

	private AppConfigModel appConfig;
	private EventSystem eventSystem;
	private PlugInSystem plugInSystem;
	private CLISystem cliSystem;
	private ConsoleSystem console;
	@Deprecated
	private ModelSystem modelSystem;

	private CommandManager commandManager;

	private boolean processConsole;

	public App() {
		if (app != null) {
			throw new InstantiationError("App already instantiated");
		}
		app = this;
	}

	public EventSystem getEventSystem() {
		return eventSystem;
	}

	public AppConfigModel getAppConfig() {
		return appConfig;
	}

	public CLISystem getCLI() {
		return cliSystem;
	}

	public PlugInSystem getPlugInSystem() {
		return plugInSystem;
	}

	public ConsoleSystem getConsole() {
		return console;
	}

	@Deprecated
	public ModelSystem getModelSystem() {
		return modelSystem;
	}

	// shortcut
	public <T extends PlugIn> T getPlugIn(Class<T> plugInClass) {
		return plugInSystem.getPlugIn(plugInClass);
	}

	public void initializeApp(boolean headlessMode) throws IOException {
		initializeConsole(headlessMode);
		initializeModel(headlessMode);
		initializeEventSystem();
		initializeCLI();

		initializePlugIns();

		// loadConfig();

		if (!headlessMode) {
			getConsole().println(Level.CONSOLE, "Console mode: Enter 'help' to get a list of available commands."); // TODO
		}
	}

	private void initializeConsole(boolean headlessMode) {
		final PrintWriter reportTo = new PrintWriter(System.out);
		final BaseConsoleSystem.MsgHandle handle = (msg) -> {
			if (msg.lineEnd) {
				reportTo.println(msg.msg);
			} else {
				reportTo.print(msg.msg);
			}
			reportTo.flush();
		};
		final BaseConsoleSystem console = new BaseConsoleSystem(handle);
		console.setHeadlessMode(headlessMode);
		this.console = console;
	}

	private void initializeCLI() {
		commandManager = new CommandManager();
		cliSystem = new CLISystem() {
			@Override
			public void registerCommand(Command cmd) {
				commandManager.registerCommand(cmd);
			}

			@Override
			public void unregisterCommand(Command cmd) {
				commandManager.unregisterCommand(cmd);
			}

			@Override
			public void printHelp() {
				final PrintHelpContext context = new PrintHelpContext("nexusvault", new PrintWriter(System.out));

				final String header = "Tool to extract data from the wildstar game archive. Some commands are only available in console-mode and can be entered without '--'. To close the app in console-mode enter 'exit'. A command followed by '?' will, if available, show a command specific help.";
				context.setHeader(header);

				final String footer = "footer";
				context.setFooter(footer);

				context.setWidth(120);

				// TODO
				commandManager.printHelp(context);
			}
		};

		commandManager.registerCommand(new ExitCmd((args) -> this.requestShutDown()));
		commandManager.registerCommand(new HelpCmd((args) -> this.cliSystem.printHelp()));
		commandManager.registerCommand(new HeadlessModeCmd());
	}

	@Deprecated
	private void initializeModel(boolean headlessMode) {
		modelSystem = new BaseModelSystem();

		appConfig = new AppConfigModel();
		appConfig.setApplicationPath(getProjectLocation());
		appConfig.setDebugMode(false);
		appConfig.setHeadlessMode(headlessMode);

		modelSystem.registerModel(AppConfigModel.class, appConfig);
	}

	private void initializeEventSystem() {
		// TODO
		eventSystem = new EventBusSystem(new SubscriberExceptionHandler() {
			@Override
			public void handleException(Throwable exception, SubscriberExceptionContext context) {
				// TODO Auto-generated method stub
				throw new RuntimeException(exception);
			}
		});

		eventSystem.registerEventHandler(new AppConfigObserver()); // TODO
	}

	private static final class AppConfigObserver {
		@Subscribe
		public void onAppConfigChangedEvent(ModelPropertyChangedEvent<?> event) {
			// TODO
			App.getInstance().getConsole().println(Level.CONSOLE, "Set " + event);
		}
	}

	private void initializePlugIns() throws IOException {
		plugInSystem = new BasePlugInProvider();

		final List<PlugIn> objs = initializeComponents("nexusvault.cli.plugin", PlugIn.class);
		for (final PlugIn obj : objs) {
			plugInSystem.registerPlugIn(obj.getClass(), obj);
		}

		for (final PlugIn obj : objs) {
			obj.initialize();
		}

		console.println(Level.DEBUG, "Plugin: " + objs.size() + " plugin(s) found.");
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> initializeComponents(String packageName, Class<? extends T> scanForClass) throws IOException {
		final ClassPath classPathScanner = ClassPath.from(this.getClass().getClassLoader());
		final ImmutableSet<ClassInfo> allClasses = classPathScanner.getTopLevelClassesRecursive(packageName);
		final Class<?>[] filteredClasses = allClasses.stream().map(i -> i.load()).filter(c -> scanForClass.isAssignableFrom(c))
				.filter(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers())).toArray(Class<?>[]::new);

		Reflection.initialize(filteredClasses);

		final T[] objects = (T[]) Array.newInstance(scanForClass, filteredClasses.length);

		for (int i = 0; i < filteredClasses.length; i++) {
			final Class<T> clazz = (Class<T>) filteredClasses[i];
			objects[i] = constructClazz(clazz);
		}

		return Arrays.asList(objects);
	}

	private <T> T constructClazz(Class<? extends T> clazz) {
		try {
			final Constructor<? extends T> constructor = clazz.getDeclaredConstructor();
			final boolean wasAccessible = constructor.isAccessible();
			constructor.setAccessible(true);
			final T obj = constructor.newInstance();
			constructor.setAccessible(wasAccessible);
			return obj;
		} catch (InstantiationException | NoSuchMethodException | IllegalAccessException | SecurityException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new IllegalStateException("Unable to instantiate classes", e);
		}
	}

	public void startApp(String[] startUpArgs) {
		commandManager.runArguments(startUpArgs);
		// TODO
		processConsole();
	}

	public void closeApp() {

		saveAppConfig();
		savePlugInConfigs();

		this.console = null;
		this.modelSystem = null;
		this.eventSystem = null;
		this.commandManager = null;
		this.cliSystem = null;
		this.plugInSystem = null;
		this.appConfig = null;
		// TODO
	}

	private void saveAppConfig() {
		// TODO Auto-generated method stub

	}

	private void savePlugInConfigs() {
		// TODO Auto-generated method stub

	}

	private void processConsole() {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line = null;

		processConsole = true; // TODO
		while (processConsole) {
			try {
				line = reader.readLine();
				if (line == null) {
					line = "exit";
				}
				line = line.trim();

				if (line.length() > 0) {
					if (!line.startsWith("-") && !line.startsWith("--")) {
						line = "--" + line;
					}
				}

				final String[] arguments = commandManager.parseArguments(line);
				commandManager.runArguments(arguments);

			} catch (final CommandFormatException e1) {
				console.println(Level.ERROR, e1 + ":" + e1.getMessage());
				// if (appProperties.isDebugMode()) { //TODO
				// e1.printStackTrace();
				// }
			} catch (final Exception e2) {
				console.println(Level.ERROR, e2 + ":" + e2.getMessage());
				// if (appProperties.isDebugMode()) { //TODO
				// e2.printStackTrace();
				// }
			}
		}

		console.println(Level.CONSOLE, "Closing app");
	}

	public void requestShutDown() {
		processConsole = false;
	}

}
