package nexusvault.cli.core.exception;

public class FileNotFoundException extends AppException {

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
