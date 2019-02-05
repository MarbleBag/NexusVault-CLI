package nexusvault.cli;

import java.util.function.Consumer;

final class ExitCmd implements Command {

	private final Consumer<CommandArguments> onCall;

	public ExitCmd(Consumer<CommandArguments> onCmd) {
		if (onCmd == null) {
			throw new IllegalArgumentException("'onCall' must not b enull");
		}
		this.onCall = onCmd;
	}

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName("exit")
				.setDescription("Instructs the application to finish all running tasks and close it on complition")
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
		// TODO Auto-generated method stub
	}
}
