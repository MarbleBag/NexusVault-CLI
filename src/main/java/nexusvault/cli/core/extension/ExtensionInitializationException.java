package nexusvault.cli.core.extension;

import nexusvault.cli.core.exception.AppInitializationException;

public final class ExtensionInitializationException extends AppInitializationException {

	private static final long serialVersionUID = -4194367560425268092L;

	public ExtensionInitializationException() {
		super();
	}

	public ExtensionInitializationException(String message) {
		super(message);
	}

	public ExtensionInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExtensionInitializationException(Throwable cause) {
		super(cause);
	}

}
