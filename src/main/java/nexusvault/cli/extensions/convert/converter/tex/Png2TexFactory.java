package nexusvault.cli.extensions.convert.converter.tex;

import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterException;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsArgument;
import nexusvault.format.tex.TexType;

@AutoInstantiate
// @IsFactory(id = "png2tex", priority = 1, fileExtensions = { "png" })
public final class Png2TexFactory implements ConverterFactory {

	private TexType texType = TexType.ARGB_1;
	private int mipmapCount = -1;
	private int quality = 100;
	private final int[] defaultColor = new int[] { -1, -1, -1, -1 };

	@IsArgument(name = "png2tex-type", description = "WS specific texture type.")
	public void setType(TexType texType) {
		this.texType = texType;
	}

	@IsArgument(name = "png2tex-mipmaps", description = "How many mip maps the texture should contain. Maximal number of mip maps is 13.")
	public void setMipmaps(int mipmaps) {
		if (mipmaps < 0 || mipmaps > 13) {
			throw new ConverterException();
		}
		this.mipmapCount = mipmaps;
	}

	@IsArgument(name = "png2tex-quality", description = "Only used for JPEG 1 to 3. Requires a numbers from 0 to 100. It represents represents the quality of the created file, the higher the quality, the less it gets compressed.")
	public void setQuality(int quality) {
		if (quality < 0 || quality > 100) {
			throw new ConverterException();
		}
		this.quality = quality;
	}

	@IsArgument(name = "png2tex-defaults", description = "Only used for JPEG 1 to 3. Requires 4 numbers from -1 to 255, -1 means it's not used. Each number represents a shade of grey for that channel. Used to reduce the file size for channels which only contain a uniform shade.")
	public void setDefaultColor(int colorA, int colorB, int colorC, int colorD) {
		if (!(colorA == -1 || 0 < colorA && colorA < 255)) {
			throw new ConverterException();
		}
		if (!(colorB == -1 || 0 < colorB && colorB < 255)) {
			throw new ConverterException();
		}
		if (!(colorC == -1 || 0 < colorC && colorC < 255)) {
			throw new ConverterException();
		}
		if (!(colorD == -1 || 0 < colorD && colorD < 255)) {
			throw new ConverterException();
		}

		this.defaultColor[0] = colorA;
		this.defaultColor[1] = colorB;
		this.defaultColor[2] = colorC;
		this.defaultColor[3] = colorD;
	}

	@Override
	public void applyArguments(ConverterArgs arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public Converter createConverter() {
		return new Png2Tex(this.texType, this.mipmapCount, this.quality, this.defaultColor);
	}

}
