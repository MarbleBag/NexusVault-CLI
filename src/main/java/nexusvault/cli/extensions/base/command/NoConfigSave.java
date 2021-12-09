package nexusvault.cli.extensions.base.command;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.Console.Level;
import nexusvault.cli.core.cmd.Argument;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.ArgumentHandler;
import nexusvault.cli.extensions.base.AppBaseExtension;

@AutoInstantiate
final class NoConfigSave implements ArgumentHandler {

	@Override
	public ArgumentDescription getArgumentDescription() {
		// @formatter:off
		return ArgumentDescription.newInfo()
				.setName("config-no-save")
				.setDescription("If set, the config will not be saved at program exit. Can be set directly to 'on' or 'off'")
				.setRequired(false)
				.setArguments(true)
				.setNumberOfArguments(1)
				.setNamesOfArguments("on/off")
			    .build();
		//@formatter:on
	}

	@Override
	public void execute(Argument arg) {
		final AppBaseExtension configPlugin = App.getInstance().getExtensionManager().getExtension(AppBaseExtension.class);

		if (arg.getValue() == null) {
			configPlugin.setConfigNotSaveable(true);
		}

		final String arg0 = arg.getValue().trim().toLowerCase();
		if ("off".equals(arg0)) {
			configPlugin.setConfigNotSaveable(false);
		} else if ("on".equals(arg0)) {
			configPlugin.setConfigNotSaveable(true);
		} else {
			App.getInstance().getConsole().println(Level.CONSOLE,
					() -> String.format("Command 'config-no-save' does not accept '%s' as an argument. Use 'off' or 'on'.", arg0));
		}
	}

}
