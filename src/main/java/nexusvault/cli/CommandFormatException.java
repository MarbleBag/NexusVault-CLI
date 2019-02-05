package nexusvault.cli;

public class CommandFormatException extends NexusvaultCLIBaseException {

	private static final long serialVersionUID = -1581053665535946345L;

	public CommandFormatException() {
		super();
	}

	public CommandFormatException(String message) {
		super(message);
	}

	public CommandFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandFormatException(Throwable cause) {
		super(cause);
	}
}
