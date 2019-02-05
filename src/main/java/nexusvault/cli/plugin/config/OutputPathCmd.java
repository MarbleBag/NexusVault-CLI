package nexusvault.cli.plugin.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;

final class OutputPathCmd implements Command {

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName("output")
				.setNameShort("o")
				.setDescription("output directory, if not set, a new directory will be created within the app directory")
				.setRequired(false)
				.setArguments(false)
				.setNumberOfArguments(1)
				.setNamesOfArguments("path")
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(CommandArguments args) {
		final Path path = Paths.get(args.getArg(0));
		App.getInstance().getPlugInSystem().getPlugIn(AppBasePlugIn.class).setOutputPath(path);
	}

	@Override
	public void onHelp(CommandArguments args) {
		// TODO Auto-generated method stub
	}

}
