package nexusvault.cli.extensions.export;

import java.nio.file.Path;

import nexusvault.vault.IdxEntry.IdxFileLink;

final class ExportError {

	private final String path;
	private final Throwable error;

	public ExportError(IdxFileLink path, Throwable error) {
		this.path = path.getFullName();
		this.error = error;
	}

	public ExportError(Path path, Throwable error) {
		this.path = path.toString();
		this.error = error;
	}

	public ExportError(String path, Throwable error) {
		this.path = path;
		this.error = error;
	}

	public String getFile() {
		return this.path;
	}

	public Throwable getError() {
		return this.error;
	}
}
