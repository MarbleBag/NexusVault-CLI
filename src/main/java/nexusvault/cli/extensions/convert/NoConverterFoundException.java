package nexusvault.cli.extensions.convert;

public final class NoConverterFoundException extends ConverterException {

	private static final long serialVersionUID = -9082872815199005002L;

	public NoConverterFoundException() {
		super();
	}

	public NoConverterFoundException(String message) {
		super(message);
	}

	public NoConverterFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoConverterFoundException(Throwable cause) {
		super(cause);
	}
}
