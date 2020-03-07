package nexusvault.cli.core.cmd;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public final class ArgumentManager {

	private static class RunnableArgumentHandler implements Runnable {
		private final Argument args;
		private final ArgumentHandler handler;

		public RunnableArgumentHandler(Argument args, ArgumentHandler handler) {
			this.args = args;
			this.handler = handler;
		}

		@Override
		public void run() {
			this.handler.execute(this.args);
		}
	}

	private final CommandLineParser parser;
	private final Options parserOptions;
	private final Map<String, ArgumentHandler> handlers = new HashMap<>();

	public ArgumentManager() {
		this(Collections.emptyList());
	}

	public ArgumentManager(List<ArgumentHandler> handlers) {
		if (handlers == null) {
			throw new IllegalArgumentException("'handlers' must not be null");
		}

		this.parserOptions = new Options();
		this.parser = new DefaultParser();

		for (final var handler : handlers) {
			registerArgumentHandler(handler);
		}
	}

	public void registerArgumentHandler(ArgumentHandler handler) throws HandlerAlreadyDefinedException {
		if (handler == null) {
			throw new IllegalArgumentException("'handler' must not be null");
		}

		final Option opt = buildOption(handler);
		if (this.parserOptions.getOption(getOptionKey(opt)) != null) {
			throw new HandlerAlreadyDefinedException("The command '" + opt.getOpt() + "/" + opt.getLongOpt() + "' is already used");
		}

		this.parserOptions.addOption(opt);
		this.handlers.put(getOptionKey(opt), handler);
	}

	private Option buildOption(ArgumentHandler handler) {
		final var info = handler.getArgumentDescription();

		final Option.Builder builder = info.hasNameShort() ? Option.builder(info.getNameShort()) : Option.builder();

		if (info.hasName()) {
			builder.longOpt(info.getName());
		}

		if (info.hasDescription()) {
			builder.desc(info.getDescription());
		}

		builder.required(info.isCommandRequired());

		if (info.hasArguments()) {
			builder.optionalArg(info.isArgumentOptional());
			builder.argName(String.join(", ", info.getArgumentNames()));

			if (info.isNumberOfArgumentsUnlimited()) {
				builder.numberOfArgs(Option.UNLIMITED_VALUES);
			} else {
				builder.numberOfArgs(info.getNumberOfArguments());
			}
		}

		return builder.build();
	}

	public void runArguments(String[] args) throws CommandFormatException {
		final var runnables = getHandlers(args);
		for (final var runnable : runnables) {
			runnable.run();
		}
	}

	private List<RunnableArgumentHandler> getHandlers(String[] args) {
		final List<RunnableArgumentHandler> cmds = new LinkedList<>();
		try {
			final CommandLine cmdLine = this.parser.parse(this.parserOptions, args);
			final Option[] options = cmdLine.getOptions();
			for (final var option : options) {
				final var key = getOptionKey(option);
				final var command = getHandler(key);
				if (command.isPresent()) {
					final var handleArgs = new Argument(option);
					cmds.add(new RunnableArgumentHandler(handleArgs, command.get()));
				}
			}
		} catch (final ParseException e) {
			throw new CommandFormatException(e);
		}

		return cmds;
	}

	private String getOptionKey(Option opt) {
		return opt.getOpt() == null ? opt.getLongOpt() : opt.getOpt();
	}

	private Optional<ArgumentHandler> getHandler(String optionName) {
		final var result = this.handlers.get(optionName);
		return Optional.ofNullable(result);
	}

	public void printHelp(PrintWriter writer) {
		if (writer == null) {
			throw new IllegalArgumentException("'writer' must not be null");
		}

		final String cmd = "nexusvault";
		final String header = "Tool to extract data from the wildstar game archive. The following arguments can be set on application start or via the <set> command.";
		final String footer = "";

		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(writer, formatter.getWidth(), cmd, header, this.parserOptions, formatter.getLeftPadding(), formatter.getDescPadding(), footer,
				true);

		writer.flush();
	}

}
