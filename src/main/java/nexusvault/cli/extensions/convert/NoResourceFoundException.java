package nexusvault.cli.extensions.convert;

public final class NoResourceFoundException extends ConverterException {

	private static final long serialVersionUID = -2340441895693446363L;

	public NoResourceFoundException() {
		super();
	}

	public NoResourceFoundException(String message) {
		super(message);
	}

	public NoResourceFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoResourceFoundException(Throwable cause) {
		super(cause);
	}
}
