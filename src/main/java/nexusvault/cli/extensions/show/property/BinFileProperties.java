package nexusvault.cli.extensions.show.property;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nexusvault.cli.extensions.show.property.FileProperties.PropertyCollector;
import nexusvault.format.bin.LanguageReader;
import nexusvault.vault.IdxEntry.IdxFileLink;

final class BinFileProperties implements PropertyCollector {

	@Override
	public boolean accepts(IdxFileLink file) {
		return "bin".equalsIgnoreCase(file.getFileEnding());
	}

	@Override
	public Map<String, Map<String, String>> mapProperties(IdxFileLink file) {
		final var properties = new HashMap<String, String>();

		try {
			final var data = file.getData();
			final var fileObj = LanguageReader.read(data);

			properties.put("Locale long", String.valueOf(fileObj.locale.longName));
			properties.put("Locale short", String.valueOf(fileObj.locale.shortName));
			properties.put("Locale tag", String.valueOf(fileObj.locale.tagName));
			properties.put("Entries", String.valueOf(fileObj.entries.size()));
		} catch (final IOException e) {

		}

		final var propertyCategory = new HashMap<String, Map<String, String>>();
		propertyCategory.put("Locale", properties);
		return propertyCategory;
	}

}