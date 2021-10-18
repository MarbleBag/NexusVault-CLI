package nexusvault.cli.plugin.export.tex;

import nexusvault.cli.core.App;
import nexusvault.cli.core.Console.Level;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;

final class TextureExportSplitCmd implements CommandHandler {

	private static final String CMD_NAME = "export-tex-split";
	private final TextureExporter exporter;

	public TextureExportSplitCmd(TextureExporter exporter) {
		this.exporter = exporter;
	}

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName(CMD_NAME)
				.setDescription("Toggles the splitting of textures when exported exporter. By default, this is on. Can be set directly to 'on' or 'off'. Some textures use their color and alpha channels to store different information, like normal maps, masks and roughness. If set to on, the exporter will export the original image, then images containing specific sub-information")
				.setNoNamedArguments()
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		if (args.getUnnamedArgumentSize() == 0) {
			this.exporter.getConfig().setSplitTexture(!this.exporter.getConfig().isSplitTexture());
			return;
		}

		final String arg0 = args.getUnnamedArgs()[0].trim().toLowerCase();
		if ("off".equals(arg0)) {
			this.exporter.getConfig().setSplitTexture(false);
		} else if ("on".equals(arg0)) {
			this.exporter.getConfig().setSplitTexture(true);
		} else {
			App.getInstance().getConsole().println(Level.CONSOLE,
					() -> String.format("Command '%s' does not accept '%s' as an argument. Use 'off' or 'on'.", CMD_NAME, arg0));
		}
	}

	@Override
	public String onHelp() {
		final String msg = String.format("Splitting of textures is set to %s, use 'on' or 'off' to directly set the value",
				this.exporter.getConfig().isSplitTexture());
		return msg;
	}
}