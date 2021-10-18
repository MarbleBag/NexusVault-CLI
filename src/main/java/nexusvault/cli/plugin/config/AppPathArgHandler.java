package nexusvault.cli.plugin.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import nexusvault.cli.core.App;
import nexusvault.cli.core.cmd.Argument;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.ArgumentHandler;

final class AppPathArgHandler implements ArgumentHandler {

	@Override
	public ArgumentDescription getArgumentDescription() {
		// @formatter:off
		return ArgumentDescription.newInfo()
				.setName("app")
				.setDescription("application root directory. If not set, the directory of the executeable is used as the root directory")
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
		App.getInstance().getExtensionManager().getExtension(AppBasePlugIn.class).setApplicationPath(path);
	}
}