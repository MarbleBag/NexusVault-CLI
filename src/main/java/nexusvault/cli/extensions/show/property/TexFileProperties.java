package nexusvault.cli.extensions.show.property;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nexusvault.cli.extensions.show.property.FileProperties.PropertyCollector;
import nexusvault.format.tex.TextureReader;
import nexusvault.vault.IdxEntry.IdxFileLink;

final class TexFileProperties implements PropertyCollector {

	@Override
	public boolean accepts(IdxFileLink file) {
		return "tex".equalsIgnoreCase(file.getFileEnding());
	}

	@Override
	public Map<String, Map<String, String>> mapProperties(IdxFileLink file) {
		final var properties = new HashMap<String, String>();

		try {
			final var texture = TextureReader.read(file.getData());
			properties.put("Format", String.valueOf(texture.getImageFormat()));
			properties.put("TexType", String.valueOf(texture.getTextureType()));
			properties.put("Width", String.valueOf(texture.getWidth()));
			properties.put("Height", String.valueOf(texture.getHeight()));
			properties.put("Mip Maps", String.valueOf(texture.getMipMapCount()));
			properties.put("Version", String.valueOf(texture.getVersion()));
			properties.put("Sides", String.valueOf(texture.getSides()));
			properties.put("Depth", String.valueOf(texture.getDepth()));
		} catch (final IOException e) {

		}

		final var propertyCategory = new HashMap<String, Map<String, String>>();
		propertyCategory.put("Texture", properties);
		return propertyCategory;
	}

}