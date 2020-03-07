package nexusvault.cli.core.cmd;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class ArgumentCollection implements Iterable<String> {

	public static ArgumentCollection build(List<String> args) {
		return new ArgumentCollection(args.toArray(new String[args.size()]));
	}

	private final String[] args;

	private ArgumentCollection(String[] args) {
		this.args = args;
	}

	public int size() {
		return this.args.length;
	}

	public String getArg(int index) {
		return this.args[index];
	}

	@Override
	public Iterator<String> iterator() {
		return Arrays.stream(this.args).iterator();
	}

}