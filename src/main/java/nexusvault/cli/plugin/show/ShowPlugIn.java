package nexusvault.cli.plugin.show;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import nexusvault.archive.IdxPath;
import nexusvault.archive.NexusArchive;
import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.ConsoleSystem.Level;
import nexusvault.cli.plugin.AbstPlugIn;
import nexusvault.cli.plugin.archive.ArchivePlugIn;
import nexusvault.cli.plugin.archive.NexusArchiveWrapper;
import nexusvault.cli.plugin.search.SearchPlugIn;

public final class ShowPlugIn extends AbstPlugIn {

	private static interface ShowAble {
		String getTrigger();

		void show();
	}

	private static final class ShowSearchResults implements ShowAble {
		@Override
		public String getTrigger() {
			return "search";
		}

		@Override
		public void show() {
			App.getInstance().getConsole().println(Level.CONSOLE, () -> {
				final List<NexusArchiveWrapper> wrappers = App.getInstance().getPlugIn(ArchivePlugIn.class).getArchives();
				final Map<Path, Set<IdxPath>> mapping = new HashMap<>();
				wrappers.stream().forEach(wrapper -> mapping.put(wrapper.getArchive().getSource().getArchiveFile(), new HashSet<>()));

				final List<IdxPath> unresolved = new LinkedList<>();
				final List<IdxPath> searchResults = App.getInstance().getPlugIn(SearchPlugIn.class).getLastSearchResults();
				for (final IdxPath path : searchResults) {
					boolean found = false;
					for (final NexusArchiveWrapper wrapper : wrappers) {
						final NexusArchive archive = wrapper.getArchive();
						if (path.isResolvable(archive.getRootDirectory())) {
							mapping.get(archive.getSource().getArchiveFile()).add(path);
							found = true;
							break;
						}
					}
					if (!found) {
						unresolved.add(path);
					}
				}

				final StringBuilder b = new StringBuilder();
				for (final Entry<Path, Set<IdxPath>> entry : mapping.entrySet()) {
					b.append("Archive: ").append(entry.getKey()).append("\n");
					if (entry.getValue().isEmpty()) {
						b.append("\tNo search entries\n");
					} else {
						for (final IdxPath idxPath : entry.getValue()) {
							b.append("\t").append(idxPath.getFullName()).append("\n");
						}
					}
				}
				if (!unresolved.isEmpty()) {
					b.append("Paths not part of an archive:\n");
					for (final IdxPath path : unresolved) {
						b.append(path.getFullName()).append("\n");
					}
				}
				return b.toString();
			});
		}

	}

	private List<ShowAble> showables;

	public ShowPlugIn() {
		final List<Command> cmds = new ArrayList<>();

		if (!App.getInstance().getAppConfig().getHeadlessMode()) {
			cmds.add(new ShowCmd());
		}

		setCommands(cmds);
	}

	@Override
	public void initialize() {
		super.initialize();
		showables = new ArrayList<>();

		showables.add(new ShowSearchResults());
	}

	@Override
	public void deinitialize() {
		super.deinitialize();
		showables = null;
	}

	public void show(String arg0) {
		for (final ShowAble showable : showables) {
			if (showable.getTrigger().equals(arg0)) {
				showable.show();
				return;
			}
		}
	}

	public void showHelp() {
		sendMsg(() -> {
			final StringBuilder builder = new StringBuilder("Usable arguments: ");
			final List<String> triggers = showables.stream().map(ShowAble::getTrigger).collect(Collectors.toList());
			builder.append(String.join(", ", triggers));
			return builder.toString();
		});
	}

}
