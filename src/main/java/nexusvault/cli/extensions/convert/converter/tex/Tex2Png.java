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
import nexusvault.format.tex.Image;
import nexusvault.format.tex.TextureReader;
import nexusvault.format.tex.util.AwtImageConverter;

public final class Tex2Png implements Converter {

	private final boolean exportMipMaps;

	public Tex2Png(boolean exportMipMaps) {
		this.exportMipMaps = exportMipMaps;
	}

	@Override
	public void deinitialize() {
	}

	@Override
	public void convert(ConversionManager manager) throws IOException {
		final var resource = manager.getResource();
		final var images = new LinkedList<Image>();
		if (this.exportMipMaps) {
			final var data = resource.getData();
			final var texture = TextureReader.read(data);
			for (var i = 0; i < texture.getMipMapCount(); ++i) {
				images.add(texture.getMipMap(i));
			}
		} else {
			images.add(TextureReader.readFirstImage(resource.getData()));
		}

		final var fileName = PathUtil.getFileName(resource.getFile());
		for (var i = 0; i < images.size(); ++i) {
			final var image = images.get(i);
			final var outputPath = manager.resolveOutputPath(getFileName(fileName, i) + ".png");
			writeImage(image, outputPath);
			manager.addCreatedFile(outputPath);
		}
	}

	private String getFileName(String fileName, int mipmap) {
		if (mipmap == 0) {
			return fileName;
		}
		return fileName + String.format(".m%02d", mipmap);
	}

	private void writeImage(Image image, Path path) throws IOException {
		final var bufferedImage = AwtImageConverter.convertToBufferedImage(image);
		try (OutputStream writer = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			ImageIO.write(bufferedImage, "PNG", writer);
		}
	}

}
