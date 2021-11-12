package nexusvault.cli.extensions.show.search;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nexusvault.archive.IdxPath;
import nexusvault.archive.NexusArchive;
import nexusvault.cli.core.App;
import nexusvault.cli.core.Console.Level;
import nexusvault.cli.extensions.archive.ArchiveExtension;
import nexusvault.cli.extensions.archive.NexusArchiveContainer;
import nexusvault.cli.extensions.search.SearchExtension;
import nexusvault.cli.extensions.show.ShowExtension.Showable;

public final class ShowSearchResults implements Showable {

	@Override
	public String getTrigger() {
		return "search";
	}

	@Override
	public void show(App app) {
		app.getConsole().println(Level.CONSOLE, () -> {
			final List<NexusArchiveContainer> containers = app.getExtension(ArchiveExtension.class).getArchives();
			final Map<Path, Set<IdxPath>> mapping = new HashMap<>();
			for (final var container : containers) {
				mapping.put(container.getArchive().getSource().getArchiveFile(), new HashSet<>());
			}

			final List<IdxPath> unresolved = new LinkedList<>();
			final List<IdxPath> searchResults = app.getExtension(SearchExtension.class).getLastSearchResults();
			for (final IdxPath path : searchResults) {
				boolean found = false;
				for (final NexusArchiveContainer container : containers) {
					final NexusArchive archive = container.getArchive();
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

			final StringBuilder resultBuilder = new StringBuilder();
			for (final Entry<Path, Set<IdxPath>> entry : mapping.entrySet()) {
				resultBuilder.append("Archive: ").append(entry.getKey()).append("\n");
				if (entry.getValue().isEmpty()) {
					resultBuilder.append("\tNo search entries\n");
				} else {
					for (final IdxPath idxPath : entry.getValue()) {
						resultBuilder.append("\t").append(idxPath.getFullName()).append("\n");
					}
				}
			}

			if (!unresolved.isEmpty()) {
				resultBuilder.append("Paths not part of an archive:\n");
				for (final IdxPath path : unresolved) {
					resultBuilder.append(path.getFullName()).append("\n");
				}
			}

			return resultBuilder.toString();
		});
	}

}