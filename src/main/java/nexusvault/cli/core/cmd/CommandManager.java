package nexusvault.cli.core.cmd;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import nexusvault.cli.App;
import nexusvault.cli.ConsoleSystem.Level;

public final class CommandManager {

	private final class RunnableCommand implements Runnable {
		private final CommandContainer commandContainer;
		private final Arguments args;

		public RunnableCommand(CommandContainer commandContainer, Arguments args) {
			this.commandContainer = commandContainer;
			this.args = args;
		}

		@Override
		public void run() {
			this.commandContainer.command.onCommand(this.args);
		}
	}

	private static final class CommandContainer {
		protected final CommandHandler command;
		protected final CommandDescription description;
		protected final Options options;

		public CommandContainer(CommandHandler command, CommandDescription description, Options options) {
			this.command = command;
			this.description = description;
			this.options = options;
		}
	}

	private final CommandLineParser parser;
	private final Map<String, CommandContainer> commands = new HashMap<>();

	public CommandManager() {
		this(Collections.emptyList());
	}

	public CommandManager(List<CommandHandler> commands) {
		if (commands == null) {
			throw new IllegalArgumentException("'commands' must not be null");
		}

		this.parser = new DefaultParser();

		for (final CommandHandler command : commands) {
			registerCommand(command);
		}
	}

	public void registerCommand(CommandHandler command) throws CommandAlreadyDefinedException {
		if (command == null) {
			throw new IllegalArgumentException("'command' must not be null");
		}

		final CommandDescription description = command.getCommandDescription();
		final var argumentOptions = new Options();
		for (final var commandArgument : description.getArgs()) {
			final var parserOption = buildOption(commandArgument);
			argumentOptions.addOption(parserOption);
		}

		// argumentOptions.addOption("?", "help", false, "Show help text of command");

		final var commandName = description.getCommandName().toUpperCase();
		if (this.commands.containsKey(commandName)) {
			throw new CommandAlreadyDefinedException(String.format("The name '%s' is already registered", commandName));
		}

		final var container = new CommandContainer(command, description, argumentOptions);
		this.commands.put(commandName, container);
	}

	private Option buildOption(ArgumentDescription description) {
		final var builder = description.hasNameShort() ? Option.builder(description.getNameShort()) : Option.builder();

		if (description.hasName()) {
			builder.longOpt(description.getName());
		}

		if (description.hasDescription()) {
			builder.desc(description.getDescription());
		}

		builder.required(description.isCommandRequired());

		if (description.hasArguments()) {
			builder.optionalArg(description.isArgumentOptional());
			builder.argName(String.join(", ", description.getArgumentNames()));

			if (description.isNumberOfArgumentsUnlimited()) {
				builder.numberOfArgs(Option.UNLIMITED_VALUES);
			} else {
				builder.numberOfArgs(description.getNumberOfArguments());
			}
		}

		return builder.build();
	}

	public boolean unregisterCommand(CommandHandler command) {
		if (command == null) {
			throw new IllegalArgumentException("'command' must not be null");
		}

		final var description = command.getCommandDescription();
		final var cmdName = description.getCommandName().toUpperCase();
		final var storedCmd = this.commands.get(cmdName);
		if (storedCmd != null) {
			if (storedCmd.command != command) {
				throw new CommandAlreadyDefinedException(String.format("The name '%s' is used by another commmand", cmdName));
			}

			this.commands.remove(cmdName);
			return true;
		}

		return false;
	}

	public void executeCommand(String[] args) throws CommandFormatException {
		if (args == null || args.length == 0) {
			return;
		}

		final var commandName = args[0].toUpperCase();
		args = getTail(args);

		final var cmds = getCommands(commandName);
		if (cmds.isEmpty()) {
			App.getInstance().getConsole().println(Level.CONSOLE, String.format("No command found with name '%s'", commandName));
			return;
		} else if (cmds.size() > 1) {
			final var cmdNames = cmds.stream().map(c -> c.description.getCommandName()).collect(Collectors.joining(", "));
			App.getInstance().getConsole().println(Level.CONSOLE, String.format("Multiple matching commands found: %s", cmdNames));
			return;
		}

		final var cmdContainer = cmds.get(0);
		if (contains(args, "?") || contains(args, "help")) { // only display help
			printCmdHelp(cmdContainer);
			return;
		}

		if (!commandName.equalsIgnoreCase(cmdContainer.description.getCommandName())) {
			App.getInstance().getConsole().println(Level.CONSOLE, String.format("Execute command '%s'", cmdContainer.description.getCommandName()));
		}

		final var runnable = wrapAsRunnable(args, cmdContainer);
		runnable.run();
	}

	private RunnableCommand wrapAsRunnable(String[] args, final CommandContainer commandContainer) {
		try {
			final var ignoreNonOptions = commandContainer.description.ignoreNonOptions() || !commandContainer.description.hasArguments();
			final var cmdLine = this.parser.parse(commandContainer.options, args, ignoreNonOptions);
			final var cmdArgs = new Arguments(cmdLine);
			return new RunnableCommand(commandContainer, cmdArgs);
		} catch (final ParseException e) {
			throw new CommandFormatException(e);
		}
	}

	private boolean contains(String[] args, String txt) {
		for (final var arg : args) {
			if (arg.equalsIgnoreCase(txt)) {
				return true;
			}
		}
		return false;
	}

