package nexusvault.cli.model;

import nexusvault.cli.core.exception.AppException;

public final class PropertyAlreadyDefinedException extends AppException {

	private static final long serialVersionUID = -7932491086087822201L;

	public PropertyAlreadyDefinedException() {
		super();
	}

	public PropertyAlreadyDefinedException(String message) {
		super(message);
	}

	public PropertyAlreadyDefinedException(String message, Throwable cause) {
		super(message, cause);
	}

	public PropertyAlreadyDefinedException(Throwable cause) {
		super(cause);
	}
}
