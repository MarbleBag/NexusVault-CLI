package nexusvault.cli.extensions.export.command;

import java.util.HashMap;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterExtension;
import nexusvault.cli.extensions.export.ExportExtension;
import nexusvault.cli.extensions.search.SearchExtension;

@AutoInstantiate
public final class Export implements CommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		final var builder = CommandDescription.newInfo()
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
						.setName("separate")
						.setDescription("export files into separate folders, each folder will only contain files specific to the exported file")
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
						);
		//@formatter:on

		for (final var arg : App.getInstance().getExtension(ConverterExtension.class).getCLIOptions()) {
			builder.addNamedArgument(arg);
		}

		return builder.ignoreUnnamedArguments().namedArgumentsDone().build();
	}

	@Override
	public void onCommand(Arguments args) {
		final var exportAsBinary = args.isNamedArgumentSet("binary");
		final var separateExports = args.isNamedArgumentSet("separate");
		final var searchResults = App.getInstance().getExtension(SearchExtension.class).getLastSearchResults();

		if (exportAsBinary) {
			App.getInstance().getExtension(ExportExtension.class).exportAsBinary(searchResults, separateExports);
		} else {

			final var useConverters = new HashMap<String, String>();
			if (args.isNamedArgumentSet("converters")) {
				for (final var mapping : args.getArgumentByName("converters").getValues()) {
					final var ids = mapping.split("->");
					if (ids.length == 2) {
						useConverters.put(ids[0], ids[1]);
					}
				}
			}

			App.getInstance().getExtension(ExportExtension.class).exportViaConverters(searchResults, separateExports, useConverters, new ConverterArgs(args));
		}
	}

	@Override
	public String onHelp() {
		return "See 'convert-file help' for a list of supported file formats.";
	}

}
