package nexusvault.cli.core.cmd;

public final class HandlerAlreadyDefinedException extends CLIHandlerException {

	private static final long serialVersionUID = 1L;

	public HandlerAlreadyDefinedException() {
		super();
	}

	public HandlerAlreadyDefinedException(String message) {
		super(message);
	}

	public HandlerAlreadyDefinedException(String message, Throwable cause) {
		super(message, cause);
	}

	public HandlerAlreadyDefinedException(Throwable cause) {
		super(cause);
	}
}