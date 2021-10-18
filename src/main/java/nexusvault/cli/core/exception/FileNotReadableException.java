package nexusvault.cli.core.exception;

public class FileNotReadableException extends AppException {

	private static final long serialVersionUID = 8456085386529168553L;

	public FileNotReadableException() {
		super();
	}

	public FileNotReadableException(String message) {
		super(message);
	}

	public FileNotReadableException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileNotReadableException(Throwable cause) {
		super(cause);
	}

}
