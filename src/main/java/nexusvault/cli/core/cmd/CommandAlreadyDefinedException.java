package nexusvault.cli.core.cmd;

public final class CommandAlreadyDefinedException extends CLIHandlerException {

	private static final long serialVersionUID = 1L;

	public CommandAlreadyDefinedException() {
		super();
	}

	public CommandAlreadyDefinedException(String message) {
		super(message);
	}

	public CommandAlreadyDefinedException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandAlreadyDefinedException(Throwable cause) {
		super(cause);
	}
}