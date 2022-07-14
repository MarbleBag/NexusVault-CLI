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

import nexusvault.cli.core.App;
import nexusvault.cli.core.Console.Level;
import nexusvault.vault.IdxEntry.IdxFileLink;

public final class FileProperties {

	interface PropertyCollector {
		boolean accepts(IdxFileLink file);

		Map<String, Map<String, String>> mapProperties(IdxFileLink file);
	}

	private static final Logger LOGGER = LogManager.getLogger(FileProperties.class);

	private static final List<PropertyCollector> PROPERTY_COLLECTORS = new ArrayList<>();
	static {
		PROPERTY_COLLECTORS.add(new FilePathProperties());
		PROPERTY_COLLECTORS.add(new TexFileProperties());
		PROPERTY_COLLECTORS.add(new BinFileProperties());
		PROPERTY_COLLECTORS.add(new TblFileProperties());
		PROPERTY_COLLECTORS.add(new M3FileProperties());
	}

	private FileProperties() {

	}

	public static List<Map<String, Map<String, String>>> collectProperties(List<IdxFileLink> files) {
		final var results = new ArrayList<Map<String, Map<String, String>>>(files.size());
		for (final var file : files) {
			final var properties = getProperties(file);

			if (properties.size() != 0) {
				results.add(properties);
			}
		}
		return results;
	}

	public static Map<String, Map<String, String>> getProperties(final IdxFileLink file) {
		final var properties = new TreeMap<String, Map<String, String>>();

		for (final var propertyCollector : PROPERTY_COLLECTORS) {
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
				LOGGER.error(e);
			}
		}
		return properties;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void deepMerge(Map src, Map dst) {
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
