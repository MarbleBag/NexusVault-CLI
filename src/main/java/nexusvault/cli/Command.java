package nexusvault.cli;

public interface Command {
	CommandInfo getCommandInfo();

	void onCommand(CommandArguments args);

	void onHelp(CommandArguments args);
}