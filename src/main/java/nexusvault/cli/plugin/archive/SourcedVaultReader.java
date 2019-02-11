package nexusvault.cli.plugin.archive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import nexusvault.archive.VaultReader;
import nexusvault.cli.exception.FileNotFoundException;
import nexusvault.cli.exception.FileNotReadableException;

public final class SourcedVaultReader {
	private final Path path;
	private VaultReader reader;

	public SourcedVaultReader(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("'past' must not be null");
		}
		this.path = path;
	}

	public Path getSource() {
		return path;
	}

	protected void load() throws ArchiveCanNotBeLoadedException {
		try {
			if (!Files.exists(path)) {
				throw new FileNotFoundException(String.format("No archive found at %s", path));
			}

			if (!Files.isReadable(path)) {
				throw new FileNotReadableException(String.format("Archive at %s not readable", path));
			}

			if (reader == null) {
				reader = VaultReader.createVaultReader();
			}
			reader.readArchive(path);
		} catch (final IOException e) {
			throw new ArchiveCanNotBeLoadedException(e);
		}
	}

	public void reload() {
		load();
	}

	public VaultReader getReader() {
		if (reader.isDisposed()) {
			load();
		}
		return reader;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((path == null) ? 0 : path.hashCode());
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
		final SourcedVaultReader other = (SourcedVaultReader) obj;
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SourcedVaultReader [path=");
		builder.append(path);
		builder.append("]");
		return builder.toString();
	}

}