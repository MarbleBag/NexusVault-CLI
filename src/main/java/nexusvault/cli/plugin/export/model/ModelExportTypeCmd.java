package nexusvault.cli.plugin.export.model;

import java.util.Arrays;

import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;
import nexusvault.cli.ConsoleSystem.Level;

final class ModelExportTypeCmd implements Command {

	private final ModelExporter modelExporter;

	public ModelExportTypeCmd(ModelExporter modelExporter) {
		this.modelExporter = modelExporter;
	}

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName("export-m3-type")
				.setDescription("Sets the exporter type. Use '?' to get more informations.")
				.setRequired(false)
				.setArguments(true)
				.setNumberOfArguments(1)
				.setNamesOfArguments("type")
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(CommandArguments args) {
		if (args.getNumberOfArguments() != 1) {
			App.getInstance().getConsole().println(Level.CONSOLE, "Use '?' to get more informations.");
			return;
		}

		final String arg0 = args.getArg(0);

		ExporterType selectedType = null;
		for (final ExporterType type : ExporterType.values()) {
			if (type.name().equalsIgnoreCase(arg0)) {
				selectedType = type;
				break;
			}
		}

		if (selectedType == null) {
			App.getInstance().getConsole().println(Level.CONSOLE, String.format("'%s' is not a valid argument. Check '?' for more informations", arg0));
			return;
		}

		modelExporter.setExportType(selectedType);
	}

	@Override
	public void onHelp(CommandArguments args) {
		final String msg = "Available m3 exporter types: " + Arrays.toString(ExporterType.values());
		App.getInstance().getConsole().println(Level.CONSOLE, msg);
	}
}