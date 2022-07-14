package nexusvault.cli.core.cmd;

import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;

public final class Arguments {
	private final CommandLine cmdLine;
	private final String[] args;
	private final Argument[] options;

	public Arguments(CommandLine cmdLine) {
		this.cmdLine = cmdLine;
		this.args = cmdLine.getArgs() == null ? new String[0] : cmdLine.getArgs();
		this.options = Arrays.stream(cmdLine.getOptions()).map(Argument::new).toArray(n -> new Argument[n]);
	}

	public Argument[] getNamedArgs() {
		return this.options;
	}

	public String[] getUnnamedArgs() {
		return this.args;
	}

	public Properties getProperties(String value) {
		return this.cmdLine.getOptionProperties(value);
	}

	public boolean hasUnnamedArgValue(String value) {
		for (final var arg : this.args) {
			if (arg != null && arg.equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}

	public Argument getNamedArgument(int idx) {
		return this.options[idx];
	}

	public boolean isNamedArgumentSet(String name) {
		for (final var arg : this.options) {
			if (arg.hasName(name)) {
				return true;
			}
		}
		return false;
	}

	public Argument getArgumentByName(String name) {
		for (final var arg : this.options) {
			if (arg.hasName(name)) {
				return arg;
			}
		}
		throw new IllegalArgumentException();
	}

	public int getUnnamedArgumentSize() {
		return this.args.length;
	}

	public int getNamedArgumentSize() {
		return this.options.length;
	}
}