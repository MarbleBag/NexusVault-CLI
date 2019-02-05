package nexusvault.cli;

import java.util.function.Consumer;

final class HelpCmd implements Command {

	private final Consumer<CommandArguments> onCall;

	public HelpCmd(Consumer<CommandArguments> onCmd) {
		if (onCmd == null) {
			throw new IllegalArgumentException("'onCall' must not b enull");
		}
		this.onCall = onCmd;
	}

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
				return CommandInfo.newInfo()
						.setName("help")
						.setNameShort("?")
						.setDescription("displays all available commands and descriptions")
						.setRequired(false)
						.setNoArguments()
						.build();
				//@formatter:on
	}

	@Override
	public void onCommand(CommandArguments args) {
		onCall.accept(args);
	}

	@Override
	public void onHelp(CommandArguments args) {

	}

}
