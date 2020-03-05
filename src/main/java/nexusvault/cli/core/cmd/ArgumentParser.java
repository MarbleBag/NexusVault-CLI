package nexusvault.cli.core.cmd;

import java.util.LinkedList;
import java.util.List;

public final class ArgumentParser {
	private ArgumentParser() {
		throw new IllegalAccessError();
	}

	/**
	 *
	 * @param line
	 * @return
	 * @throws ArgumentFormatException
	 */
	public static String[] parseArguments(String line) throws ArgumentFormatException {
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
					throw new ArgumentFormatException("Expected whitespace after command or argument at " + (i + 1));
				}
			}

			if ('\'' == c || '\"' == c) {
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

}
