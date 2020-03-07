package nexusvault.cli.plugin.config;

import nexusvault.cli.App;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;

final class ConfigReloadCmd implements CommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("config-reload")
				.setDescription("reloads the current config")
				.setNoNamedArguments()
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		App.getInstance().getPlugInSystem().getPlugIn(AppBasePlugIn.class).reloadConfig();
	}

	@Override
	public String onHelp() {
		return null;
	}

}
