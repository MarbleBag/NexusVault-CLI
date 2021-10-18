package nexusvault.cli.extensions.archive;

import java.nio.file.Path;

public abstract class ArchiveEvent {
	public final Path path;

	ArchiveEvent(Path path) {
		this.path = path;
	}
}