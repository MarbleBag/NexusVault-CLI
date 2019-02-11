package nexusvault.cli.plugin.show;

import nexusvault.cli.App;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;
import nexusvault.cli.plugin.AbstCommand;

final class ShowCmd extends AbstCommand {

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName("show")
				.setDescription("(console mode) Shows data which are associated with the given argument. Use '?' to recieve a list of possible arguments")
				.setRequired(false)
				.setArguments(true)
				.setNumberOfArguments(1)
				.setNamesOfArguments("arg")
			    .build();
		//@formatter:on
	}

	@Override
	public void onCommand(CommandArguments args) {
		if (args.getNumberOfArguments() != 1) {
			sendMsg("Use '?' to recieve a list of possible arguments");
			return;
		}
		final String arg0 = args.getArg(0).trim().toLowerCase();
		App.getInstance().getPlugIn(ShowPlugIn.class).show(arg0);
	}

	@Override
	public void onHelp(CommandArguments args) {
		App.getInstance().getPlugIn(ShowPlugIn.class).showHelp();
	}

}
