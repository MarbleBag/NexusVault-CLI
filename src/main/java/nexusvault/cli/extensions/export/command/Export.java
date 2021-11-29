package nexusvault.cli.extensions.export.command;

import java.util.HashMap;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.export.ExportExtension;
import nexusvault.cli.extensions.search.SearchExtension;

@AutoInstantiate
public final class Export implements CommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("export")
				.setDescription("Exports the last searched entries from the archive.")
				.addNamedArgument(
							ArgumentDescription.newInfo()
							.setName("binary")
							.setDescription("export files unprocessed")
							.setRequired(false)
							.setNoArguments()
							.build()
						)
				.addNamedArgument(
							ArgumentDescription.newInfo()
							.setName("converters")
							.setNameShort("cvts")
							.setDescription("")
							.setRequired(false)
							.setArguments(false)
							.setNumberOfArgumentsUnlimited()
							.setNamesOfArguments("ext->id")
							.build()
						)

				.namedArgumentsDone()
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		final var exportAsBinary = args.isNamedArgumentSet("binary");
		final var searchResults = App.getInstance().getExtension(SearchExtension.class).getLastSearchResults();

		if (exportAsBinary) {
			App.getInstance().getExtension(ExportExtension.class).exportAsBinary(searchResults);
		} else {

			final var useConverters = new HashMap<String, String>();
			if (args.isNamedArgumentSet("converters")) {
				for (final var use : args.getArgumentByName("converters").getValues()) {
					final var extId = use.split(">");
					if (extId.length == 2) {
						useConverters.put(extId[0], extId[1]);
					}
				}
			}

			App.getInstance().getExtension(ExportExtension.class).exportViaConverters(searchResults, useConverters, new ConverterArgs(args));
		}
	}

	@Override
	public String onHelp() {
		return "See 'convert-file help' for a list of supported file formats.";
	}

}
