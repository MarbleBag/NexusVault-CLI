package nexusvault.cli.core.command;

import java.util.function.Consumer;

import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;

public final class SetCmd implements CommandHandler {

	private final Consumer<Arguments> onCmd;

	public SetCmd(Consumer<Arguments> onCmd) {
		if (onCmd == null) {
			throw new IllegalArgumentException("'onCmd' must not b enull");
		}
		this.onCmd = onCmd;
	}

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("set")
				.setDescription("")
				.setNoNamedArguments()
//				.addArgument(
//						ArgumentDescription.newInfo()
//						.setName("options")
//						.setNameShort("o")
//						.setDescription("options to set")
//						.setRequired(true)
//						.setArguments(false)
//						.setNumberOfArgumentsUnlimited()
//						.setNamesOfArguments("<option, ...>")
//						.build()
//						)
//				.ignoreNoOptions()
//				.setArgumentsDone()
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		this.onCmd.accept(args);
	}

	@Override
	public String onHelp() {
		return null;
	}

}
