package nexusvault.cli.core.exception;

public class AppInitializationException extends AppException {

	private static final long serialVersionUID = -4194367560425268092L;

	public AppInitializationException() {
		super();
	}

	public AppInitializationException(String message) {
		super(message);
	}

	public AppInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public AppInitializationException(Throwable cause) {
		super(cause);
	}

}
