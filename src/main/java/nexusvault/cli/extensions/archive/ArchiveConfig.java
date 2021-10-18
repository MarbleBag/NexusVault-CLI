package nexusvault.cli.extensions.archive;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import nexusvault.archive.IdxPath;
import nexusvault.cli.core.App;
import nexusvault.cli.core.Config;

final class ArchiveConfig {

	private final Config config;

	private IdxPath currentArchiveDirectory = IdxPath.createPath();
	private final Set<Path> archivePaths = Collections.emptySet();
	private final List<NexusArchiveContainer> archives = Collections.emptyList();

	protected ArchiveConfig() {
		this.config = App.getInstance().geConfig().loadConfig(ArchiveConfig.class.getCanonicalName());

		if (this.config.get("archive.pointer") != null) {
			this.currentArchiveDirectory = IdxPath.createPathFrom((String) this.config.get("archive.pointer"));
		}
	}

	public Set<Path> getArchivePaths() {
		return this.archivePaths;
	}

	public IdxPath getInnerArchivePath() {
		return this.currentArchiveDirectory;
	}

	void setInnerArchivePath(IdxPath path) {
		final var oldPath = this.currentArchiveDirectory;
		final var newPath = path;

		if (!oldPath.equals(newPath)) {
			this.currentArchiveDirectory = path;
			this.config.set("archive.pointer", this.currentArchiveDirectory.getFullName());
			this.config.flush();

			final var eventSystem = App.getInstance().getEventSystem();
			eventSystem.postEvent(new ArchiveModelPathPointerChangedEvent(oldPath, newPath));
		}
	}

}
