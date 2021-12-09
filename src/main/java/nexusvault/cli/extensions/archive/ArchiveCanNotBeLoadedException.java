package nexusvault.cli.extensions.archive;

import nexusvault.cli.core.exception.AppException;

public class ArchiveCanNotBeLoadedException extends AppException {

	private static final long serialVersionUID = -5147238759891485498L;

	public ArchiveCanNotBeLoadedException() {
		super();
	}

	public ArchiveCanNotBeLoadedException(String message) {
		super(message);
	}

	public ArchiveCanNotBeLoadedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArchiveCanNotBeLoadedException(Throwable cause) {
		super(cause);
	}

}
