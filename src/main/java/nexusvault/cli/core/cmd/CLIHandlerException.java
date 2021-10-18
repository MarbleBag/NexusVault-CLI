package nexusvault.cli.core.cmd;

import nexusvault.cli.core.exception.AppException;

public abstract class CLIHandlerException extends AppException {

	private static final long serialVersionUID = 1L;

	public CLIHandlerException() {
		super();
	}

	public CLIHandlerException(String message) {
		super(message);
	}

	public CLIHandlerException(String message, Throwable cause) {
		super(message, cause);
	}

	public CLIHandlerException(Throwable cause) {
		super(cause);
	}
}