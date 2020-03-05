package nexusvault.cli.plugin.export;

import nexusvault.cli.core.exception.NexusvaultCLIBaseException;

public class NoExporterFoundException extends NexusvaultCLIBaseException {

	private static final long serialVersionUID = 9075159068858426953L;

	public NoExporterFoundException() {
		super();
	}

	public NoExporterFoundException(String message) {
		super(message);
	}

	public NoExporterFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoExporterFoundException(Throwable cause) {
		super(cause);
	}

}
