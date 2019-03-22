package nexusvault.cli.plugin.archive;

import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;

final class NavigatorListDirectoryContentCmd implements Command {

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName("ls")
				.setDescription("list the content of TODO")
				.setRequired(false)
				.setNoArguments()
			    .build();
		//@formatter:on
	}

	@Override
	public void onCommand(CommandArguments args) {
		App.getInstance().getPlugIn(ArchivePlugIn.class).listDirectoryContent();
	}

	@Override
	public void onHelp(CommandArguments args) {
		// TODO Auto-generated method stub

	}

}
