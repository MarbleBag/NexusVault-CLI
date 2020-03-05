package nexusvault.cli;

import nexusvault.cli.core.cmd.ArgumentHandler;
import nexusvault.cli.core.cmd.CommandHandler;

public interface CLISystem {

	void registerCommand(CommandHandler cmd);

	void unregisterCommand(CommandHandler cmd);

	/**
	 * Will be executed on program start
	 */
	void registerStartArgumentHandler(ArgumentHandler handler);

	// void printHelp();

}
