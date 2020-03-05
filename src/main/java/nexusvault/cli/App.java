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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.common.reflect.Reflection;

import nexusvault.cli.ConsoleSystem.Level;
import nexusvault.cli.core.cmd.ArgumentHandler;
import nexusvault.cli.core.cmd.ArgumentManager;
import nexusvault.cli.core.cmd.ArgumentParser;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandFormatException;
import nexusvault.cli.core.cmd.CommandHandler;
import nexusvault.cli.core.cmd.CommandManager;
import nexusvault.cli.model.ModelPropertyChangedEvent;
import nexusvault.cli.plugin.config.AppConfigModel;
import nexusvault.cli.plugin.config.AppConfigModel.AppConfigAppPathChangedEvent;
import nexusvault.cli.plugin.config.AppConfigModel.AppConfigDebugModeChangedEvent;

public final class App {

	private static Path getProjectLocation() {
		try {
			final Path currentLocation = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			return currentLocation.getParent();
		} catch (final URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

	private final static Logger logger = LogManager.getLogger(App.class);

	private static App app;

	public static App getInstance() {
		return app;
	}

	private AppConfigModel appConfig;
	private EventSystem eventSystem;
	private PlugInSystem plugInSystem;
	private CLISystem cliSystem;
	private BaseConsoleSystem console;

	@Deprecated
	private ModelSystem modelSystem;

	private ArgumentManager argumentManager;
	private CommandManager commandManager;

	private boolean processConsole;

	public App() {
		if (app != null) {
			throw new InstantiationError("App already instantiated");
		}
		app = this;
	}

	public EventSystem getEventSystem() {
		return this.eventSystem;
	}

	public AppConfigModel getAppConfig() {
		return this.appConfig;
	}

	public CLISystem getCLI() {
		return this.cliSystem;
	}

	public PlugInSystem getPlugInSystem() {
		return this.plugInSystem;
	}

	public ConsoleSystem getConsole() {
		return this.console;
	}

	@Deprecated
	public ModelSystem getModelSystem() {
		return this.modelSystem;
	}

	// shortcut
	public <T extends PlugIn> T getPlugIn(Class<T> plugInClass) {
		return this.plugInSystem.getPlugIn(plugInClass);
	}

	public void initializeApp() throws IOException {
		initializeModel();
		// initializeLogging();
		// updateLogger();
		initializeConsole();
		initializeEventSystem();
		initializeCLI();

		initializePlugIns();

		// loadConfig();

	}

	protected void setHeadlessMode() {
		this.console.setHeadlessMode(true);
		this.appConfig.setHeadlessMode(true);
	}

	private void updateLogger() {
		final String appenderName = "RollingFile";
		final String appenderRef = appenderName;// "rolling";
		final String packageRef = "nexusvault.cli";

		final LoggerContext context = (LoggerContext) LogManager.getContext(false);
		final Configuration configuration = context.getConfiguration();
		final RollingFileAppender oldAppender = configuration.getAppender(appenderName);
		final RollingFileManager oldManager = oldAppender.getManager();

		oldAppender.stop();
		configuration.removeLogger(packageRef);

		final String logName = getAppConfig().getReportFolder().resolve("app.log").toString();
		final String logPattern = getAppConfig().getReportFolder().resolve("app-%i.log.gz").toString();

		// final TriggeringPolicy triggerOnStartUp = OnStartupTriggeringPolicy.createPolicy(1);
		// final TriggeringPolicy trigerOnSize = SizeBasedTriggeringPolicy.createPolicy("1 KB");
		// final TriggeringPolicy triggeringPolicy = CompositeTriggeringPolicy.createPolicy(triggerOnStartUp,trigerOnSize);
		//
		// final RolloverStrategy rolloverStrategy = DefaultRolloverStrategy.newBuilder()
		// .withCompressionLevelStr(String.valueOf(Deflater.DEFAULT_COMPRESSION))
		// .withMin("1")
		// .withMax("5")
		// .withFileIndex("min")
		// .withConfig(configuration)
		// .build();

		// @formatter:off
		final RollingFileAppender newAppender = RollingFileAppender.newBuilder()
				.withFileName(logName)
				.withFilePattern(logPattern)
				.withAppend(oldManager.isAppend())
				.setName(oldAppender.getName())
				.setLayout(oldAppender.getLayout())
				.withBufferedIo(oldManager.getBufferSize() > 0)
				.withBufferSize(oldManager.getBufferSize())
				.withCreateOnDemand(oldManager.isCreateOnDemand())
				.withImmediateFlush(oldAppender.getImmediateFlush())
				.withPolicy( oldManager.getTriggeringPolicy())
				.withStrategy(oldManager.getRolloverStrategy())
				.setFilter(oldAppender.getFilter())
				.setIgnoreExceptions(oldAppender.ignoreExceptions())
				.withAdvertise(false)
				.setConfiguration(configuration)
				.build();
		// @formatter:on

		// create new appender/logger
		final LoggerConfig loggerConfig = new LoggerConfig(packageRef, org.apache.logging.log4j.Level.DEBUG, false);

		newAppender.start();
		loggerConfig.addAppender(newAppender, org.apache.logging.log4j.Level.DEBUG, null);
		configuration.addLogger(packageRef, loggerConfig);

		context.updateLoggers();
	}

	private void initializeLogging() {
		// log4j
		final ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

		final LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout").addAttribute("pattern", "%d %p %C{1.} [%t] %m%n");

		final ComponentBuilder<?> triggeringPolicy = builder.newComponent("Policies");
		triggeringPolicy.addComponent(builder.newComponent("OnStartupTriggeringPolicy"));
		triggeringPolicy.addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "1 MB"));

		final ComponentBuilder<?> rolloverStrategy = builder.newComponent("DefaultRolloverStrategy");
		rolloverStrategy.addAttribute("fileIndex", "min");
		rolloverStrategy.addAttribute("min", 1);
		rolloverStrategy.addAttribute("max", 4);

		final String logName = getAppConfig().getReportFolder().resolve("app.log").toString();
		final String logPattern = getAppConfig().getReportFolder().resolve("app-%i.log").toString();

		final AppenderComponentBuilder appenderBuilder = builder.newAppender("rolling", "RollingFile");
		appenderBuilder.addAttribute("fileName", logName);
		appenderBuilder.addAttribute("filePattern", logPattern);
		appenderBuilder.add(layoutBuilder);
		appenderBuilder.addComponent(triggeringPolicy);
		appenderBuilder.addComponent(rolloverStrategy);

		builder.add(appenderBuilder);
		builder.add(builder.newRootLogger(org.apache.logging.log4j.Level.ALL).add(builder.newAppenderRef("rolling")).addAttribute("additivity", false)
				.addAttribute("name", "nexusvault.cli"));

		final Configuration config = builder.build();

		Configurator.initialize(config);
	}

