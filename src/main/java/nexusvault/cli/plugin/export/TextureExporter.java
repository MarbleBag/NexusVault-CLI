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

import nexusvault.archive.IdxFileLink;
import nexusvault.archive.util.DataHeader;
import nexusvault.cli.App;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureObject;
import nexusvault.format.tex.TextureReader;

final class TextureExporter implements Exporter {

	private TextureReader reader;

	@Override
	public Set<String> getAcceptedFileEndings() {
		return Collections.singleton("tex");
	}

	@Override
	public boolean accepts(DataHeader header) {
		return reader.acceptFileSignature(header.getSignature()) && reader.acceptFileVersion(header.getVersion());
	}

	@Override
	public void export(IdxFileLink fileLink, ByteBuffer data) throws IOException {
		final TextureObject texture = reader.read(data);
		final TextureImage textureImage = texture.getImage(0);
		if (textureImage == null) {
			throw new IllegalStateException("No Image was created: " + fileLink.fullName());
		}

		final Path outputFolder = App.getInstance().getAppConfig().getOutputPath();
		final Path destination = outputFolder.resolve(fileLink.fullName()).getParent();
		Files.createDirectories(destination);

		final String imageName = fileLink.getName().substring(0, fileLink.getName().lastIndexOf('.'));

		saveImage(textureImage, destination, String.format("%s.png", imageName));

		final List<TextureImage> textureComponents = texture.splitImageIntoComponents(textureImage);

		for (int i = 0; i < textureComponents.size(); ++i) {
			saveImage(textureComponents.get(i), destination, String.format("%s.%d.png", imageName, i));
		}
	}

	private void saveImage(TextureImage textureImage, Path destination, String fileName) throws IOException {
		final BufferedImage image = textureImage.convertToBufferedImage();
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
		reader = TextureReader.buildDefault();
	}

	@Override
	public void deinitialize() {
		reader = null;
	}

}
