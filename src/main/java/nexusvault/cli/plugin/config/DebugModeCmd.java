package nexusvault.cli.plugin.config;

import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;
import nexusvault.cli.ConsoleSystem.Level;

final class DebugModeCmd implements Command {

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName("debug")
				.setDescription("toggles between debug mode without argument. Can be set directly to 'on' or 'off'")
				.setRequired(false)
				.setArguments(true)
				.setNumberOfArguments(1)
				.setNamesOfArguments("on/off")
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(CommandArguments args) {
		final AppBasePlugIn configPlugin = App.getInstance().getPlugInSystem().getPlugIn(AppBasePlugIn.class);

		if (args.getNumberOfArguments() == 0) {
			configPlugin.setDebugMode(!configPlugin.getDebugMode());
		}

		final String arg0 = args.getArg(0).trim().toLowerCase();
		if ("off".equals(arg0)) {
			configPlugin.setDebugMode(false);
		} else if ("on".equals(arg0)) {
			configPlugin.setDebugMode(true);
		} else {
			App.getInstance().getConsole().println(Level.CONSOLE,
					() -> String.format("Command 'debug' does not accept '%s' as an argument. Use 'off' or 'on'.", arg0));
		}
	}

	@Override
	public void onHelp(CommandArguments args) {
		App.getInstance().getConsole().println(Level.CONSOLE,
				() -> "Sets the debug mode. If no argument is supplied, the debug mode will be toggled to 'on' if it was 'off' or 'off' if it was 'on'. The debug mode can be directly set with one of the arguments 'on'/'off'.");
	}

}
