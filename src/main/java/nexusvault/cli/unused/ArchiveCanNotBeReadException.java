package nexusvault.cli.unused;

import nexusvault.cli.core.exception.AppException;

class ArchiveCanNotBeReadException extends AppException {

	private static final long serialVersionUID = -5147238759891485498L;

	public ArchiveCanNotBeReadException() {
		super();
	}

	public ArchiveCanNotBeReadException(String message) {
		super(message);
	}

	public ArchiveCanNotBeReadException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArchiveCanNotBeReadException(Throwable cause) {
		super(cause);
	}

}
