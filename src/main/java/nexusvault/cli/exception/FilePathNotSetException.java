package nexusvault.cli.exception;

import nexusvault.cli.NexusvaultCLIBaseException;

public class FilePathNotSetException extends NexusvaultCLIBaseException {

	private static final long serialVersionUID = 8519301397403828639L;

	public FilePathNotSetException() {
		super();
	}

	public FilePathNotSetException(String message) {
		super(message);
	}

	public FilePathNotSetException(String message, Throwable cause) {
		super(message, cause);
	}

	public FilePathNotSetException(Throwable cause) {
		super(cause);
	}

}
