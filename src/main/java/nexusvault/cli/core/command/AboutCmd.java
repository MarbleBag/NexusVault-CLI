package nexusvault.cli.core.command;

import nexusvault.cli.core.App;
import nexusvault.cli.core.cmd.AbstractCommandHandler;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;

public final class AboutCmd extends AbstractCommandHandler implements CommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("about")
				.setDescription("shows the about page")
				.setNoNamedArguments()
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		final var builder = new StringBuilder();
		builder.append("NexusVault CLI").append('\n');
		builder.append("Version: ").append(App.getInstance().getAppConfig().getApplicationVersion()).append('\n');
		builder.append("Copyright 2019-2021 MarbleBag").append('\n');
		builder.append("Source available @ https://github.com/MarbleBag/NexusVault-CLI").append('\n');
		sendMsg(builder.toString());
	}

	@Override
	public String onHelp() {
		return null;
	}

}
