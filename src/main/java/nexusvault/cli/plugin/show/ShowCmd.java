package nexusvault.cli.plugin.show;

import nexusvault.cli.App;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.plugin.AbstractCommandHandler;

final class ShowCmd extends AbstractCommandHandler {

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
		final String arg0 = args.getUnnamedArgs()[0].trim().toLowerCase();
		App.getInstance().getPlugIn(ShowPlugIn.class).show(arg0);
	}

	@Override
	public String onHelp(Arguments args) {
		App.getInstance().getPlugIn(ShowPlugIn.class).showHelp();
		return null;
	}

}
