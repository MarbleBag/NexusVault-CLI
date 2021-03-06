package nexusvault.cli.plugin.archive;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import kreed.util.property.PropertyChangedEvent;
import kreed.util.property.PropertyListener;
import kreed.util.property.provider.ConstantProvider;
import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.IdxPath;
import nexusvault.cli.App;
import nexusvault.cli.ConsoleSystem.Level;
import nexusvault.cli.EventSystem;
import nexusvault.cli.model.ModelPropertyChangedEvent;
import nexusvault.cli.model.ModelSet;
import nexusvault.cli.model.PropertyKey;
import nexusvault.cli.model.PropertyOption;
import nexusvault.cli.plugin.AbstractPlugIn;

public final class ArchivePlugIn extends AbstractPlugIn {

	private final static Logger logger = LogManager.getLogger(ArchivePlugIn.class);

	public static abstract class ArchiveEvent {

	}

	public static final class ArchiveLoadedEvent extends ArchiveEvent {

		public ArchiveLoadedEvent(Path archivePath) {
			// TODO Auto-generated constructor stub
		}

	}

	public static final class ArchiveDisposedEvent extends ArchiveEvent {

		public ArchiveDisposedEvent(Path archivePath) {
			// TODO Auto-generated constructor stub
		}

	}

	public static abstract class ArchiveModelChangedEvent<T> extends ModelPropertyChangedEvent<T> {
		private ArchiveModelChangedEvent(String eventName, T oldValue, T newValue) {
			super(eventName, oldValue, newValue);
		}
	}

	public static final class ArchiveModelArchivePathsChangedEvent extends ArchiveModelChangedEvent<Set<Path>> {
		private ArchiveModelArchivePathsChangedEvent(Set<Path> oldPath, Set<Path> newPath) {
			super("Archive Paths", oldPath, newPath);
		}
	}

	public static final class ArchiveModelPathPointerChangedEvent extends ArchiveModelChangedEvent<IdxPath> {
		private ArchiveModelPathPointerChangedEvent(IdxPath oldValue, IdxPath newValue) {
			super("Inner Archive Path", oldValue, newValue);
		}
	}

	public static final class ArchiveModelArchiveChangedEvent extends ArchiveModelChangedEvent<Void> {
		private ArchiveModelArchiveChangedEvent() {
			super("Archive", null, null);
		}
	}

	private static final class ArchiveModel {
		private static enum Key implements PropertyKey<Key> {
			ARCHIVE_PATHS(new PropertyOption<Key>("archive.paths", true, Set.class, new ConstantProvider<>(Collections.emptySet()))),
			INNER_PATH(new PropertyOption<Key>("archive.pointer", false, IdxPath.class, new ConstantProvider<>(IdxPath.createPath()))),
			ARCHIVES(new PropertyOption<Key>("archive.vaults", false, List.class, new ConstantProvider<>(Collections.emptyList())));

			private final PropertyOption<Key> opt;

			private Key(PropertyOption<Key> opt) {
				this.opt = opt;
			}

			@Override
			public PropertyOption<Key> getOptions() {
				return this.opt;
			}
		}

		private final ModelSet<Key> data;

		public ArchiveModel() {
			this.data = new ModelSet<>(Arrays.asList(Key.values()));

			this.data.setListener(new PropertyListener<Key>() {
				@SuppressWarnings("unchecked")
				@Override
				public void onPropertyChange(PropertyChangedEvent<Key> property) {
					final EventSystem eventSystem = App.getInstance().getEventSystem();
					if (eventSystem == null) {
						return;
					}
					// TODO
					switch (property.key) {
						case ARCHIVES:
							eventSystem.postEvent(new ArchiveModelArchiveChangedEvent());
							break;
						case ARCHIVE_PATHS:
							eventSystem.postEvent(new ArchiveModelArchivePathsChangedEvent((Set<Path>) property.oldValue, (Set<Path>) property.newValue));
							break;
						case INNER_PATH:
							eventSystem.postEvent(new ArchiveModelPathPointerChangedEvent((IdxPath) property.oldValue, (IdxPath) property.newValue));
							break;
						default:
							break;

					}
				}
			});
		}

		protected void setArchivePaths(Set<Path> pathes) {
			if (pathes == null || pathes.isEmpty()) {
				this.data.clearProperty(Key.ARCHIVE_PATHS);
			} else {
				this.data.setProperty(Key.ARCHIVE_PATHS, Collections.unmodifiableSet(new HashSet<>(pathes)));
			}
		}

		public Set<Path> getArchivePaths() {
			return this.data.getProperty(Key.ARCHIVE_PATHS);
		}

		protected void setArchives(List<NexusArchiveWrapper> vaults) {
			if (vaults == null || vaults.isEmpty()) {
				this.data.clearProperty(Key.ARCHIVES);
			} else {
				this.data.setProperty(Key.ARCHIVES, Collections.unmodifiableList(new ArrayList<>(vaults)));
			}
		}

		protected List<NexusArchiveWrapper> getArchives() {
			return this.data.getProperty(Key.ARCHIVES);
		}

