package nexusvault.cli.extensions.show.property;

import java.util.ArrayList;
import java.util.List;

import nexusvault.cli.core.App;
import nexusvault.cli.core.Console.Level;
import nexusvault.cli.extensions.archive.ArchiveExtension;
import nexusvault.cli.extensions.search.SearchExtension;
import nexusvault.cli.extensions.show.ShowExtension.Showable;
import nexusvault.vault.IdxEntry.IdxFileLink;

public final class ShowFileProperties implements Showable {

	@Override
	public String getTrigger() {
		return "filemeta";
	}

	@Override
	public void show(App app) {
		app.getConsole().println(Level.CONSOLE, () -> {
			final StringBuilder b = new StringBuilder();

			final var files = getSearchResults();
			for (final var file : files) {
				final var propertyMap = FileProperties.getProperties(file);
				if (propertyMap.isEmpty()) {
					continue;
				}

				b.append("Inspect file: ").append(file.getFullName()).append("\n");
				for (final var propertyCategory : propertyMap.entrySet()) {
					b.append(propertyCategory.getKey()).append(":").append('\n');
					for (final var property : propertyCategory.getValue().entrySet()) {
						b.append("    ").append(property.getKey()).append(" = ").append(property.getValue()).append("\n");
					}
				}
				b.append('\n');
			}

			return b.toString();
		});
	}

	private List<IdxFileLink> getSearchResults() {
		final var result = new ArrayList<IdxFileLink>();
		final var archives = App.getInstance().getExtension(ArchiveExtension.class).getArchives();
		final var searchResults = App.getInstance().getExtension(SearchExtension.class).getLastSearchResults();
		for (final var searchResult : searchResults) {
			for (final var archive : archives) {
				final var file = archive.getArchive().find(searchResult);
				if (file.isPresent() && file.get().isFile()) {
					result.add(file.get().asFile());
				}
			}
		}
		return result;
	}

}