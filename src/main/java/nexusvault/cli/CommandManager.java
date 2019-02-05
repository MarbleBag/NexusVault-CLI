package nexusvault.cli;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

final class CommandManager {

	private static class RunnableCommand implements Runnable {
		private final CommandArguments args;
		private final Command command;

		public RunnableCommand(CommandArguments args, Command command) {
			this.args = args;
			this.command = command;
		}

		@Override
		public void run() {
			if (args.getNumberOfArguments() != 0) {
				if ("?".equals(args.getArg(0)) || "h".equalsIgnoreCase(args.getArg(0)) || "help".equalsIgnoreCase(args.getArg(0))) {
					command.onHelp(args);
					return;
				}
			}

			command.onCommand(args);
		}
	}

	private final CommandLineParser parser;
	private Options parserOptions;
	private final Map<String, Command> commands = new HashMap<>();

	public CommandManager() {
		this(Collections.emptyList());
	}

	public CommandManager(List<Command> commands) {
		if (commands == null) {
			throw new IllegalArgumentException("'commands' must not be null");
		}

		this.parserOptions = new Options();
		this.parser = new DefaultParser();

		for (final Command command : commands) {
			registerCommand(command);
		}
	}

	public void registerCommand(Command command) throws CommandAlreadyDefinedException {
		if (command == null) {
			throw new IllegalArgumentException("'command' must not be null");
		}

		final Option opt = buildOption(command);
		if (this.parserOptions.getOption(getOptionKey(opt)) != null) {
			throw new CommandAlreadyDefinedException("The command '" + opt.getOpt() + "/" + opt.getLongOpt() + "' is already used");
		}

		this.parserOptions.addOption(opt);
		this.commands.put(getOptionKey(opt), command);
	}

	private Option buildOption(Command command) {
		final CommandInfo info = command.getCommandInfo();

		final Option.Builder builder = info.hasCommandNameShort() ? Option.builder(info.getCommandNameShort()) : Option.builder();

		if (info.hasCommandName()) {
			builder.longOpt(info.getCommandName());
		}

		if (info.hasCommandDescription()) {
			builder.desc(info.getCommandDescription());
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

	public boolean unregisterCommand(Command command) {
		if (command == null) {
			throw new IllegalArgumentException("'command' must not be null");
		}

		final Option opt = buildOption(command);
		if (this.commands.containsKey(getOptionKey(opt))) {
			this.commands.remove(getOptionKey(opt));
			this.parserOptions = new Options();
			for (final Entry<String, Command> entry : this.commands.entrySet()) {
				this.parserOptions.addOption(buildOption(entry.getValue()));
			}
		}
		return false;
	}

	public String[] parseArguments(String line) throws CommandFormatException {
		final String[] args = parseArgument(line);
		return args;
	}

	public void runArguments(String[] args) throws CommandFormatException {
		final List<RunnableCommand> cmds = getCommands(args);
		for (final RunnableCommand cmd : cmds) {
			cmd.run();
		}
	}

	private List<RunnableCommand> getCommands(String[] args) {
		final List<RunnableCommand> cmds = new LinkedList<>();
		try {
			final CommandLine cmdLine = parser.parse(parserOptions, args);
			final Option[] options = cmdLine.getOptions();
			for (final Option option : options) {
				final String key = getOptionKey(option);
				final Optional<Command> command = getCommand(key);
				if (command.isPresent()) {
					final CommandArguments cmdArgs = CommandArguments.build(option.getValuesList());
					cmds.add(new RunnableCommand(cmdArgs, command.get()));
				}
			}
		} catch (final ParseException e) {
			throw new CommandFormatException(e);
		}

		// Collections.sort(cmds);

		return cmds;
	}

	private String getOptionKey(Option opt) {
		return opt.getOpt() == null ? opt.getLongOpt() : opt.getOpt();
	}

	private Optional<Command> getCommand(String optionName) {
		final Command cmd = this.commands.get(optionName);
		return Optional.ofNullable(cmd);
	}

	private String[] parseArgument(String line) throws CommandFormatException {
		final List<String> args = new LinkedList<>();

		boolean isQuoted = false;
		boolean expectSpace = false;
		char quoteChar = 0;

		final StringBuilder argBuilder = new StringBuilder();

		char c = 0;
		for (int i = 0; i < line.length(); ++i) {
			c = line.charAt(i);

			if (expectSpace) {
				if (Character.isWhitespace(c)) {
					expectSpace = false;
				} else {
					throw new CommandFormatException("Expected whitespace after command or argument at " + (i + 1));
				}
			}

			if (('\'' == c) || ('\"' == c)) {
				if (!isQuoted) {
					isQuoted = true;
					quoteChar = c;
					continue;
				} else if (quoteChar == c) {
					isQuoted = false;
					expectSpace = true;
					args.add(argBuilder.toString());
					argBuilder.setLength(0);
					continue;
				}
			} else if (Character.isWhitespace(c)) {
				if (!isQuoted) {
					if (argBuilder.length() > 0) {
						args.add(argBuilder.toString());
					}
					argBuilder.setLength(0);
					continue;
				}
			}

			argBuilder.append(c);
		}

		if (argBuilder.length() != 0) {
			args.add(argBuilder.toString());
		}

		return args.toArray(new String[args.size()]);
	}

	public void printHelp(PrintHelpContext context) {
		if (context == null) {
			throw new IllegalArgumentException("'context' must not be null");
		}

		final PrintWriter writer = context.getWriter();

		final HelpFormatter formatter = new HelpFormatter();
		if (context.getWidth() > 0) {
			formatter.setWidth(context.getWidth());
		}
		if (context.getLeftPadding() > 0) {
			formatter.setLeftPadding(context.getLeftPadding());
		}
		if (context.getDescPadding() > 0) {
			formatter.setDescPadding(context.getDescPadding());
		}

		final String cmd = context.getCommandName();
		final String header = context.getHeader();
		final String footer = context.getFooter();

		formatter.printHelp(writer, formatter.getWidth(), cmd, header, parserOptions, formatter.getLeftPadding(), formatter.getDescPadding(), footer, true);

		writer.flush();
	}

}
