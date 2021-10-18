package nexusvault.cli.extensions.export;

import nexusvault.cli.core.exception.AppException;

public class ExportException extends AppException {

	private static final long serialVersionUID = 8767328590929264693L;

	public ExportException() {
		super();
	}

	public ExportException(String message) {
		super(message);
	}

	public ExportException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExportException(Throwable cause) {
		super(cause);
	}

}