	private String[] getTail(String[] array) {
		final var result = new String[array.length - 1];
		System.arraycopy(array, 1, result, 0, result.length);
		return result;
	}

	private List<CommandContainer> getCommands(String cmdName) {
		if (this.commands.containsKey(cmdName)) {
			return getCommand(cmdName);
		} else {
			final var possibleCommands = new ArrayList<String>();
			for (final var availableCommand : this.commands.keySet()) {
				if (availableCommand.startsWith(cmdName)) {
					possibleCommands.add(availableCommand);
				}
			}

			if (possibleCommands.size() == 0) {
				return Collections.emptyList();
			} else if (possibleCommands.size() == 1) {
				return getCommand(possibleCommands.get(0));
			} else {
				return possibleCommands.stream().map(c -> getCommand(c)).flatMap(List::stream).collect(Collectors.toList());
			}
		}
	}

	private List<CommandContainer> getCommand(String cmdName) {
		final var cmd = this.commands.get(cmdName);
		return cmd != null ? Collections.singletonList(cmd) : Collections.emptyList();
	}

	private void printCmdHelp(CommandContainer container) {
		final var description = container.description;

		final var cmdDescription = description.getCommandDescription();
		final var helpText = container.command.onHelp();

		final var textBuilder = new StringBuilder();
		if (cmdDescription != null && !cmdDescription.isBlank()) {
			textBuilder.append(cmdDescription);
		}

		if (helpText != null && !helpText.isBlank()) {
			if (textBuilder.length() > 0) {
				textBuilder.append('\n');
			}
			textBuilder.append(helpText);
		}

		if (textBuilder.length() > 0) {
			textBuilder.append('\n');
		}
		textBuilder.append("Flags and options:");

		final var header = textBuilder.toString();

		final PrintWriter writer = new PrintWriter(System.out);
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(writer, formatter.getWidth(), description.getCommandName(), header, container.options, formatter.getLeftPadding(),
				formatter.getDescPadding(), null, true);
		writer.flush();
	}

	public void printHelp(PrintWriter writer) {
		if (writer == null) {
			throw new IllegalArgumentException("'writer' must not be null");
		}

		final HelpFormatter formatter = new HelpFormatter();

		final var builder = new StringBuilder();

		final var maxCommandNameLength = this.commands.keySet().stream().map(String::length).mapToInt(Integer::valueOf).max().orElse(0);
		final var maxLineLength = formatter.getWidth();

		for (final var entry : this.commands.entrySet()) {
			final var firstLine = String.format(">%-" + maxCommandNameLength + "s   ", entry.getKey().toUpperCase());

			builder.append(firstLine).append("\n");

			final var description = entry.getValue().description.getCommandDescription();
			final var lines = wrapLine(description, maxLineLength);
			for (final var line : lines) {
				builder.append(line).append('\n');
			}

			builder.append('\n');
		}

		formatter.printWrapped(writer, formatter.getWidth(),
				"List of available commands. A command  followed by '?' will, if available, show a command specific help.");
		formatter.printWrapped(writer, formatter.getWidth(), builder.toString());

		writer.flush();
	}

	private static List<String> wrapLine(String line, int lineLength) {
		if (line.length() < lineLength) {
			return Collections.singletonList(line);
		}

		final var words = line.split(" ");
		final var lineBuilder = new StringBuilder(lineLength);
		final var results = new ArrayList<String>();
		for (var word : words) {
			if (word.isEmpty()) {
				continue;
			}

			if (lineBuilder.length() + word.length() <= lineLength) {
				lineBuilder.append(word).append(" ");
				continue;
			}

			if (word.length() > lineLength) { // special case, word is too long for one line

				if (lineBuilder.length() > 0) { // builder is not empty
					final var remainingSpace = lineLength - lineBuilder.length();
					if (remainingSpace > 3) { // enough space for some characters and a hypen
						lineBuilder.append(word.substring(0, remainingSpace - 1)).append("-");
						results.add(lineBuilder.toString());
						lineBuilder.setLength(0);
						word = word.substring(remainingSpace - 1);
					}
				}

				final var wordParts = wrapWord(word, lineLength, "-");
				for (final var part : wordParts) {
					if (part.length() == lineLength) {
						results.add(part);
					} else {
						lineBuilder.append(part).append(" "); // only true for the last part
					}
				}

				continue;
			}

			lineBuilder.setLength(lineBuilder.length() - 1);
			results.add(lineBuilder.toString());
			lineBuilder.setLength(0);

			lineBuilder.append(word).append(" ");
		}

		if (lineBuilder.length() > 0) {
			lineBuilder.setLength(lineBuilder.length() - 1);
			results.add(lineBuilder.toString());
		}

		return results;
	}

	private static List<String> wrapWord(String word, int wrapLength, String delimiter) {
		if (word.length() < wrapLength) {
			return Collections.singletonList(word);
		}

		final var result = new ArrayList<String>();
		var startIdx = 0;

		while (startIdx < word.length()) {
			var endIdx = Math.min(word.length(), startIdx + wrapLength);

			if (endIdx == word.length()) { // done
				result.add(word.substring(startIdx, endIdx));
			} else {
				endIdx = endIdx - delimiter.length();
				final var part = word.substring(startIdx, endIdx) + delimiter;
				result.add(part);
			}

			startIdx = endIdx;
		}

		return result;
	}

}
