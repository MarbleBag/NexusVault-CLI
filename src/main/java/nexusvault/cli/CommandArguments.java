package nexusvault.cli;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class CommandArguments implements Iterable<String> {

	public static CommandArguments build(List<String> args) {
		return new CommandArguments(args.toArray(new String[args.size()]));
	}

	private final String[] args;

	private CommandArguments(String[] args) {
		this.args = args;
	}

	public int getNumberOfArguments() {
		return args.length;
	}

	public String getArg(int index) {
		return args[index];
	}

	@Override
	public Iterator<String> iterator() {
		return Arrays.stream(args).iterator();
	}

}