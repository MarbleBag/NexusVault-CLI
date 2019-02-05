package nexusvault.cli.plugin.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;

final class NavigatorSearchArchiveCmd implements Command {

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
		// TODO Auto-generated method stub
	}

}
