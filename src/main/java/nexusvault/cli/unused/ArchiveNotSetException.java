package nexusvault.cli.unused;

import nexusvault.cli.core.exception.AppException;

class ArchiveNotSetException extends AppException {

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
