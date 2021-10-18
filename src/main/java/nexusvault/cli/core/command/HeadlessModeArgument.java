package nexusvault.cli.core.command;

import nexusvault.cli.core.App;
import nexusvault.cli.core.cmd.Argument;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.ArgumentHandler;

/**
 * No use for this
 */
@Deprecated
public final class HeadlessModeArgument implements ArgumentHandler {

	@Override
	public ArgumentDescription getArgumentDescription() {
		// @formatter:off
		return ArgumentDescription.newInfo()
				.setName("no-console")
				.setDescription("starts the application in headless mode: after processing the start arguments, the application will terminate. This command will only be read at startup.")
				.setRequired(false)
				.setNoArguments()
			    .build();
		//@formatter:on
	}

	@Override
	public void execute(Argument args) {
		App.getInstance().setHeadlessMode();
	}

}
