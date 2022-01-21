package nexusvault.cli.extensions.convert.converter.tex;

import nexusvault.cli.core.cmd.ArgumentHelper;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsArgument;
import nexusvault.cli.extensions.convert.IsFactory;

@IsFactory(id = "tex2png", fileExtensions = { "tex" }, priority = 1)
public final class Tex2PngFactory implements ConverterFactory {

	private boolean exportMipMaps = false;

	@IsArgument(name = "tex2png-mipmaps", description = "Toggles the export of mipmaps. By default off. Can be set directly to 'on' or 'off'.", isArgumentOptional = true)
	public void setExportMipMaps(boolean value) {
		this.exportMipMaps = value;
	}

	@Override
	public void applyArguments(ConverterArgs args) {
		args.onHas("tex2png-mipmaps", value -> setExportMipMaps(ArgumentHelper.toBoolean(value, this.exportMipMaps)));
	}

	@Override
	public Converter createConverter() {
		return new Tex2Png(this.exportMipMaps);
	}

}
