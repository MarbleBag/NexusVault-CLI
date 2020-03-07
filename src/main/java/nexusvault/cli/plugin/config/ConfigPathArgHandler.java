package nexusvault.cli.plugin.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import nexusvault.cli.App;
import nexusvault.cli.core.cmd.Argument;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.ArgumentHandler;

final class ConfigPathArgHandler implements ArgumentHandler {

	@Override
	public ArgumentDescription getArgumentDescription() {
		// @formatter:off
		return ArgumentDescription.newInfo()
				.setName("config")
				.setNameShort("c")
				.setDescription("path to the config-file that should be loaded")
				.setRequired(false)
				.setArguments(false)
				.setNumberOfArguments(1)
				.setNamesOfArguments("path")
				.build();
		//@formatter:on
	}

	@Override
	public void execute(Argument args) {
		final Path path = Paths.get(args.getValue());
		App.getInstance().getPlugInSystem().getPlugIn(AppBasePlugIn.class).setConfigPath(path);
	}

}
