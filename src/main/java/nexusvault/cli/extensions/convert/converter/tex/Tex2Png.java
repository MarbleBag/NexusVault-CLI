package nexusvault.cli.extensions.convert.converter.tex;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageSplitter;
import nexusvault.format.tex.TextureReader;
import nexusvault.format.tex.util.AwtImageConverter;

public final class Tex2Png implements Converter {

	private final boolean splitImages;
	private final boolean exportMipMaps;
	private TextureReader reader;
	private TextureImageSplitter textureSplitter;

	public Tex2Png(boolean splitImages, boolean exportMipMaps) {
		this.splitImages = splitImages;
		this.exportMipMaps = exportMipMaps;

		this.reader = TextureReader.buildDefault();
		this.textureSplitter = new nexusvault.format.tex.TextureImageSplitter();
	}

	@Override
	public void deinitialize() {
		this.reader = null;
		this.textureSplitter = null;
	}

	@Override
	public void convert(ConversionManager manager) throws IOException {
		final var resource = manager.getResource();
		final var imageObject = this.reader.read(resource.getData());
		final var images = new LinkedList<TextureImage>();
		images.add(imageObject.getImage(0));

		if (this.exportMipMaps) {
			for (var i = 1; i < imageObject.getMipMapCount(); ++i) {
				images.add(imageObject.getImage(i));
			}
		}

		final var fileName = PathUtil.getFileName(resource.getFile());
		for (var i = 0; i < images.size(); ++i) {
			final var image = images.get(i);
			final var outputPath = manager.resolveOutputPath(getFileName(fileName, i) + ".png");
			writeImage(image, outputPath);
			manager.addCreatedFile(outputPath);
		}

		if (this.splitImages && this.textureSplitter.isSplitable(imageObject.getTextureDataType())) {
			for (var i = 0; i < images.size(); ++i) {
				final var textureComponents = this.textureSplitter.split(images.get(i), imageObject.getTextureDataType());
				for (var j = 0; j < textureComponents.size(); ++j) {
					final var outputPath = manager.resolveOutputPath(getFileName(fileName, i) + String.format(".%d.png", j));
					writeImage(textureComponents.get(j), outputPath);
					manager.addCreatedFile(outputPath);
				}
			}
		}
	}

	private String getFileName(String fileName, int mipmap) {
		if (mipmap == 0) {
			return fileName;
		}
		return fileName + String.format(".m%02d", mipmap);
	}

	private void writeImage(TextureImage image, Path path) throws IOException {
		final var bufferedImage = AwtImageConverter.convertToBufferedImage(image);
		try (OutputStream writer = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			ImageIO.write(bufferedImage, "PNG", writer);
		}
	}

}
