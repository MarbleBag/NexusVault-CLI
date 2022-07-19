package nexusvault.cli.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

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

import com.google.common.eventbus.Subscribe;

import nexusvault.cli.Main;
import nexusvault.cli.core.AppConfig.AppConfigAppPathChangedEvent;
import nexusvault.cli.core.AppConfig.AppConfigDebugModeChangedEvent;
import nexusvault.cli.core.Console.Level;
import nexusvault.cli.core.cmd.ArgumentHandler;
import nexusvault.cli.core.cmd.ArgumentManager;
import nexusvault.cli.core.cmd.ArgumentParser;
import nexusvault.cli.core.cmd.CommandFormatException;
import nexusvault.cli.core.cmd.CommandHandler;
import nexusvault.cli.core.cmd.CommandManager;
import nexusvault.cli.core.command.Exit;
import nexusvault.cli.core.command.Help;
import nexusvault.cli.core.command.SetCmd;
import nexusvault.cli.core.extension.Extension;
import nexusvault.cli.model.ModelPropertyChangedEvent;

public final class App {

	private final static Logger logger = LogManager.getLogger(App.class);

	private static App app;

	public static App getInstance() {
		return app;
	}

	private EventManager eventManager;
	private BaseExtensionManager extensionManager;
	private CommandLineManager cliSystem;
	private BaseConsoleManager consoleManager;

	private ArgumentManager argumentManager;
	private CommandManager commandManager;

	private boolean processConsole;

	private AppConfig appConfig;

	public App() {
		if (app != null) {
			throw new InstantiationError("App already instantiated");
		}
		app = this;
	}

	public EventManager getEventSystem() {
		return this.eventManager;
	}

	public AppConfig getAppConfig() {
		return this.appConfig;
	}

	public CommandLineManager getCLI() {
		return this.cliSystem;
	}

	public ExtensionManager getExtensionManager() {
		return this.extensionManager;
	}

	public Console getConsole() {
		return this.consoleManager;
	}

	// shortcut
	public <T extends Extension> T getExtension(Class<T> extensionClass) {
		return this.extensionManager.getExtension(extensionClass);
	}

