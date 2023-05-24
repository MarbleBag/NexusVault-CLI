package nexusvault.cli.extensions.base.command;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.Console.Level;
import nexusvault.cli.core.cmd.Argument;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.ArgumentHandler;

@AutoInstantiate
final class DebugMode implements ArgumentHandler {

	@Override
	public ArgumentDescription getArgumentDescription() {
		// @formatter:off
		return ArgumentDescription.newInfo()
				.setName("debug")
				.setDescription("Sets the debug mode. If no argument is supplied, the debug mode will be toggled to 'on' if it was 'off' or 'off' if it was 'on'. The debug mode can be directly set with one of the arguments 'on'/'off'.")
				.setRequired(false)
				.setArguments(true)
				.setNumberOfArguments(1)
				.setNamesOfArguments("on/off")
				.build();
		//@formatter:on
	}

	@Override
	public void execute(Argument arg) {
		final var config = App.getInstance().getAppConfig();

		if (arg.getValue() == null) {
			config.setDebugMode(!config.getDebugMode());
			return;
		}

		final String arg0 = arg.getValue().trim().toLowerCase();
		if ("off".equals(arg0)) {
			config.setDebugMode(false);
		} else if ("on".equals(arg0)) {
			config.setDebugMode(true);
		} else {
			App.getInstance().getConsole().println(Level.CONSOLE,
					() -> String.format("Command 'debug' does not accept '%s' as an argument. Use 'off' or 'on'.", arg0));
		}
	}

}
