package nexusvault.cli;

import nexusvault.cli.ConsoleSystem.Level;

final class DebugCmd implements Command {

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName("debug")
				.setDescription("toggles debug mode. Can be set to 'on' or 'off'")
				.setRequired(false)
				.setArguments(true)
				.setNumberOfArguments(1)
				.setNamesOfArguments("on/off")
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(CommandArguments args) {
		boolean setDebugTo = !App.getInstance().getAppConfig().getDebugMode();
		if (args.getNumberOfArguments() != 0) {
			final String arg0 = args.getArg(0).trim().toLowerCase();
			if ("off".equals(arg0)) {
				setDebugTo = false;
			} else if ("on".equals(arg0)) {
				setDebugTo = true;
			} else {
				App.getInstance().getConsole().println(Level.CONSOLE,
						() -> String.format("Cmd 'debug' does not accept '%s' as an argument. Try 'off' or 'on'.", arg0));
				return;
			}
		}
		App.getInstance().getAppConfig().setDebugMode(setDebugTo);
	}

	@Override
	public void onHelp(CommandArguments args) {
		App.getInstance().getConsole().println(Level.CONSOLE,
				() -> "Sets the debug mode. If no argument is supplied, the debug mode will be toggled to 'on' if it was 'off' or 'off' if it was 'on'. The debug mode can be directly set with one of the arguments 'on'/'off'.");
	}

}
