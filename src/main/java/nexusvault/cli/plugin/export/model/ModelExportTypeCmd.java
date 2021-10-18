package nexusvault.cli.plugin.export.model;

import java.util.Arrays;

import nexusvault.cli.core.App;
import nexusvault.cli.core.Console.Level;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;

final class ModelExportTypeCmd implements CommandHandler {

	private final ModelExporter modelExporter;

	public ModelExportTypeCmd(ModelExporter modelExporter) {
		this.modelExporter = modelExporter;
	}

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("export-m3-type")
				.setDescription("Sets the exporter type. Use '?' to get more informations.")
				.setNoNamedArguments()
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		if (args.getUnnamedArgumentSize() != 1) {
			App.getInstance().getConsole().println(Level.CONSOLE, "Use '?' to get more informations.");
			return;
		}

		final String arg0 = args.getUnnamedArgs()[0];

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

		this.modelExporter.setExportType(selectedType);
	}

	@Override
	public String onHelp() {
		final String msg = "Available m3 exporter types: " + Arrays.toString(ExporterType.values());
		return msg;
	}
}