	private void initializeConsole() {
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
		this.console = console;

		LogManager.getLogger(App.class);
	}

	private void initializeCLI() {
		this.argumentManager = new ArgumentManager();
		this.commandManager = new CommandManager();

		this.cliSystem = new CLISystem() {
			@Override
			public void registerCommand(CommandHandler cmd) {
				App.this.commandManager.registerCommand(cmd);
			}

			@Override
			public void unregisterCommand(CommandHandler cmd) {
				App.this.commandManager.unregisterCommand(cmd);
			}

			@Override
			public void registerStartArgumentHandler(ArgumentHandler handler) {
				App.this.argumentManager.registerArgumentHandler(handler);
			}

		};

		this.argumentManager.registerArgumentHandler(new HeadlessModeArgument());
		this.commandManager.registerCommand(new ExitCmd((args) -> requestShutDown()));
		this.commandManager.registerCommand(new SetCmd((args) -> setArguments(args.getUnnamedArgs())));
		this.commandManager.registerCommand(new HelpCmd((args) -> printHelp(args)));
	}

	private void printHelp(Arguments args) {
		final var output = new PrintWriter(System.out);
		if (args.isNamedArgumentSet("cmd")) {
			App.this.commandManager.printHelp(output);
		} else if (args.isNamedArgumentSet("args")) {
			App.this.argumentManager.printHelp(output);
		} else {
			App.this.argumentManager.printHelp(output);
			App.this.commandManager.printHelp(output);
		}
	}

