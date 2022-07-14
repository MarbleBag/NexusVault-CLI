package nexusvault.cli.extensions.show.property;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import nexusvault.cli.extensions.show.property.FileProperties.PropertyCollector;
import nexusvault.format.tbl.TableReader;
import nexusvault.vault.IdxEntry.IdxFileLink;

final class TblFileProperties implements PropertyCollector {

	@Override
	public boolean accepts(IdxFileLink file) {
		return "tbl".equalsIgnoreCase(file.getFileEnding());
	}

	@Override
	public Map<String, Map<String, String>> mapProperties(IdxFileLink file) {
		final var propertyCategory = new HashMap<String, Map<String, String>>();
		final var properties = propertyCategory.computeIfAbsent("Table", e -> new HashMap<>());

		try {
			final var tbl = TableReader.read(file.getData());
			properties.put("Entries", String.valueOf(tbl.entries.length));
			properties.put("Table name", String.valueOf(tbl.name));
			properties.put("Columns", Arrays.stream(tbl.columns).map(e -> e.name).collect(Collectors.joining(", ")));
		} catch (final IOException e) {

		}

		return propertyCategory;
	}

}