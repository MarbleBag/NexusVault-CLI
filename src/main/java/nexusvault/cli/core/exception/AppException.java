package nexusvault.cli.core.exception;

public abstract class AppException extends RuntimeException {

	private static final long serialVersionUID = -651903647904619396L;

	public AppException() {
		super();
	}

	public AppException(String message) {
		super(message);
	}

	public AppException(String message, Throwable cause) {
		super(message, cause);
	}

	public AppException(Throwable cause) {
		super(cause);
	}

}
