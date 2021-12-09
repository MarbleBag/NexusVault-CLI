package nexusvault.cli.core;

import nexusvault.cli.core.cmd.ArgumentHandler;
import nexusvault.cli.core.cmd.CommandHandler;

public interface CommandLineManager {

	void registerCommand(CommandHandler cmd);

	void unregisterCommand(CommandHandler cmd);

	/**
	 * Will be executed on program start
	 */
	void registerArgument(ArgumentHandler handler);

	// void printHelp();

}
