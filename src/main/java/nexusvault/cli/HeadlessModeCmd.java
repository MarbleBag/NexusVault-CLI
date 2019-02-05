package nexusvault.cli;

final class HeadlessModeCmd implements Command {

	public static final String CLI_SYNTAX = "no-console";

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName(CLI_SYNTAX)
				.setDescription("starts the application in headless mode: after processing the start arguments, the application will terminate. This command will only be read at startup.")
				.setRequired(false)
				.setNoArguments()
			    .build();
		//@formatter:on
	}

	@Override
	public void onCommand(CommandArguments args) {
		// App.getInstance().getPlugInSystem().getPlugIn(AppBasePlugIn.class).setHeadlessMode();
	}

	@Override
	public void onHelp(CommandArguments args) {
		// TODO Auto-generated method stub
	}

}
