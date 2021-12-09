package nexusvault.cli.extensions.show.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nexusvault.archive.IdxException;
import nexusvault.archive.IdxFileLink;
import nexusvault.cli.core.App;
import nexusvault.cli.core.Console.Level;
import nexusvault.cli.extensions.archive.ArchiveExtension;
import nexusvault.cli.extensions.search.SearchExtension;
import nexusvault.cli.extensions.show.ShowExtension.Showable;

public final class ShowFileProperties implements Showable {

	private static final Logger logger = LogManager.getLogger(ShowFileProperties.class);

	interface PropertyCollector {
		boolean accepts(IdxFileLink file);

		Map<String, Map<String, String>> mapProperties(IdxFileLink file);
	}

	private final List<ShowFileProperties.PropertyCollector> propertyCollectors = new ArrayList<>();

	public ShowFileProperties() {
		this.propertyCollectors.add(new FilePathProperties());
		this.propertyCollectors.add(new TexFileProperties());
		this.propertyCollectors.add(new BinFileProperties());
		this.propertyCollectors.add(new TblFileProperties());
		this.propertyCollectors.add(new M3FileProperties());
	}

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
				final var propertyMap = getProperties(file);
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

	private List<Map<String, Map<String, String>>> collectProperties(List<IdxFileLink> files) {
		final var results = new ArrayList<Map<String, Map<String, String>>>(files.size());

		for (final var file : files) {
			final var properties = getProperties(file);

			if (properties.size() != 0) {
				results.add(properties);
			}
		}

		return results;
	}

	private Map<String, Map<String, String>> getProperties(final IdxFileLink file) {
		final var properties = new TreeMap<String, Map<String, String>>();

		for (final var propertyCollector : this.propertyCollectors) {
			try {
				if (propertyCollector.accepts(file)) {
					final var newProperties = propertyCollector.mapProperties(file);
					if (newProperties == null) {
						continue;
					}

					deepMerge(newProperties, properties);
				}
			} catch (final Exception e) {
				App.getInstance().getConsole().print(Level.CONSOLE, e.getMessage());
				logger.error(e);
			}
		}
		return properties;
	}

	private List<IdxFileLink> getSearchResults() {
		final var result = new ArrayList<IdxFileLink>();
		final var archives = App.getInstance().getExtension(ArchiveExtension.class).getArchives();
		final var searchResults = App.getInstance().getExtension(SearchExtension.class).getLastSearchResults();
		for (final var searchResult : searchResults) {
			for (final var archive : archives) {
				try {
					final var file = searchResult.resolve(archive.getArchive().getRootDirectory());
					if (file.isFile()) {
						result.add(file.asFile());
					}
				} catch (final IdxException a) {
					// TODO
				}
			}
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void deepMerge(Map src, Map dst) {
		for (final var srcEntry : (Set<Entry>) src.entrySet()) {
			final var srcKey = srcEntry.getKey();
			final var srcValue = srcEntry.getValue();

			if (dst.containsKey(srcKey)) {
				final var dstValue = dst.get(srcKey);

				if (Objects.equals(dstValue, srcValue)) {
					return;
				}

				if (dstValue instanceof Collection) {
					if (!(srcValue instanceof Collection)) {
						throw new IllegalStateException(String.format("a non-collection collided with a collection: %s%n\t%s", srcValue, dstValue));
					}

					((Collection) dstValue).addAll((Collection) srcValue);
					continue;
				}

				if (dstValue instanceof Map) {
					if (!(srcValue instanceof Map)) {
						throw new IllegalStateException(String.format("a non-map collided with a map: %s%n\t%s", srcValue, dstValue));
					}

					deepMerge((Map) srcValue, (Map) dstValue);
					continue;
				}

				dst.put(srcKey, srcValue);
			} else {
				dst.put(srcKey, srcValue);
			}
		}
	}

}