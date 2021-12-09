package nexusvault.cli.extensions.show.property;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import nexusvault.archive.IdxFileLink;
import nexusvault.cli.extensions.show.property.ShowFileProperties.PropertyCollector;
import nexusvault.format.m3.v100.ModelReader;

final class M3FileProperties implements PropertyCollector {

	@Override
	public boolean accepts(IdxFileLink file) {
		return "m3".equalsIgnoreCase(file.getFileEnding());
	}

	@Override
	public Map<String, Map<String, String>> mapProperties(IdxFileLink file) {
		final var properties = new HashMap<String, String>();

		try {
			final var reader = new ModelReader();
			final var data = file.getData();
			final var fileObj = reader.read(data);

			final var modelGeometry = fileObj.getGeometry();

			properties.put("Meshe count", String.valueOf(modelGeometry.getMeshCount()));

			properties.put("Material count", String.valueOf(fileObj.getMaterials().size()));
			properties.put("Bone count", String.valueOf(fileObj.getBones().size()));

			final var textures = fileObj.getTextures();
			properties.put("Texture count", String.valueOf(textures.size()));

			final var textureNames = textures.stream().map(t -> t.getTexturePath()).collect(Collectors.joining(", "));
			properties.put("Textures", String.valueOf(textureNames));

		} catch (final IOException e) {

		}

		final var propertyCategory = new HashMap<String, Map<String, String>>();
		propertyCategory.put("Geometry", properties);
		return propertyCategory;
	}

}