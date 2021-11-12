package nexusvault.cli.extensions.show.command;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.cmd.AbstractCommandHandler;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.extensions.show.ShowExtension;

@AutoInstantiate
public final class Show extends AbstractCommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("show")
				.setDescription("(console mode) Shows data which are associated with the given argument. Use '?' to recieve a list of possible arguments")
				.setNoNamedArguments()
			    .build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		if (args.getUnnamedArgumentSize() != 1) {
			sendMsg("Use '?' to recieve a list of possible arguments");
			return;
		}

		final String trigger = args.getUnnamedArgs()[0].trim().toLowerCase();
		App.getInstance().getExtension(ShowExtension.class).show(trigger);
	}

	@Override
	public String onHelp() {
		App.getInstance().getExtension(ShowExtension.class).showHelp();
		return null;
	}

}