		public IdxPath getInnerArchivePath() {
			return this.data.getProperty(Key.INNER_PATH);
		}

		protected void setInnerArchivePath(IdxPath path) {
			this.data.setProperty(Key.INNER_PATH, path);
		}
	}

	private static interface Callback {
		void call();
	}

	private class ArchivePathChangedEventListener {
		private final Callback callback;

		private ArchivePathChangedEventListener(Callback callback) {
			this.callback = callback;
		}

		@Subscribe
		public void onPathChanged(ArchiveModelArchivePathsChangedEvent event) {
			this.callback.call();
		}
	}

	private ArchiveModel model;
	private boolean reloadArchive;

	public ArchivePlugIn() {
		final List<Object> listener = new ArrayList<>();
		listener.add(new ArchivePathChangedEventListener(() -> this.reloadArchive = true));
		setEventListener(listener);

	}

	@Override
	public void initialize() { // TODO
		setCommands( //
				new ArchivePathHandler(), //
				new NavigatorListDirectoryContentCmd(), //
				new NavigatorChangeDirectoryCmd() //
		);

		setArguments( //
				new ArchivePathHandler()//
		);

		super.initialize();
		this.model = new ArchiveModel();
	}

	@Override
	public void deinitialize() {
		super.deinitialize();
		unloadArchives();
	}

	public void listDirectoryContent() {
		final List<NexusArchiveWrapper> wrappers = getArchives();
		if (wrappers.isEmpty()) {
			sendMsg("No vaults are loaded. Use 'help' to learn how to load them");
			return;
		}

		final IdxPath path = this.model.getInnerArchivePath();

		for (final NexusArchiveWrapper wrapper : wrappers) {
			final IdxDirectory rootFolder = wrapper.getArchive().getRootDirectory();
			if (!path.isResolvable(rootFolder)) {
				continue;
			}

			sendMsg("Archive: '" + wrapper.getSource() + "'");

			final IdxEntry resolvedEntry = path.resolve(rootFolder);
			if (resolvedEntry.isFile()) {
				sendMsg("\tFile: " + resolvedEntry.getFullName());
			} else {
				for (final IdxEntry child : resolvedEntry.asDirectory().getChilds()) {
					if (child.isDir()) {
						sendMsg("\tDir: " + child.getFullName());
					} else {
						sendMsg("\tFile: " + child.getFullName());
					}
				}
			}
		}
	}

	public void changeDirectory(String target) {
		final List<NexusArchiveWrapper> wrappers = getArchives();
		if (wrappers.isEmpty()) {
			sendMsg("No vaults are loaded. Use 'help' to learn how to load them");
			return;
		}

		IdxPath path = this.model.getInnerArchivePath();

		target = Paths.get(target).toString();
		if (target.startsWith(IdxPath.SEPARATOR)) {
			path = path.getRoot();
		}

		final String[] steps = target.split(Pattern.quote(IdxPath.SEPARATOR));
		IdxPath newPath = path;
		for (final String step : steps) {
			newPath = newPath.resolve(step);
			boolean isResolvable = false;
			for (final NexusArchiveWrapper wrapper : wrappers) {
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

		this.model.setInnerArchivePath(newPath);
	}

	public IdxPath getPathWithinArchives() {
		return this.model.getInnerArchivePath();
	}

	public void unloadArchives() {
		final List<NexusArchiveWrapper> wrappers = this.model.getArchives();
		this.model.setArchives(null);
		for (final NexusArchiveWrapper wrapper : wrappers) {
			wrapper.dispose();
		}
		this.model.setArchives(Collections.emptyList());
		this.reloadArchive = true;
	}

	public List<NexusArchiveWrapper> getArchives() {
		if (this.reloadArchive) {
			unloadArchives();
			loadArchives();
			return this.model.getArchives();
		} else {
			// maybe check for disposed readers and try to reload them
			return this.model.getArchives();
		}
	}

	private void loadArchives() {
		final Set<Path> paths = this.model.getArchivePaths();
		final List<NexusArchiveWrapper> wrappers = new LinkedList<>();

		if (paths.isEmpty()) {
			App.getInstance().getConsole().println(Level.CONSOLE, () -> String.format("No paths to archives set"));
		} else {
			App.getInstance().getConsole().println(Level.CONSOLE, () -> String.format("Load %d archives", paths.size()));
		}

		for (final Path path : paths) {
			try {
				final NexusArchiveWrapper wrapper = new NexusArchiveWrapper(path);
				wrapper.load();
				wrappers.add(wrapper);
				// App.getInstance().getConsole().println(Level.CONSOLE, () -> String.format("Archive '%s' loaded", path));
			} catch (final ArchiveCanNotBeLoadedException e) {
				throw e;
			}
		}

		this.model.setArchives(wrappers);
		this.reloadArchive = false;
	}

	public void setArchivePaths(List<Path> paths) {
		final Set<Path> archivePaths = findValidArchivePaths(paths);
		this.model.setArchivePaths(archivePaths);
	}

	private Set<Path> findValidArchivePaths(Collection<Path> paths) {
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

	private Collection<? extends Path> scanForArchives(Path path) {
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
