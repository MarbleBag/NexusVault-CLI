package nexusvault.cli.core.exception;

public abstract class AppCheckedException extends Exception {

	private static final long serialVersionUID = 761803836276197113L;

	public AppCheckedException() {
		super();
	}

	public AppCheckedException(String message) {
		super(message);
	}

	public AppCheckedException(String message, Throwable cause) {
		super(message, cause);
	}

	public AppCheckedException(Throwable cause) {
		super(cause);
	}

}
