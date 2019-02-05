package nexusvault.cli.exception;

import nexusvault.cli.NexusvaultCLIBaseException;

public class FileNotFoundException extends NexusvaultCLIBaseException {

	private static final long serialVersionUID = 6054345784552077959L;

	public FileNotFoundException() {
		super();
	}

	public FileNotFoundException(String message) {
		super(message);
	}

	public FileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileNotFoundException(Throwable cause) {
		super(cause);
	}

}
