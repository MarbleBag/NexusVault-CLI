package nexusvault.cli.unused;

import nexusvault.cli.core.exception.NexusvaultCLIBaseException;

class ArchiveNotSetException extends NexusvaultCLIBaseException {

	public ArchiveNotSetException() {
		super();
	}

	public ArchiveNotSetException(String message) {
		super(message);
	}

	public ArchiveNotSetException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArchiveNotSetException(Throwable cause) {
		super(cause);
	}

}
