package nexusvault.cli.core.cmd;

public final class ArgumentFormatException extends CLIHandlerException {

	private static final long serialVersionUID = 1L;

	public ArgumentFormatException() {
		super();
	}

	public ArgumentFormatException(String message) {
		super(message);
	}

	public ArgumentFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArgumentFormatException(Throwable cause) {
		super(cause);
	}
}
