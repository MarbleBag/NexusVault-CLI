package nexusvault.cli.extensions.base.command;

import nexusvault.cli.core.App;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;

// @AutoInstantiate
final class ReloadConfig implements CommandHandler {

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
		App.getInstance().getAppConfig().reloadConfig();
	}

	@Override
	public String onHelp() {
		return null;
	}

}
