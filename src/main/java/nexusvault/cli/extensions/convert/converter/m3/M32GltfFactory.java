package nexusvault.cli.extensions.convert.converter.m3;

import nexusvault.cli.core.cmd.ArgumentHelper;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsArgument;
import nexusvault.cli.extensions.convert.IsFactory;

@IsFactory(id = "m32gltf", fileExtensions = "m3", priority = 3)
public final class M32GltfFactory implements ConverterFactory {

	private boolean includeTextures = true;

	@IsArgument(name = "m32gltf-textures", isArgumentOptional = true)
	public void setIncludeTextures(boolean value) {
		this.includeTextures = value;
	}

	public boolean getIncludeTextures() {
		return this.includeTextures;
	}

	@Override
	public Converter createConverter() {
		return new M32Gltf(this.includeTextures);
	}

	@Override
	public void applyArguments(ConverterArgs args) {
		args.onHas("m32gltf-textures", value -> setIncludeTextures(ArgumentHelper.toBoolean(value, getIncludeTextures())));
	}
}
