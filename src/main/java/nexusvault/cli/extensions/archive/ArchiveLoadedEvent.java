package nexusvault.cli.extensions.archive;

import java.nio.file.Path;

public final class ArchiveLoadedEvent extends ArchiveEvent {
	ArchiveLoadedEvent(Path path) {
		super(path);
	}
}