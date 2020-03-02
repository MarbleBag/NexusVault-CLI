package nexusvault.cli.plugin.export;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import nexusvault.archive.IdxPath;
import nexusvault.archive.util.DataHeader;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureObject;
import nexusvault.format.tex.TextureReader;
import nexusvault.format.tex.util.TextureImageAwtConverter;

final class TextureExporter implements Exporter {

	private TextureReader reader;

	@Override
	public Set<String> getAcceptedFileEndings() {
		return Collections.singleton("tex");
	}

	@Override
	public boolean accepts(DataHeader header) {
		return this.reader.acceptFileSignature(header.getSignature()) && this.reader.acceptFileVersion(header.getVersion());
	}

	@Override
	public void export(Path outputFolder, ByteBuffer data, IdxPath dataName) throws IOException {
		final TextureObject texture = this.reader.read(data);
		final TextureImage textureImage = texture.getImage(0);
		if (textureImage == null) {
			throw new IllegalStateException("No Image was created: " + PathUtil.getFullName(dataName));
		}

		final Path destination = outputFolder.resolve(PathUtil.getFolder(dataName));
		Files.createDirectories(destination);

		final String imageName = PathUtil.getNameWithoutExtension(dataName);

		saveImage(textureImage, destination, String.format("%s.png", imageName));

		final var textureSplitter = new nexusvault.format.tex.TextureImageSplitter();
		if (textureSplitter.isSplitable(texture.getTextureDataType())) {
			final List<TextureImage> textureComponents = textureSplitter.split(textureImage, texture.getTextureDataType());
			for (int i = 0; i < textureComponents.size(); ++i) {
				saveImage(textureComponents.get(i), destination, String.format("%s.%d.png", imageName, i));
			}
		}
	}

	private void saveImage(TextureImage textureImage, Path destination, String fileName) throws IOException {
		final BufferedImage image = TextureImageAwtConverter.convertToBufferedImage(textureImage);
		destination = destination.resolve(Paths.get(fileName));
		saveImage(image, destination);
	}

	private void saveImage(BufferedImage image, final Path destination) throws IOException {
		try (OutputStream writer = Files.newOutputStream(destination, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			ImageIO.write(image, "PNG", writer);
		}
	}

	@Override
	public void initialize() {
		this.reader = TextureReader.buildDefault();
	}

	@Override
	public void deinitialize() {
		this.reader = null;
	}

}
