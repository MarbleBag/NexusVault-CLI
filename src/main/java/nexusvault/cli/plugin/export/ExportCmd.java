package nexusvault.cli.plugin.export;

import java.util.HashSet;
import java.util.Set;

import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;
import nexusvault.cli.ConsoleSystem.Level;
import nexusvault.cli.plugin.export.ExportPlugIn.ExportRequest;
import nexusvault.cli.plugin.search.SearchPlugIn;

final class ExportCmd implements Command {

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName("export")
				.setNameShort("e")
				.setDescription("Export the last searched entries from the archive. Use '?' to get more informations.")
				.setRequired(false)
				.setArguments(true)
				.setNumberOfArguments(1)
				.setNamesOfArguments("flags")
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(CommandArguments args) {
		final ExportRequest exportRequest = new ExportRequest();

		if (args.getNumberOfArguments() != 0) {
			if ("as-binary".equalsIgnoreCase(args.getArg(0))) {
				exportRequest.exportAsBinary(true);
			}
		}

		App.getInstance().getPlugIn(SearchPlugIn.class).getLastSearchResults();

		App.getInstance().getPlugIn(ExportPlugIn.class).export(exportRequest);
	}

	@Override
	public void onHelp(CommandArguments args) {
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
		sendMsg("File types with converter: " + String.join(", ", supportedFileTypes));
	}

	private void sendMsg(String msg) {
		App.getInstance().getConsole().println(Level.CONSOLE, msg);
	}

}