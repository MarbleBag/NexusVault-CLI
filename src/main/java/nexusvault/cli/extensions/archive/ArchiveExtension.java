package nexusvault.cli.extensions.archive;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nexusvault.archive.IdxPath;
import nexusvault.cli.core.App;
import nexusvault.cli.core.Console.Level;
import nexusvault.cli.core.extension.AbstractExtension;
import nexusvault.cli.extensions.archive.command.SetArchivePath;
import nexusvault.cli.extensions.archive.command.ChangeDirectory;
import nexusvault.cli.extensions.archive.command.ListDirectoryContent;

public final class ArchiveExtension extends AbstractExtension {

	private final static Logger logger = LogManager.getLogger(ArchiveExtension.class);

	private IdxPath innerPath;
	private Set<Path> archiveFiles;
	private List<NexusArchiveContainer> archiveContainers;

	private boolean reloadArchive;

	public ArchiveExtension() {
	}

	@Override
	public void initializeExtension(InitializationHelper initializationHelper) {
		initializationHelper.addArgumentHandler(new SetArchivePath());

		initializationHelper.addCommandHandler(new SetArchivePath());
		initializationHelper.addCommandHandler(new ListDirectoryContent());
		initializationHelper.addCommandHandler(new ChangeDirectory());
	}

	@Override
	public void deinitializeExtension() {
		unloadArchives();
	}

	public void changeDirectory(String target) {
		final List<NexusArchiveContainer> wrappers = getArchives();
		if (wrappers.isEmpty()) {
			sendMsg("No vaults are loaded. Use 'help' to learn how to load them");
			return;
		}

		IdxPath path = this.innerPath;

		target = Paths.get(target).toString();
		if (target.startsWith(IdxPath.SEPARATOR)) {
			path = path.getRoot();
		}

		final String[] steps = target.split(Pattern.quote(IdxPath.SEPARATOR));
		IdxPath newPath = path;
		for (final String step : steps) {
			newPath = newPath.resolve(step);
			boolean isResolvable = false;
			for (final NexusArchiveContainer wrapper : wrappers) {
				isResolvable |= newPath.isResolvable(wrapper.getArchive().getRootDirectory());
				if (isResolvable) {
					break;
				}
			}
			if (!isResolvable) {
				sendMsg(String.format("Directory '%s' not found.", target));
				return;
			}
		}

		this.innerPath = newPath;
	}

	public IdxPath getPathWithinArchives() {
		return this.innerPath;
	}

	public void unloadArchives() {
		final var archiveContainers = this.archiveContainers;
		this.archiveContainers = Collections.emptyList();
		for (final var container : archiveContainers) {
			container.dispose();
		}
		this.reloadArchive = true;
	}

	public List<NexusArchiveContainer> getArchives() {
		if (this.reloadArchive) {
			unloadArchives();
			loadArchives();
		}

		// maybe check for disposed readers and try to reload them
		return this.archiveContainers;
	}

	private void loadArchives() {
		final Set<Path> paths = this.archiveFiles;

		if (paths.isEmpty()) {
			App.getInstance().getConsole().println(Level.CONSOLE, () -> String.format("No paths to archives set"));
		} else {
			App.getInstance().getConsole().println(Level.CONSOLE, () -> String.format("Load %d archives", paths.size()));
		}

		final var containers = new LinkedList<NexusArchiveContainer>();
		for (final Path path : paths) {
			try {
				final var container = new NexusArchiveContainer(path);
				container.load();
				containers.add(container);
				// App.getInstance().getConsole().println(Level.CONSOLE, () -> String.format("Archive '%s' loaded", path));
			} catch (final ArchiveCanNotBeLoadedException e) {
				throw e;
			}
		}

		this.archiveContainers = Collections.unmodifiableList(containers);
		this.reloadArchive = false;
	}

	public void setArchivePaths(List<Path> paths) {
		this.archiveFiles = Collections.unmodifiableSet(findValidArchivePaths(paths));
	}

	private static Set<Path> findValidArchivePaths(Collection<Path> paths) {
		final Set<Path> archivePaths = new HashSet<>();
		for (final Path path : paths) {
			if (!Files.exists(path)) {
				App.getInstance().getConsole().println(Level.CONSOLE, () -> String.format("Path %s is neither a directory nor a file.", path));
				continue;
			}
			if (Files.isDirectory(path)) {
				archivePaths.addAll(scanForArchives(path));
			} else if (Files.isRegularFile(path)) {
				String fileName = path.getFileName().toString();
				if (!(fileName.endsWith(".index") || fileName.endsWith(".archive"))) {
					App.getInstance().getConsole().println(Level.CONSOLE, () -> String.format("Path %s does not end with '.index' or '.archive'.", path));
					continue;
				}
				if (path.endsWith(".index")) {
					fileName = fileName.substring(0, fileName.lastIndexOf(".index")) + ".archive";
					archivePaths.add(path.resolveSibling(fileName));
				} else {
					archivePaths.add(path);
				}
			}
		}
		return archivePaths;
	}

	private static Collection<? extends Path> scanForArchives(Path path) {
		final Set<Path> archivePaths = new HashSet<>();
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				private final List<Path> archiveFound = new LinkedList<>();
				private final List<Path> indexFound = new LinkedList<>();

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (!attrs.isRegularFile()) {
						return FileVisitResult.CONTINUE;
					}
					final String fileName = file.getFileName().toString();
					if (fileName.toLowerCase().endsWith(".archive")) {
						this.archiveFound.add(file);
					}
					if (fileName.toLowerCase().endsWith(".index")) {
						this.indexFound.add(file);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					super.postVisitDirectory(dir, exc);

					for (final Path archive : this.archiveFound) {
						String fileName = archive.getFileName().toString();
						fileName = fileName.substring(0, fileName.lastIndexOf(".archive")) + ".index";
						final Path idxPath = archive.resolveSibling(fileName);
						if (this.indexFound.contains(idxPath)) {
							archivePaths.add(archive);
						}
					}

					this.archiveFound.clear();
					this.indexFound.clear();
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (final IOException e) {
			logger.error(String.format("Scanned path %s", path), e);
			// App.getInstance().getConsole().println(Level.ERROR, () -> e.getLocalizedMessage());
		}
		return archivePaths;
	}

}
