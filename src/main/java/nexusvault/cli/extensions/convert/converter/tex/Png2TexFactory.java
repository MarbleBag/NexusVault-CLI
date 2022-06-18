package nexusvault.cli.extensions.convert.converter.tex;

import java.util.Arrays;
import java.util.stream.Collectors;

import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterException;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsArgument;
import nexusvault.cli.extensions.convert.IsFactory;
import nexusvault.format.tex.TextureType;

@IsFactory(id = "png2tex", fileExtensions = { "png" }, priority = 1)
public final class Png2TexFactory implements ConverterFactory {

	private TextureType texType = TextureType.ARGB1;
	private int mipmapCount = -1;
	private int quality = 100;
	private final int[] defaultColor = new int[] { -1, -1, -1, -1 };
	private int depth = 1;
	private int sides = 1;

	@IsArgument(name = "png2tex-type", description = "WS specific texture type. JPG may or may not work. Defaults to ARGB1.")
	public void setType(TextureType texType) {
		if (texType == null || texType == TextureType.UNKNOWN) {
			throw new ConverterException(String.format("Unknown type '%s', known types are: %s", texType,
					Arrays.asList(TextureType.values()).stream().map(Object::toString).collect(Collectors.joining(", "))));
		}

		this.texType = texType;
	}

	@IsArgument(name = "png2tex-mipmaps", description = "How many mip maps the texture should contain. Maximal number of mip maps is 13. By default the optimal number, down to 1x1, is generated.")
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

	@IsArgument(name = "png2tex-depth", description = "Sets the depth of the texture, minimum 1.")
	public void setDepth(int depth) {
		if (depth < 0) {
			throw new ConverterException();
		}
		this.depth = depth;
	}

	@IsArgument(name = "png2tex-sides", description = "Sets the number of sides of the texture, minimum 1.")
	public void setSides(int sides) {
		if (sides < 0) {
			throw new ConverterException();
		}
		this.sides = sides;
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
		arg.onHasArray("png2tex-defaults",
				values -> setDefaultColor(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3])));
		arg.onHas("png2tex-quality", value -> setQuality(Integer.parseInt(value)));
		arg.onHas("png2tex-mipmaps", value -> setMipmaps(Integer.parseInt(value)));
		arg.onHas("png2tex-type", value -> setType(TextureType.resolve(value)));
		arg.onHas("png2tex-depth", value -> setDepth(Integer.parseInt(value)));
		arg.onHas("png2tex-sides", value -> setSides(Integer.parseInt(value)));
	}

	@Override
	public Converter createConverter() {
		return new Png2Tex(this.texType, this.depth, this.sides, this.mipmapCount, this.quality, this.defaultColor);
	}

}