	@Deprecated
	private void initializeModel() {
		this.modelSystem = new BaseModelSystem();

		this.appConfig = new AppConfigModel();
		this.appConfig.setApplicationPath(getProjectLocation());
		this.appConfig.setDebugMode(false);
		// this.appConfig.setHeadlessMode(headlessMode);

		this.modelSystem.registerModel(AppConfigModel.class, this.appConfig);
	}

	private void initializeEventSystem() {
		// TODO
		this.eventSystem = new EventBusSystem((exception, context) -> {
			// TODO Auto-generated method stub
			throw new RuntimeException(exception);
		});

		this.eventSystem.registerEventHandler(new AppConfigObserver()); // TODO
	}

	private final class AppConfigObserver {
		@Subscribe
		public void onAppConfigChangedEvent(ModelPropertyChangedEvent<?> event) {
			App.this.console.println(Level.CONSOLE, () -> {
				if (event.getOldValue() == null && event.getNewValue() == null) {
					return String.format("Property '%s' changed", event.getEventName());
				} else if (event.getOldValue() == null) {
					return String.format("Set '%s' to: '%s'", event.getEventName(), event.getNewValue());
				} else {
					return String.format("Change '%s'\nfrom: '%s'\nto: '%s'", event.getEventName(), event.getOldValue(), event.getNewValue());
				}
			});

			if (event instanceof AppConfigDebugModeChangedEvent) {
				App.this.console.setDebugMode((Boolean) event.getNewValue());
			} else if (event instanceof AppConfigAppPathChangedEvent) {
				updateLogger();
			}
		}
	}

	private void initializePlugIns() throws IOException {
		this.plugInSystem = new BasePlugInProvider();

		final List<PlugIn> objs = initializeComponents("nexusvault.cli.plugin", PlugIn.class);
		for (final PlugIn obj : objs) {
			this.plugInSystem.registerPlugIn(obj.getClass(), obj);
		}

		for (final PlugIn obj : objs) {
			obj.initialize();
		}

		this.console.println(Level.DEBUG, "Plugin: " + objs.size() + " plugin(s) found.");
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

	public void startApp(String[] args) {
		setArguments(args);

		getConsole().println(Level.CONSOLE, "Console mode: Enter 'help' to get a list of available commands."); // TODO

		processConsole();

		this.console.println(Level.CONSOLE, "Closing app");
	}

	protected void setArguments(String[] args) {
		this.argumentManager.runArguments(args);
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

		this.processConsole = true; // TODO
		String[] arguments = null;
		while (this.processConsole) {
			try {
				line = reader.readLine();
				if (line == null) {
					line = "exit";
				}
				line = line.trim();

				// if (line.length() > 0) {
				// if (!line.startsWith("-") && !line.startsWith("--")) {
				// line = "--" + line;
				// }
				// }

				arguments = ArgumentParser.parseArguments(line);
				this.commandManager.executeCommand(arguments);

			} catch (final CommandFormatException e1) {
				logger.error(String.format("Error at cmd(s): %s", Arrays.toString(arguments)), e1);
				this.console.println(Level.CONSOLE, () -> {
					final StringBuilder msg = new StringBuilder();
					msg.append("Command not executable\n");
					msg.append(e1.getMessage()).append("\n");
					return msg.toString();
				});
			} catch (final Exception e2) {
				logger.error(String.format("Error at cmd(s): %s", Arrays.toString(arguments)), e2);
				this.console.println(Level.CONSOLE, () -> {
					final StringBuilder msg = new StringBuilder();
					msg.append("An error occured: (The error log contains more detailed informations)\n");
					msg.append(e2.getClass().toString()).append(" : ").append(e2.getMessage()).append("\n");
					Throwable t = e2.getCause();
					while (t != null) {
						msg.append(t.toString()).append("\n");
						t = t.getCause();
					}
					return msg.toString();
				});
			}
		}
	}

	public void requestShutDown() {
		this.processConsole = false;
	}

}
