package nexusvault.cli.extensions.convert;

import nexusvault.cli.core.exception.AppException;

public class ConverterException extends AppException {

	private static final long serialVersionUID = 870305364045478445L;

	public ConverterException() {
		super();
	}

	public ConverterException(String message) {
		super(message);
	}

	public ConverterException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConverterException(Throwable cause) {
		super(cause);
	}
}
