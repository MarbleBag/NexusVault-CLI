package nexusvault.cli.plugin.show;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nexusvault.archive.IdxFileLink;
import nexusvault.cli.plugin.show.ShowFileProperties.PropertyCollector;
import nexusvault.format.bin.LanguageReader;

final class BinFileProperties implements PropertyCollector {

	@Override
	public boolean accepts(IdxFileLink file) {
		return "bin".equalsIgnoreCase(file.getFileEnding());
	}

	@Override
	public Map<String, Map<String, String>> mapProperties(IdxFileLink file) {
		final var properties = new HashMap<String, String>();

		try {
			final var reader = new LanguageReader();
			final var data = file.getData();
			final var fileObj = reader.read(data);

			properties.put("Locale long", String.valueOf(fileObj.getLocaleLong()));
			properties.put("Locale short", String.valueOf(fileObj.getLocaleShort()));
			properties.put("Locale tag", String.valueOf(fileObj.getLocaleTag()));
			properties.put("Entries", String.valueOf(fileObj.entryCount()));
		} catch (final IOException e) {

		}

		final var propertyCategory = new HashMap<String, Map<String, String>>();
		propertyCategory.put("Locale", properties);
		return propertyCategory;
	}

}