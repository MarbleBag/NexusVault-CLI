package nexusvault.cli.extensions.convert.converter.tex;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.imageio.ImageIO;

import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageSplitter;
import nexusvault.format.tex.TextureReader;
import nexusvault.format.tex.util.AwtImageConverter;

public final class Tex2Png implements Converter {

	private TextureReader reader;
	private TextureImageSplitter textureSplitter;
	private final boolean splitImages;

	public Tex2Png(boolean splitImages) {
		this.reader = TextureReader.buildDefault();
		this.textureSplitter = new nexusvault.format.tex.TextureImageSplitter();
		this.splitImages = splitImages;
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
		final var image = imageObject.getImage(0);
		final var outputPath = manager.resolveOutputPath(PathUtil.replaceFileExtension(resource.getFile(), "png"));

		writeImage(image, outputPath);
		manager.addCreatedFile(outputPath);

		if (this.splitImages && this.textureSplitter.isSplitable(imageObject.getTextureDataType())) {
			final var textureComponents = this.textureSplitter.split(image, imageObject.getTextureDataType());
			for (int i = 0; i < textureComponents.size(); ++i) {
				final var splitOutputPath = PathUtil.addFileNameSuffix(outputPath, String.format(".%d", i));
				writeImage(textureComponents.get(i), splitOutputPath);
				manager.addCreatedFile(splitOutputPath);
			}
		}
	}

	private void writeImage(TextureImage image, Path path) throws IOException {
		final var bufferedImage = AwtImageConverter.convertToBufferedImage(image);
		try (OutputStream writer = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			ImageIO.write(bufferedImage, "PNG", writer);
		}
	}

}
