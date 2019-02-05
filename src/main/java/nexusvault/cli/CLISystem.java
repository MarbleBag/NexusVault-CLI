package nexusvault.cli;

public interface CLISystem {

	void registerCommand(Command cmd);

	void unregisterCommand(Command cmd);

	void printHelp();

}
