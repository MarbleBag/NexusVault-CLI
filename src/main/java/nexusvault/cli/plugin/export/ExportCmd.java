package nexusvault.cli.plugin.export;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nexusvault.archive.IdxPath;
import nexusvault.cli.App;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.plugin.AbstractCommandHandler;
import nexusvault.cli.plugin.export.ExportPlugIn.ExportConfig;
import nexusvault.cli.plugin.search.SearchPlugIn;

final class ExportCmd extends AbstractCommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("export")
				.setDescription("Export the last searched entries from the archive. Use '?' to get more informations.")
				.addNamedArgument(
							ArgumentDescription.newInfo()
							.setName("binary")
							.setDescription("exports files unprocessed")
							.setRequired(false)
							.setNoArguments()
							.build()
						)
				.namedArgumentsDone()
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		final ExportConfig exportConfig = new ExportConfig();

		if (args.isNamedArgumentSet("binary")) {
			exportConfig.exportAsBinary(true);
		}

		final List<IdxPath> searchResults = App.getInstance().getPlugIn(SearchPlugIn.class).getLastSearchResults();
		App.getInstance().getPlugIn(ExportPlugIn.class).exportIdxPath(searchResults, exportConfig);
	}

	@Override
	public String onHelp(Arguments args) {
		// sendMsg("Reads the entries of the '" + FILE
		// + "' file in the report folder and tries to extract one after another from the game archive. If the first argument of this cmd is '"
		// + CMD_AS_BINARY
		// + "', the extracted data will be saved 'as-is' on the disk, otherwise the extracted data will be converted, if possible, to a more known format. In
		// case no converter is available, the extracted data will not be saved, instead an error report will be written. Is the first argument '"
		// + CMD_EVERYTHING + "' the behaviour changes and extracted data without an available converter will be saved as binaries.");
		//
		// final List<String> supportedFileTypes = new LinkedList<>();
		// for (final Extractor e : resources.getExtractors()) {
		// supportedFileTypes.addAll(e.getAcceptedFileEndings());
		// }
		// Collections.sort(supportedFileTypes);
		//
		// sendMsg("File types with converter: " + String.join(", ", supportedFileTypes));

		final Set<String> supportedFileTypes = new HashSet<>();
		for (final Exporter exporter : App.getInstance().getPlugIn(ExportPlugIn.class).getExporters()) {
			supportedFileTypes.addAll(exporter.getAcceptedFileEndings());
		}

		final var builder = new StringBuilder();
		builder.append("Supported file types: ").append(String.join(", ", supportedFileTypes));
		return builder.toString();
	}

}