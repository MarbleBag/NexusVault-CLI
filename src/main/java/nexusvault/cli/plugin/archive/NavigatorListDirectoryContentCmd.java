package nexusvault.cli.plugin.archive;

import nexusvault.cli.App;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;

final class NavigatorListDirectoryContentCmd implements CommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("ls")
				.setDescription("List the contents of the currently selected folder")
				.setNoNamedArguments()
			    .build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		App.getInstance().getPlugIn(ArchivePlugIn.class).listDirectoryContent();
	}

	@Override
	public String onHelp() {
		return null;
	}

}
