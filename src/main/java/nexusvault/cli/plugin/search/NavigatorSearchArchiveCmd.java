package nexusvault.cli.plugin.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import nexusvault.cli.App;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;
import nexusvault.cli.plugin.AbstCommand;

final class NavigatorSearchArchiveCmd extends AbstCommand {

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName("search")
				.setNameShort("s")
				.setDescription("regex expression to match against full file names. Multiple expressions are seperated by a single white space. If the last argument is an integer, it will be interpreted as the maximal number of results to find, before the procedure stops.")
				.setRequired(false)
				.setArguments(true)
				.setNumberOfArgumentsUnlimited()
				.setNamesOfArguments("regex, ..., maxResults")
			    .build();
		//@formatter:on
	}

	@Override
	public void onCommand(CommandArguments args) {
		int maxSearchResults = Integer.MAX_VALUE;
		boolean lastArgIsInteger = false;
		final String lastArg = args.getArg(args.getNumberOfArguments() - 1);
		try {
			final int result = Integer.valueOf(lastArg);
			maxSearchResults = Math.max(1, result);
			lastArgIsInteger = true;
		} catch (final NumberFormatException e) {
			// ignore
		}

		final int numberOfPattern = lastArgIsInteger ? args.getNumberOfArguments() - 1 : args.getNumberOfArguments();
		final List<Pattern> regex = new ArrayList<>(numberOfPattern);
		for (int i = 0; i < numberOfPattern; ++i) {
			final String s = args.getArg(i);
			regex.add(Pattern.compile(s, Pattern.CASE_INSENSITIVE));
		}

		final SearchRequest request = new SearchRequest();
		request.setPattern(regex);
		request.setMaxResults(maxSearchResults);

		App.getInstance().getPlugIn(SearchPlugIn.class).searchArchive(request);
	}

	@Override
	public void onHelp(CommandArguments args) {
		sendMsg(() -> {
			final String sep = File.separator.equals("\\") ? "\\\\" : File.separator;

			final StringBuilder msg = new StringBuilder();
			msg.append("Accepts one or multiple regular expressions using java pattern. The space character is used as a seperator for multiple arguments.\n");
			msg.append("This means, to use a space within a regular expression, the expression needs be surrounded by double quotes (\").\n");
			msg.append("File seperator are OS dependend. This system uses ");
			msg.append(File.separator);
			msg.append(
					"as a seperator. In case the seperator is equal to '\\' and should be included into the regular expression, it needs to be escaped with an additional '\\' -> '\\\\'\n");

			msg.append("General examples:\n");
			msg.append("Finding all textures which are located within art/character/aurin:\n");
			msg.append("art").append(sep).append("character").append(sep).append("aurin").append(".*.tex\n");

			msg.append("Finding all m3 models that end with aurin_f:\n");
			msg.append("aurin_f.m3\n");

			msg.append("Finding only m3 models that are named aurin_f:\n");
			msg.append(sep).append("aurin_f.m3\n");

			return msg.toString();
		});
	}

}
