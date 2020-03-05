package nexusvault.cli.plugin.archive;

import nexusvault.cli.App;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandFormatException;
import nexusvault.cli.core.cmd.CommandHandler;

final class NavigatorChangeDirectoryCmd implements CommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("cd")
				.setDescription("Change directory - select a folder")
				.setNoNamedArguments()
//				.addArgument(
//						ArgumentDescription.newInfo()
//						.setName("value")
//						.setNameShort("v")
//						.setDescription("target path")
//						.setRequired(true)
//						.setArguments(false)
//						.setNumberOfArguments(1)
//						.setNamesOfArguments("target")
//						.build()
//						)
//				.setArgumentsDone()
			    .build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		if (args.getUnnamedArgumentSize() == 0) {
			throw new CommandFormatException("");
		}
		final String target = args.getUnnamedArgs()[0];
		App.getInstance().getPlugIn(ArchivePlugIn.class).changeDirectory(target);
	}

	@Override
	public String onHelp() {
		return "Changes the current directory. Either '/' or '\\' can be used to separate folder names. To move to the parent folder use '..'";
	}

}
