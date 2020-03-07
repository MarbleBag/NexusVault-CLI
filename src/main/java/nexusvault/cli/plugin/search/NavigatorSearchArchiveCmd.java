package nexusvault.cli.plugin.search;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import nexusvault.cli.App;
import nexusvault.cli.core.cmd.AbstractCommandHandler;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;

final class NavigatorSearchArchiveHandler extends AbstractCommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("search")
				.setDescription("regex expression to match against full file names. Multiple expressions are seperated by a single white space.")
				.addNamedArgument(
						ArgumentDescription.newInfo()
							.setName("maxResults")
							.setNameShort("max")
							.setDescription("Maximal numbers of results which should be returned. By default this is not limited. A value of -1 will be handled as default.")
							.setRequired(false)
							.setArguments(false)
							.setNumberOfArguments(1)
							.setNamesOfArguments("number")
							.build()
						)
				.ignoreUnnamedArguments()
				.namedArgumentsDone()
			    .build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		if (args.getUnnamedArgumentSize() == 0) {
			sendMsg("Needs at least one argument. Use '?' to get more informations.");
			return;
		}

		int maxSearchResults = Integer.MAX_VALUE;

		if (args.hasUnnamedArgValue("max")) {
			final var argMaxResults = args.getArgumentByName("max");
			try {
				final int result = Integer.valueOf(argMaxResults.getValue());
				maxSearchResults = Math.max(1, result);
			} catch (final NumberFormatException e) {
				// ignore
			}
		}

		final var unnamedArgs = args.getUnnamedArgs();
		final var regex = new ArrayList<Pattern>(unnamedArgs.length);
		for (final String unnamedArg : unnamedArgs) {
			regex.add(Pattern.compile(unnamedArg, Pattern.CASE_INSENSITIVE));
		}

		final SearchRequest request = new SearchRequest();
		request.setPattern(regex);
		request.setMaxResults(maxSearchResults);

		App.getInstance().getPlugIn(SearchPlugIn.class).searchArchive(request);
	}

	@Override
	public String onHelp() {
		sendMsg(() -> {
			final String sep = File.separator.equals("\\") ? "\\\\" : File.separator;

			final StringBuilder msg = new StringBuilder();
			msg.append("Accepts one or multiple regular expressions using java pattern. The space character is used as a seperator for multiple arguments.\n");
			msg.append("This means, to use a space within a regular expression, the expression needs be surrounded by double quotes ( \" ).\n");
			msg.append("File seperator are OS dependend. This system uses ");
			msg.append("'").append(File.separator).append("' as a separator.\n");
			msg.append(
					"If the separator is identical to '\\' it needs to be escaped with an additional '\\' and should now look like '\\\\'. This is because the regular expression itself uses '\\' as an escape character.\n");

			msg.append("General examples:\n");
			msg.append("Finding all textures which are located within art/character/aurin:\n");
			msg.append("art").append(sep).append("character").append(sep).append("aurin").append(".*.tex\n");

			msg.append("Finding all m3 models that end with aurin_f:\n");
			msg.append("aurin_f.m3\n");

			msg.append("Finding only m3 models that are named aurin_f:\n");
			msg.append(sep).append("aurin_f.m3\n");

			return msg.toString();
		});

		return null;
	}

}
