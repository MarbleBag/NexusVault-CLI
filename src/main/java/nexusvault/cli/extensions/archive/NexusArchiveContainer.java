package nexusvault.cli.extensions.archive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nexusvault.cli.core.App;
import nexusvault.cli.core.Console.Level;
import nexusvault.cli.core.exception.FileNotFoundException;
import nexusvault.cli.core.exception.FileNotReadableException;
import nexusvault.vault.IdxEntry;
import nexusvault.vault.IdxPath;
import nexusvault.vault.NexusArchive;

public final class NexusArchiveContainer {

	private final static Logger logger = LogManager.getLogger(NexusArchiveContainer.class);

	private final Path archivePath;
	private NexusArchive archive;

	public NexusArchiveContainer(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("'path' must not be null");
		}
		this.archivePath = path;
	}

	public Path getSource() {
		return this.archivePath;
	}

	protected void load() throws ArchiveCanNotBeLoadedException {
		try {
			dispose();

			if (!Files.exists(this.archivePath)) {
				throw new FileNotFoundException(String.format("No archive found at %s", this.archivePath));
			}

			if (!Files.isReadable(this.archivePath)) {
				throw new FileNotReadableException(String.format("Archive at %s not readable", this.archivePath));
			}

			App.getInstance().getConsole().println(Level.CONSOLE, () -> String.format("Load archive: %s", this.archivePath)); // TODO
			this.archive = NexusArchive.open(this.archivePath);
			App.getInstance().getEventSystem().postEvent(new ArchiveLoadedEvent(this.archivePath));
		} catch (final IOException e) {
			throw new ArchiveCanNotBeLoadedException(e);
		}
	}

	public Optional<IdxEntry> find(IdxPath path) {
		try {
			return getArchive().find(path);
		} catch (final IOException e1) {
			try {
				dispose();
				throw new ArchiveCanNotBeLoadedException(e1);
			} catch (final IOException e2) {
				throw new ArchiveCanNotBeLoadedException(e2);
			}
		}
	}

	public void dispose() throws IOException {
		if (this.archive != null) {
			try {
				this.archive.close();
			} finally {
				this.archive = null;
				App.getInstance().getEventSystem().postEvent(new ArchiveDisposedEvent(this.archivePath));
			}
		}
	}

	public void reload() {
		load();
	}

	public NexusArchive getArchive() {
		if (this.archive == null || this.archive.isDisposed()) {
			load();
		}
		return this.archive;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("NexusArchiveWrapper [path=");
		builder.append(this.archivePath);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.archivePath == null ? 0 : this.archivePath.hashCode());
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
		final NexusArchiveContainer other = (NexusArchiveContainer) obj;
		if (this.archivePath == null) {
			if (other.archivePath != null) {
				return false;
			}
		} else if (!this.archivePath.equals(other.archivePath)) {
			return false;
		}
		return true;
	}

}
