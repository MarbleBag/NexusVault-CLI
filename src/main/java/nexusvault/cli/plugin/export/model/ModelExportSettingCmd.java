package nexusvault.cli.plugin.export.model;

import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;
import nexusvault.cli.ConsoleSystem.Level;

final class ModelExportSettingCmd implements Command {

	private static final String CMD_NAME = "export-m3-texture";
	private final ModelExporter modelExporter;

	public ModelExportSettingCmd(ModelExporter modelExporter) {
		this.modelExporter = modelExporter;
	}

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName(CMD_NAME)
				.setDescription("Toggles the usage of textures in exporter. By default, each exporter includes textures if supported. Can be set directly to 'on' or 'off'")
				.setRequired(false)
				.setArguments(true)
				.setNumberOfArguments(1)
				.setNamesOfArguments("on/off")
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(CommandArguments args) {
		if (args.getNumberOfArguments() == 0) {
			modelExporter.getConfig().setIncludeTextureh(!modelExporter.getConfig().isIncludeTexture());
			return;
		}

		final String arg0 = args.getArg(0).trim().toLowerCase();
		if ("off".equals(arg0)) {
			modelExporter.getConfig().setIncludeTextureh(false);
		} else if ("on".equals(arg0)) {
			modelExporter.getConfig().setIncludeTextureh(true);
		} else {
			App.getInstance().getConsole().println(Level.CONSOLE,
					() -> String.format("Command '%s' does not accept '%s' as an argument. Use 'off' or 'on'.", CMD_NAME, arg0));
		}
	}

	@Override
	public void onHelp(CommandArguments args) {
		final String msg = String.format("Usage of textures is set to %s, use 'on' or 'off' to directly set the value",
				modelExporter.getConfig().isIncludeTexture());
		App.getInstance().getConsole().println(Level.CONSOLE, msg);
	}
}