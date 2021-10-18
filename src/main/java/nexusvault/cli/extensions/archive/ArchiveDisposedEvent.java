package nexusvault.cli.extensions.archive;

import java.nio.file.Path;

public final class ArchiveDisposedEvent extends ArchiveEvent {
	ArchiveDisposedEvent(Path path) {
		super(path);
	}
}