package nexusvault.cli;

public abstract class NexusvaultCLIBaseException extends RuntimeException {

	private static final long serialVersionUID = -651903647904619396L;

	public NexusvaultCLIBaseException() {
		super();
	}

	public NexusvaultCLIBaseException(String message) {
		super(message);
	}

	public NexusvaultCLIBaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public NexusvaultCLIBaseException(Throwable cause) {
		super(cause);
	}

}
