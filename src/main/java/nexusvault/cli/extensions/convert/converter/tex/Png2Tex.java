package nexusvault.cli.extensions.convert.converter.tex;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

import javax.imageio.ImageIO;

import kreed.io.util.SeekableByteChannelBinaryWriter;
import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterException;
import nexusvault.cli.extensions.convert.resource.Resource;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.TextureWriter;
import nexusvault.format.tex.jpg.JPGTextureImageWriter;
import nexusvault.format.tex.util.AwtImageConverter;
import nexusvault.format.tex.util.TextureMipMapGenerator;

public final class Png2Tex implements Converter {

	private TextureWriter writer;
	private final TexType target;
	private final HashMap<String, Object> config;
	private final int mipmapCount;

	public Png2Tex(TexType texType, int mipmapCount, int quality, int[] defaultColor) {
		this.writer = TextureWriter.buildDefault();
		this.target = texType;
		this.mipmapCount = Math.min(1, mipmapCount);
		this.config = new HashMap<>();
		this.config.put(JPGTextureImageWriter.CONFIG_QUALITY, quality);
		if (defaultColor[0] != -1) {
			this.config.put(JPGTextureImageWriter.CONFIG_VALUE_LAYER1, defaultColor[0]);
		}
		if (defaultColor[1] != -1) {
			this.config.put(JPGTextureImageWriter.CONFIG_VALUE_LAYER2, defaultColor[1]);
		}
		if (defaultColor[2] != -1) {
			this.config.put(JPGTextureImageWriter.CONFIG_VALUE_LAYER3, defaultColor[2]);
		}
		if (defaultColor[3] != -1) {
			this.config.put(JPGTextureImageWriter.CONFIG_VALUE_LAYER4, defaultColor[3]);
		}
	}

	@Override
	public void deinitialize() {
		this.writer = null;
	}

	@Override
	public void convert(ConversionManager manager) throws IOException {
		final var resource = manager.getResource();
		final var baseImage = loadImage(resource);

		TextureImage[] images;
		if (this.mipmapCount > 1) {
			images = TextureMipMapGenerator.buildMipMaps(baseImage, this.mipmapCount);
		} else {
			images = new TextureImage[] { baseImage };
		}

		final var binaryData = this.writer.write(this.target, images, this.config);

		final var outputPath = manager.resolveOutputPath(PathUtil.replaceFileExtension(resource.getFile(), "tex"));
		manager.addCreatedFile(outputPath);

		Files.createDirectories(outputPath.getParent());
		try (var channel = Files.newByteChannel(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
				var writer = new SeekableByteChannelBinaryWriter(channel, ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN))) {
			channel.write(binaryData);
		}
	}

	private TextureImage loadImage(Resource resource) throws IOException {
		BufferedImage image;
		try (var stream = resource.getDataAsStream()) {
			image = ImageIO.read(stream);
		}

		switch (this.target) {
			case ARGB_1:
			case ARGB_2:
			case DXT1:
			case DXT3:
			case DXT5:
			case JPEG_TYPE_1:
			case JPEG_TYPE_2:
			case JPEG_TYPE_3:
				return AwtImageConverter.convertToTextureImage(TextureImageFormat.ARGB, image);
			case RGB:
				return AwtImageConverter.convertToTextureImage(TextureImageFormat.RGB, image);
			case GRAYSCALE:
				return AwtImageConverter.convertToTextureImage(TextureImageFormat.GRAYSCALE, image);
			default:
				throw new ConverterException(String.format("Unknown type '%s'", this.target));
		}
	}

}
