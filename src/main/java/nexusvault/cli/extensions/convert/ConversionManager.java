package nexusvault.cli.extensions.convert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nexusvault.archive.IdxPath;
import nexusvault.cli.core.App;
import nexusvault.cli.extensions.archive.ArchiveExtension;
import nexusvault.cli.extensions.archive.NexusArchiveContainer;
import nexusvault.cli.extensions.convert.resource.ArchiveResource;
import nexusvault.cli.extensions.convert.resource.FileResource;
import nexusvault.cli.extensions.convert.resource.Resource;

public final class ConversionManager {

	private final Path inputDir;
	private final Path outputDir;
	private final Set<Path> createdFiles = new HashSet<>();
	private final Resource resource;
	private final Map<Path, Resource> cache = new HashMap<>();

	public ConversionManager(Resource resource, Path outputDir) {
		this.resource = resource;
		this.outputDir = outputDir;
		this.inputDir = resource.getDirectory();
	}

	public Resource getResource() {
		return this.resource;
	}

	public Resource requestResource(String name) {
		return null;
	}

	public Resource checkForFile(Path path) {
		if (path.isAbsolute()) {
			if (Files.exists(path)) {
				return new FileResource(path);
			}
			return null;
		}

		final var idxPath = IdxPath.createPathFrom(path.toString());
		final var archiveExtension = App.getInstance().getExtension(ArchiveExtension.class);
		final List<NexusArchiveContainer> archiveContainer = archiveExtension.getArchives();
		for (final NexusArchiveContainer wrapper : archiveContainer) {
			final var resolvedEntry = idxPath.tryToResolve(wrapper.getArchive().getRootDirectory());
			if (resolvedEntry.isPresent() && resolvedEntry.get().isFile()) {
				return new ArchiveResource(resolvedEntry.get().asFile());
			}
		}
		return null;
	}

	// TODO not perfect, lots of missing cases
	public Resource requestResource(Path path) {
		var resource = this.cache.get(path);
		if (resource == null) {
			resource = checkForFile(path);
			if (resource == null && !path.isAbsolute()) {
				final var extendedPath = this.inputDir.resolve(path);
				resource = checkForFile(extendedPath);
			}
			if (resource != null) {
				this.cache.put(path, resource);
			} else {
				throw new NoResourceFoundException(String.format("Resource '%s' not found", path));
			}
		}
		return resource;
	}

	public Path resolveOutputPath(String path) {
		return this.outputDir.resolve(path);
	}

	public Path resolveOutputPath(Path path) {
		if (path.isAbsolute()) {
			return this.outputDir.resolve(path.getFileName());
		}
		return this.outputDir.resolve(path);
	}

	public void addCreatedFile(Path path) {
		if (path == null) {
			throw new IllegalArgumentException();
		}
		this.createdFiles.add(path);
	}

	public Set<Path> getAvailableResources() {
		return Collections.unmodifiableSet(this.cache.keySet());
	}

	public Set<Path> getCreatedFiles() {
		return Collections.unmodifiableSet(this.createdFiles);
	}
}