package nexusvault.cli.plugin.archive;

import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;

final class NavigatorChangeDirectoryCmd implements Command {

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName("cd")
				.setDescription("list the content of TODO")
				.setRequired(false)
				.setArguments(false)
				.setNumberOfArguments(1)
				.setNamesOfArguments("path")
			    .build();
		//@formatter:on
	}

	@Override
	public void onCommand(CommandArguments args) {
		final String target = args.getArg(0);
		App.getInstance().getPlugIn(ArchivePlugIn.class).changeDirectory(target);
	}

	@Override
	public void onHelp(CommandArguments args) {
		// TODO Auto-generated method stub

	}

}
