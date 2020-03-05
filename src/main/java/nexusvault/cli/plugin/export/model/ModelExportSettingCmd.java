package nexusvault.cli.plugin.export.model;

import nexusvault.cli.App;
import nexusvault.cli.ConsoleSystem.Level;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;

final class ModelExportSettingCmd implements CommandHandler {

	private static final String CMD_NAME = "export-m3-texture";
	private final ModelExporter modelExporter;

	public ModelExportSettingCmd(ModelExporter modelExporter) {
		this.modelExporter = modelExporter;
	}

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName(CMD_NAME)
				.setDescription("Toggles the usage of textures in exporter. By default, each exporter includes textures if supported. Can be set directly to 'on' or 'off'")
				.setNoNamedArguments()
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		if (args.getUnnamedArgumentSize() == 0) {
			this.modelExporter.getConfig().setIncludeTextureh(!this.modelExporter.getConfig().isIncludeTexture());
			return;
		}

		final String arg0 = args.getUnnamedArgs()[0].trim().toLowerCase();
		if ("off".equals(arg0)) {
			this.modelExporter.getConfig().setIncludeTextureh(false);
		} else if ("on".equals(arg0)) {
			this.modelExporter.getConfig().setIncludeTextureh(true);
		} else {
			App.getInstance().getConsole().println(Level.CONSOLE,
					() -> String.format("Command '%s' does not accept '%s' as an argument. Use 'off' or 'on'.", CMD_NAME, arg0));
		}
	}

	@Override
	public String onHelp() {
		final String msg = String.format("Usage of textures is set to %s, use 'on' or 'off' to directly set the value",
				this.modelExporter.getConfig().isIncludeTexture());
		return msg;
	}
}