package nexusvault.cli.extensions.convert.converter.tex;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.imageio.ImageIO;

import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterException;
import nexusvault.cli.extensions.convert.resource.Resource;
import nexusvault.format.tex.Image;
import nexusvault.format.tex.Image.ImageFormat;
import nexusvault.format.tex.TextureType;
import nexusvault.format.tex.TextureWriter;
import nexusvault.format.tex.util.AwtImageConverter;

public final class Png2Tex implements Converter {

	private final TextureType target;
	private final int mipmapCount;
	private final int depth;
	private final int sides;
	private final int quality;
	private final int[] defaultColors;

	public Png2Tex(TextureType texType, int depth, int sides, int mipmapCount, int quality, int[] defaultColors) {
		this.target = texType;
		this.mipmapCount = Math.max(-1, Math.min(13, mipmapCount));
		this.depth = depth;
		this.sides = sides;
		this.quality = quality;
		this.defaultColors = defaultColors;
	}

	@Override
	public void deinitialize() {

	}

	@Override
	public void convert(ConversionManager manager) throws IOException {
		final var resource = manager.getResource();
		final var baseImage = loadImage(resource);

		final var binaryData = TextureWriter.toBinary(this.target, baseImage, this.mipmapCount, this.quality, this.defaultColors);

		final var outputPath = manager.resolveOutputPath(PathUtil.replaceFileExtension(resource.getFile(), "tex"));
		manager.addCreatedFile(outputPath);

		try (var channel = Files.newByteChannel(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
			channel.write(ByteBuffer.wrap(binaryData));
		}
	}

	private Image loadImage(Resource resource) throws IOException {
		BufferedImage image;
		try (var stream = resource.getDataAsStream()) {
			image = ImageIO.read(stream);
		}

		switch (this.target) {
			case ARGB1:
			case ARGB2:
			case DXT1:
			case DXT3:
			case DXT5:
			case JPG1:
			case JPG2:
			case JPG3:
				return AwtImageConverter.convertToTextureImage(ImageFormat.ARGB, image);
			case RGB:
				return AwtImageConverter.convertToTextureImage(ImageFormat.RGB, image);
			case GRAYSCALE:
				return AwtImageConverter.convertToTextureImage(ImageFormat.GRAYSCALE, image);
			default:
				throw new ConverterException(String.format("Unknown type '%s'", this.target));
		}
	}

}
