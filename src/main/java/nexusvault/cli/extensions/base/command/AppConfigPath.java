package nexusvault.cli.extensions.base.command;

import java.nio.file.Path;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.cmd.Argument;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.ArgumentHandler;

@AutoInstantiate
public final class AppConfigPath implements ArgumentHandler {

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
		final Path path = Path.of(args.getValue());
		App.getInstance().getAppConfig().setConfigPath(path);
	}

}
