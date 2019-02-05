package nexusvault.cli.plugin.config;

import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;

final class ConfigReloadCmd implements Command {

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName("config-reload")
				.setDescription("reloads the current config")
				.setRequired(false)
				.setNoArguments()
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(CommandArguments args) {
		App.getInstance().getPlugInSystem().getPlugIn(AppBasePlugIn.class).reloadConfig();
	}

	@Override
	public void onHelp(CommandArguments args) {
		// TODO Auto-generated method stub
	}

}
