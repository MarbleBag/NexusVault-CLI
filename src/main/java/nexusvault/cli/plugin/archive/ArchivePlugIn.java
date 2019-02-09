package nexusvault.cli.plugin.archive;

import java.io.File;
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

import com.google.common.eventbus.Subscribe;

import kreed.util.property.PropertyChangedEvent;
import kreed.util.property.PropertyListener;
import kreed.util.property.provider.ConstantProvider;
import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.util.ArchivePath;
import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.ConsoleSystem.Level;
import nexusvault.cli.EventSystem;
import nexusvault.cli.exception.ArchiveCanNotBeLoadedException;
import nexusvault.cli.model.ModelPropertyChangedEvent;
import nexusvault.cli.model.ModelSet;
import nexusvault.cli.model.PropertyKey;
import nexusvault.cli.model.PropertyOption;
import nexusvault.cli.plugin.AbstPlugIn;

public final class ArchivePlugIn extends AbstPlugIn {

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

	public static final class ArchiveModelPathPointerChangedEvent extends ArchiveModelChangedEvent<ArchivePath> {
		private ArchiveModelPathPointerChangedEvent(ArchivePath oldValue, ArchivePath newValue) {
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
			INNER_PATH(new PropertyOption<Key>("archive.pointer", false, ArchivePath.class, new ConstantProvider<>(new ArchivePath()))),
			ARCHIVES(new PropertyOption<Key>("archive.vaults", false, List.class, new ConstantProvider<>(Collections.emptyList())));

			private final PropertyOption<Key> opt;

			private Key(PropertyOption<Key> opt) {
				this.opt = opt;
			}

			@Override
			public PropertyOption<Key> getOptions() {
				return opt;
			}
		}

		private final ModelSet<Key> data;