	public void initializeApp() throws IOException {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			// TODO Auto-generated method stub
			return;
		}));

		this.extensionManager = new BaseExtensionManager(this);

		// console and logger
		this.consoleManager = new BaseConsoleManager(this);

		// event manager
		this.eventManager = new EventBusSystem((exception, context) -> {
			throw new RuntimeException(exception);
		});

		this.eventManager.registerEventHandler(new AppConfigObserver()); // TODO

		initializeAppConfig();

		// command line manager
		this.argumentManager = new ArgumentManager();
		this.commandManager = new CommandManager();
		this.cliSystem = new CommandLineManager() {
			@Override
			public void registerCommand(CommandHandler cmd) {
				App.this.commandManager.registerCommand(cmd);
			}

			@Override
			public void unregisterCommand(CommandHandler cmd) {
				App.this.commandManager.unregisterCommand(cmd);
			}

			@Override
			public void registerArgument(ArgumentHandler handler) {
				App.this.argumentManager.registerArgumentHandler(handler);
			}
		};

		getCLI().registerCommand(new Exit((args) -> requestShutDown()));
		getCLI().registerCommand(new SetCmd((args) -> setArguments(args.getUnnamedArgs())));
		getCLI().registerCommand(new Help((args) -> {
			final var output = getConsole().getWriter(Level.CONSOLE);
			if (args.isNamedArgumentSet("cmd")) {
				App.this.commandManager.printHelp(output);
			} else if (args.isNamedArgumentSet("args")) {
				App.this.argumentManager.printHelp(output);
			} else {
				App.this.argumentManager.printHelp(output);
				App.this.commandManager.printHelp(output);
			}
		}));

		this.extensionManager.loadExtensions("nexusvault.cli.extensions");

		// loadConfig();
	}

	private void initializeAppConfig() {
		this.appConfig = new AppConfig();

		{ // set application path
			try {
				final var currentLocation = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				this.appConfig.setApplicationPath(currentLocation);
			} catch (final URISyntaxException e) {
				throw new IllegalStateException(e);
			}
		}

		{ // set app version
			final var properties = new Properties();

			final var classLoader = this.getClass().getClassLoader();
			final var propertyContent = classLoader.getResourceAsStream("app.properties");
			if (propertyContent != null) {
				try (propertyContent) {
					properties.load(propertyContent);
				} catch (final IOException e) {
					logger.error(String.format("Unable to load app.properties"), e);
				}
			}

			if (properties.containsKey("app.version")) {
				this.appConfig.setApplicationVersion(properties.getProperty("app.version"));
			}
		}
	}

	protected void setHeadlessMode() {
		this.consoleManager.setHeadlessMode(true);
		this.appConfig.setHeadlessMode(true);
	}

	private void updateRootLogger() {
		final String packageRef = "nexusvault.cli";

		final LoggerContext context = (LoggerContext) LogManager.getContext(false);
		final Configuration configuration = context.getConfiguration();
		final RollingFileAppender oldAppender = configuration.getAppender("ToFile");
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

		final AppenderComponentBuilder appenderBuilder = builder.newAppender("rootappender", "RollingFile");
		appenderBuilder.addAttribute("fileName", logName);
		appenderBuilder.addAttribute("filePattern", logPattern);
		appenderBuilder.add(layoutBuilder);
		appenderBuilder.addComponent(triggeringPolicy);
		appenderBuilder.addComponent(rolloverStrategy);

		builder.add(appenderBuilder);
		builder.add(builder.newRootLogger(org.apache.logging.log4j.Level.ALL).add(builder.newAppenderRef("rootappender")).addAttribute("additivity", false)
				.addAttribute("name", "nexusvault.cli"));

		final Configuration config = builder.build();

		Configurator.initialize(config);

		// logger = LogManager.getLogger(App.class);
	}

	private final class AppConfigObserver {
		@Subscribe
		public void onAppConfigChangedEvent(ModelPropertyChangedEvent<?> event) {
			App.this.consoleManager.println(Level.CONSOLE, () -> {
				if (event.getOldValue() == null && event.getNewValue() == null) {
					return String.format("Property '%s' changed", event.getEventName());
				} else if (event.getOldValue() == null) {
					return String.format("Set '%s' to: '%s'", event.getEventName(), event.getNewValue());
				} else {
					return String.format("Change '%s'\nfrom: '%s'\nto: '%s'", event.getEventName(), event.getOldValue(), event.getNewValue());
				}
			});

			if (event instanceof AppConfigDebugModeChangedEvent) {
				App.this.consoleManager.setDebugMode((Boolean) event.getNewValue());
			} else if (event instanceof AppConfigAppPathChangedEvent) {
				updateRootLogger();
			}
		}
	}

	public void startApp(String[] args) {
		setArguments(args);

		getConsole().println(Level.CONSOLE, "Console mode: Enter 'help' to get a list of available commands."); // TODO

		processConsole();

		getConsole().println(Level.CONSOLE, "Closing app");
	}

	protected void setArguments(String[] args) {
		this.argumentManager.runArguments(args);
	}

	public void closeApp() {
		saveAppConfig();
		savePlugInConfigs();

		this.consoleManager = null;
		this.eventManager = null;
		this.commandManager = null;
		this.cliSystem = null;
		this.extensionManager = null;
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
				getConsole().println(Level.CONSOLE, () -> {
					final StringBuilder msg = new StringBuilder();
					msg.append("Command not executable\n");
					msg.append(e1.getMessage()).append("\n");
					return msg.toString();
				});
			} catch (final Exception e2) {
				logger.error(String.format("Error at cmd(s): %s", Arrays.toString(arguments)), e2);
				getConsole().println(Level.CONSOLE, () -> {
					final StringBuilder msg = new StringBuilder();
					msg.append("An error occured: (The error log may contain more detailed information)\n");
					msg.append(e2.getClass()).append(" : ").append(e2.getMessage()).append("\n");
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
