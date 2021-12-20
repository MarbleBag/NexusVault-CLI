package nexusvault.cli.extensions.convert.converter.tex;

import nexusvault.cli.core.cmd.ArgumentHelper;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsArgument;
import nexusvault.cli.extensions.convert.IsFactory;

@IsFactory(id = "tex2png", fileExtensions = { "tex" }, priority = 1)
public final class Tex2PngFactory implements ConverterFactory {

	private boolean splitImage = false;
	private boolean exportMipMaps = false;

	@IsArgument(name = "tex2png-split", description = "Toggles the splitting of textures. By default off. Can be set directly to 'on' or 'off'. Some textures use their color and alpha channels to store different information, like normal maps, masks and roughness. If set to on, the exporter will export will not only export the image, but each channel as a separate image if applicable.")
	public void setSplitImage(boolean value) {
		this.splitImage = value;
	}

	@IsArgument(name = "tex2png-mipmaps", description = "Toggles the export of mipmaps. By default off. Can be set directly to 'on' or 'off'.")
	public void setExportMipMaps(boolean value) {
		this.exportMipMaps = value;
	}

	@Override
	public void applyArguments(ConverterArgs args) {
		args.onHas("tex2png-split", value -> setSplitImage(ArgumentHelper.toBoolean(value, this.splitImage)));
		args.onHas("tex2png-mipmaps", value -> setExportMipMaps(ArgumentHelper.toBoolean(value, this.exportMipMaps)));
	}

	@Override
	public Converter createConverter() {
		return new Tex2Png(this.splitImage, this.exportMipMaps);
	}

}
