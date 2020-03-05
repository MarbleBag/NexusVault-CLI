package nexusvault.cli.plugin.archive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import nexusvault.archive.NexusArchive;
import nexusvault.cli.App;
import nexusvault.cli.ConsoleSystem.Level;
import nexusvault.cli.core.exception.FileNotFoundException;
import nexusvault.cli.core.exception.FileNotReadableException;
import nexusvault.cli.plugin.archive.ArchivePlugIn.ArchiveDisposedEvent;
import nexusvault.cli.plugin.archive.ArchivePlugIn.ArchiveLoadedEvent;

public final class NexusArchiveWrapper {

	private final Path archivePath;
	private NexusArchive archive;

	public NexusArchiveWrapper(Path archivePath) {
		if (archivePath == null) {
			throw new IllegalArgumentException("'archivePath' must not be null");
		}
		this.archivePath = archivePath;
	}

	public Path getSource() {
		return archivePath;
	}

	protected void load() throws ArchiveCanNotBeLoadedException {
		dispose();

		try {
			if (!Files.exists(archivePath)) {
				throw new FileNotFoundException(String.format("No archive found at %s", archivePath));
			}

			if (!Files.isReadable(archivePath)) {
				throw new FileNotReadableException(String.format("Archive at %s not readable", archivePath));
			}

			archive = NexusArchive.loadArchive(archivePath);
			App.getInstance().getConsole().println(Level.CONSOLE, () -> String.format("Load archive: %s", archivePath)); // TODO
			App.getInstance().getEventSystem().postEvent(new ArchiveLoadedEvent(archivePath));
		} catch (final IOException e) {
			throw new ArchiveCanNotBeLoadedException(e);
		}
	}

	public void dispose() {
		if (archive != null) {
			archive.dispose();
			archive = null;
			App.getInstance().getEventSystem().postEvent(new ArchiveDisposedEvent(archivePath));
		}
	}

	public void reload() {
		load();
	}

	public NexusArchive getArchive() {
		if ((archive == null) || archive.isDisposed()) {
			load();
		}
		return archive;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("NexusArchiveWrapper [path=");
		builder.append(archivePath);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((archivePath == null) ? 0 : archivePath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final NexusArchiveWrapper other = (NexusArchiveWrapper) obj;
		if (archivePath == null) {
			if (other.archivePath != null) {
				return false;
			}
		} else if (!archivePath.equals(other.archivePath)) {
			return false;
		}
		return true;
	}

}
