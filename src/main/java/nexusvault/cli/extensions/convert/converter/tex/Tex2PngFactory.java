package nexusvault.cli.extensions.convert.converter.tex;

import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.cmd.ArgumentHelper;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsArgument;
import nexusvault.cli.extensions.convert.IsFactory;

@AutoInstantiate
@IsFactory(id = "tex2png", fileExtensions = { "tex" }, priority = 1)
public final class Tex2PngFactory implements ConverterFactory {

	private boolean splitImage = true;

	@IsArgument(name = "tex2png-split", description = "Toggles the splitting of textures. By default, this is on. Can be set directly to 'on' or 'off'. Some textures use their color and alpha channels to store different information, like normal maps, masks and roughness. If set to on, the exporter will export will not only export the image, but each channel as a separate image if applicable.")
	public void setSplitImage(boolean value) {
		this.splitImage = value;
	}

	@Override
	public void applyArguments(ConverterArgs args) {
		if (args.has("tex2png-split")) {
			setSplitImage(ArgumentHelper.toBoolean(args.get("tex2png-split"), this.splitImage));
		}
	}

	@Override
	public Converter createConverter() {
		return new Tex2Png(this.splitImage);
	}

}
