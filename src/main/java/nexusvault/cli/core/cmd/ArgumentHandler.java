package nexusvault.cli.core.cmd;

public interface ArgumentHandler {
	ArgumentDescription getArgumentDescription();

	void execute(Argument args);
}