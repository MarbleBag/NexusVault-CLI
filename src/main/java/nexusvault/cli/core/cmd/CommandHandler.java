package nexusvault.cli.core.cmd;

public interface CommandHandler {
	CommandDescription getCommandDescription();

	void onCommand(Arguments args);

	String onHelp();
}