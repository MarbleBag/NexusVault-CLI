package nexusvault.cli.extensions.base.command;

import java.nio.file.Path;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.cmd.Argument;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.ArgumentHandler;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;
import nexusvault.cli.extensions.base.AppBaseExtension;

@AutoInstantiate
final class AppOutputPath implements ArgumentHandler, CommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("output")
				.setDescription("output directory, if not set, a new directory will be created within the app directory")
				.setNoNamedArguments()
				.build();
		//@formatter:on
	}

	@Override
	public ArgumentDescription getArgumentDescription() {
		// @formatter:off
		return ArgumentDescription.newInfo()
				.setName("output")
				.setDescription("output directory, if not set, a new directory will be created within the app directory")
				.setRequired(false)
				.setArguments(false)
				.setNumberOfArguments(1)
				.setNamesOfArguments("path")
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		final Path path = Path.of(args.getUnnamedArgs()[0]);
		App.getInstance().getExtensionManager().getExtension(AppBaseExtension.class).setOutputPath(path);
	}

	@Override
	public void execute(Argument args) {
		final Path path = Path.of(args.getValue());
		App.getInstance().getExtensionManager().getExtension(AppBaseExtension.class).setOutputPath(path);
	}

	@Override
	public String onHelp() {
		return null;
	}

}
