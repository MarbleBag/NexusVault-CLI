package nexusvault.cli.plugin.show;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nexusvault.archive.IdxFileLink;
import nexusvault.cli.plugin.show.ShowFileProperties.PropertyCollector;
import nexusvault.format.tex.TextureReader;

final class TexFileProperties implements PropertyCollector {

	@Override
	public boolean accepts(IdxFileLink file) {
		return "tex".equalsIgnoreCase(file.getFileEnding());
	}

	@Override
	public Map<String, Map<String, String>> mapProperties(IdxFileLink file) {
		final var properties = new HashMap<String, String>();

		try {
			final var reader = TextureReader.buildDefault();
			final var data = file.getData();
			final var texObj = reader.read(data);

			properties.put("Format", String.valueOf(texObj.getTextureImageFormat()));
			properties.put("TexType", String.valueOf(texObj.getTextureDataType()));
			properties.put("Width", String.valueOf(texObj.getImageWidth()));
			properties.put("Height", String.valueOf(texObj.getImageHeight()));
			properties.put("Mip Maps", String.valueOf(texObj.getMipMapCount()));
		} catch (final IOException e) {

		}

		final var propertyCategory = new HashMap<String, Map<String, String>>();
		propertyCategory.put("Texture", properties);
		return propertyCategory;
	}

}