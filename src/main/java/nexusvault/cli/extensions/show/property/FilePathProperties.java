package nexusvault.cli.extensions.show.property;

import java.util.HashMap;
import java.util.Map;

import nexusvault.archive.IdxFileLink;
import nexusvault.cli.extensions.show.property.ShowFileProperties.PropertyCollector;

final class FilePathProperties implements PropertyCollector {

	@Override
	public boolean accepts(IdxFileLink file) {
		return true;
	}

	@Override
	public Map<String, Map<String, String>> mapProperties(IdxFileLink file) {
		final var properties = new HashMap<String, String>();
		properties.put("File path", file.getFullName());
		properties.put("File size compressed", String.valueOf(file.getCompressedSize()));
		properties.put("File size uncompressed", String.valueOf(file.getUncompressedSize()));
		properties.put("File extension", file.getFileEnding());

		final var propertyCategory = new HashMap<String, Map<String, String>>();
		propertyCategory.put("File", properties);

		return propertyCategory;
	}

}