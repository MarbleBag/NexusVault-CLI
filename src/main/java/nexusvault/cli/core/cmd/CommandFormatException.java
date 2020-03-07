package nexusvault.cli.core.cmd;

public final class CommandFormatException extends CLIHandlerException {

	private static final long serialVersionUID = 1L;

	public CommandFormatException() {
		super();
	}

	public CommandFormatException(String message) {
		super(message);
	}

	public CommandFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandFormatException(Throwable cause) {
		super(cause);
	}
}
