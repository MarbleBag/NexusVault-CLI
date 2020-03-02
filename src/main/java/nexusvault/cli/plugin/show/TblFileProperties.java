package nexusvault.cli.plugin.show;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nexusvault.archive.IdxFileLink;
import nexusvault.cli.plugin.show.ShowFileProperties.PropertyCollector;
import nexusvault.format.tbl.TableReader;

final class TblFileProperties implements PropertyCollector {

	@Override
	public boolean accepts(IdxFileLink file) {
		return "tbl".equalsIgnoreCase(file.getFileEnding());
	}

	@Override
	public Map<String, Map<String, String>> mapProperties(IdxFileLink file) {
		final var properties = new HashMap<String, String>();

		try {
			final var reader = new TableReader();
			final var data = file.getData();
			final var fileObj = reader.read(data);

			properties.put("Entries", String.valueOf(fileObj.size()));
			properties.put("Table name", String.valueOf(fileObj.getTableName()));
			properties.put("Columns", String.valueOf(fileObj.getFieldCount()));
		} catch (final IOException e) {

		}

		final var propertyCategory = new HashMap<String, Map<String, String>>();
		propertyCategory.put("Table", properties);
		return propertyCategory;
	}

}