		public ArchiveModel() {
			data = new ModelSet<>(Arrays.asList(Key.values()));

			data.setListener(new PropertyListener<Key>() {
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
							eventSystem.postEvent(new ArchiveModelPathPointerChangedEvent((ArchivePath) property.oldValue, (ArchivePath) property.newValue));
							break;
						default:
							break;

					}
				}
			});
		}

		protected void setArchivePaths(Set<Path> pathes) {
			if ((pathes == null) || pathes.isEmpty()) {
				data.clearProperty(Key.ARCHIVE_PATHS);
			} else {
				data.setProperty(Key.ARCHIVE_PATHS, Collections.unmodifiableSet(new HashSet<>(pathes)));
			}
		}

		public Set<Path> getArchivePaths() {
			return data.getProperty(Key.ARCHIVE_PATHS);
		}

		protected void setArchives(List<SourcedVaultReader> vaults) {
			if ((vaults == null) || vaults.isEmpty()) {
				data.clearProperty(Key.ARCHIVES);
			} else {
				data.setProperty(Key.ARCHIVES, Collections.unmodifiableList(new ArrayList<>(vaults)));
			}
		}

		protected List<SourcedVaultReader> getArchives() {
			return data.getProperty(Key.ARCHIVES);
		}

		public ArchivePath getInnerArchivePath() {
			return data.getProperty(Key.INNER_PATH);
		}

		protected void setInnerArchivePath(ArchivePath path) {
			data.setProperty(Key.INNER_PATH, path);
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
			callback.call();
		}
	}

	private boolean reloadArchive;

	public ArchivePlugIn() {
		final List<Object> listener = new ArrayList<>();
		listener.add(new ArchivePathChangedEventListener(() -> this.reloadArchive = true));
		setEventListener(listener);
	}

	@Override
	public void initialize() {
		final List<Command> cmds = new ArrayList<>();
		cmds.add(new ArchivePathCmd());
		if (!App.getInstance().getAppConfig().getHeadlessMode()) {
			cmds.add(new NavigatorChangeDirectoryCmd());
			cmds.add(new NavigatorListDirectoryContentCmd());
		}
		setCommands(cmds);

		super.initialize();
		App.getInstance().getModelSystem().registerModel(ArchiveModel.class, new ArchiveModel());
	}

	@Override
	public void deinitialize() {
		super.deinitialize();
		unloadArchives();
	}

	public void listDirectoryContent() {
		final List<SourcedVaultReader> archives = loadArchives();
		if (archives.isEmpty()) {
			sendMsg("No vaults are loaded. Use 'help' to learn how to load them");
			return;
		}

		final ArchivePath path = App.getInstance().getModelSystem().getModel(ArchiveModel.class).getInnerArchivePath();

		for (final SourcedVaultReader archive : archives) {
			final IdxDirectory rootFolder = archive.getReader().getRootFolder();
			if (!path.isResolvable(rootFolder)) {
				continue;
			}

			sendMsg("Archive: '" + archive.getSource() + "'");

			final IdxEntry resolvedEntry = path.resolve(rootFolder);
			if (resolvedEntry.isFile()) {
				sendMsg("\tFile: " + resolvedEntry.fullName());
			} else {
				for (final IdxEntry child : resolvedEntry.asDirectory().getChilds()) {
					if (child.isDir()) {
						sendMsg("\tDir: " + child.fullName());
					} else {
						sendMsg("\tFile: " + child.fullName());
					}
				}
			}
		}
	}

	public void changeDirectory(String target) {
		final List<SourcedVaultReader> archives = loadArchives();
		if (archives.isEmpty()) {
			sendMsg("No vaults are loaded. Use 'help' to learn how to load them");
			return;
		}

		ArchivePath path = App.getInstance().getModelSystem().getModel(ArchiveModel.class).getInnerArchivePath();

		target = Paths.get(target).toString();
		if (target.startsWith(File.separator)) {
			path = path.getRoot();
		}

		final String[] steps = target.split(Pattern.quote(File.separator));
		ArchivePath newPath = path;
		for (final String step : steps) {
			newPath = newPath.resolve(step);
			boolean isResolvable = false;
			for (final SourcedVaultReader archive : archives) {
				isResolvable |= newPath.isResolvable(archive.getReader().getRootFolder());
				if (isResolvable) {
					break;
				}
			}
			if (!isResolvable) {
				sendMsg(String.format("Directory '%s' not found.", target));
				return;
			}
		}

		App.getInstance().getModelSystem().getModel(ArchiveModel.class).setInnerArchivePath(newPath);
	}

	public ArchivePath getPathWithinArchives() {
		return App.getInstance().getModelSystem().getModel(ArchiveModel.class).getInnerArchivePath();
	}

	public void unloadArchives() {
		final List<SourcedVaultReader> readers = App.getInstance().getModelSystem().getModel(ArchiveModel.class).getArchives();
		App.getInstance().getModelSystem().getModel(ArchiveModel.class).setArchives(null);
		for (final SourcedVaultReader reader : readers) {
			reader.getReader().dispose();
		}
		reloadArchive = true;
	}

	public List<SourcedVaultReader> loadArchives() {
		final ArchiveModel model = App.getInstance().getModelSystem().getModel(ArchiveModel.class);

		if (reloadArchive) {
			unloadArchives();
			loadArchives(model);
			return model.getArchives();
		} else {
			// maybe check for disposed readers and try to reload them
			return model.getArchives();
		}
	}

	private void loadArchives(final ArchiveModel model) {
		final Set<Path> paths = model.getArchivePaths();
		final List<SourcedVaultReader> readers = new LinkedList<>();

		if (paths.isEmpty()) {
			App.getInstance().getConsole().println(Level.CONSOLE, () -> String.format("No paths to archives set"));
		} else {
			App.getInstance().getConsole().println(Level.CONSOLE, () -> String.format("Load %d archives", paths.size()));
		}

		for (final Path path : paths) {
			try {
				final SourcedVaultReader vault = new SourcedVaultReader(path);
				vault.load();
				readers.add(vault);
				App.getInstance().getConsole().println(Level.CONSOLE, () -> String.format("Archive '%s' loaded", path));
			} catch (final ArchiveCanNotBeLoadedException e) {
				if (e.getCause() != null) {
					App.getInstance().getConsole().println(Level.ERROR, () -> e.getLocalizedMessage() + " : " + e.getCause().getLocalizedMessage());
				} else {
					App.getInstance().getConsole().println(Level.ERROR, () -> e.getLocalizedMessage());
				}
			}
		}

		model.setArchives(readers);
		reloadArchive = false;
	}

	public void setArchivePaths(List<Path> paths) {
		final Set<Path> archivePaths = findValidArchivePaths(paths);
		App.getInstance().getModelSystem().getModel(ArchiveModel.class).setArchivePaths(archivePaths);
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
				if (!(path.endsWith(".index") || path.endsWith(".archive"))) {
					App.getInstance().getConsole().println(Level.CONSOLE, () -> String.format("Path %s does not end with '.index' or '.archive'.", path));
					continue;
				}
				String fileName = path.getFileName().toString();
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
						archiveFound.add(file);
					}
					if (fileName.toLowerCase().endsWith(".index")) {
						indexFound.add(file);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					super.postVisitDirectory(dir, exc);

					for (final Path archive : archiveFound) {
						String fileName = archive.getFileName().toString();
						fileName = fileName.substring(0, fileName.lastIndexOf(".archive")) + ".index";
						final Path idxPath = archive.resolveSibling(fileName);
						if (indexFound.contains(idxPath)) {
							archivePaths.add(archive);
						}
					}

					archiveFound.clear();
					indexFound.clear();
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (final IOException e) {
			App.getInstance().getConsole().println(Level.ERROR, () -> e.getLocalizedMessage());
		}
		return archivePaths;
	}

}
