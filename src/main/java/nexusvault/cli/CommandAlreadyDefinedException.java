package nexusvault.cli;

public final class CommandAlreadyDefinedException extends RuntimeException {

	private static final long serialVersionUID = 4351511956427260760L;

	public CommandAlreadyDefinedException() {
		super();
	}

	public CommandAlreadyDefinedException(String message) {
		super(message);
	}

	public CommandAlreadyDefinedException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandAlreadyDefinedException(Throwable cause) {
		super(cause);
	}
}