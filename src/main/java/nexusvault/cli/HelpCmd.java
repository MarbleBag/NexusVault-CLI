package nexusvault.cli;

import java.util.function.Consumer;

import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;

final class HelpCmd implements CommandHandler {

	private final Consumer<Arguments> onCall;

	public HelpCmd(Consumer<Arguments> onCmd) {
		if (onCmd == null) {
			throw new IllegalArgumentException("'onCall' must not b enull");
		}
		this.onCall = onCmd;
	}

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("help")
				.addAlternativeNames("?")
				.setDescription("displays all available commands and descriptions")
				.addNamedArgument(
							ArgumentDescription.newInfo()
							.setName("cmd").setDescription("show commands only")
							.setRequired(false).setNoArguments()
							.build()
						)
				.addNamedArgument(
						ArgumentDescription.newInfo()
						.setName("args").setDescription("show application arguments only")
						.setRequired(false).setNoArguments()
						.build()
					)
				.namedArgumentsDone()
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		this.onCall.accept(args);
	}

	@Override
	public String onHelp() {
		return null;
	}

}
