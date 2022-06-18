package nexusvault.cli.extensions.convert.converter.m3;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import nexusvault.cli.core.App;
import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.archive.ArchiveExtension;
import nexusvault.cli.extensions.archive.NexusArchiveContainer;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.export.m3.gltf.GlTFExportMonitor;
import nexusvault.export.m3.gltf.PathTextureResource;
import nexusvault.export.m3.gltf.ResourceBundle;
import nexusvault.format.m3.ModelReader;
import nexusvault.format.tex.Image;
import nexusvault.format.tex.TextureReader;
import nexusvault.format.tex.util.AwtImageConverter;
import nexusvault.vault.IdxEntry.IdxFileLink;
import nexusvault.vault.IdxPath;

public final class M32Gltf implements Converter {

	private List<NexusArchiveContainer> archiveContainers;
	private final boolean includeTextures;

	public M32Gltf(boolean includeTextures) {
		this.archiveContainers = App.getInstance().getExtension(ArchiveExtension.class).getArchives();
		this.includeTextures = includeTextures;
	}

	@Override
	public void deinitialize() {
		this.archiveContainers = null;
	}

	@Override
	public void convert(ConversionManager manager) throws IOException {
		final var gltfExporter = nexusvault.export.m3.gltf.GlTFExporter.makeExporter();
		gltfExporter.setGlTFExportMonitor(new GlTFExportMonitor() {
			@Override
			public void requestTexture(String textureId, ResourceBundle resourceBundle) {
				if (M32Gltf.this.includeTextures) {
					try {
						findAndLoatTexture(manager.getOutputPath(), textureId, resourceBundle);
					} catch (final IOException e) {
						throw new IllegalStateException(e); // TODO
					}
				}
			}

			@Override
			public void newFileCreated(Path path) {
				manager.addCreatedFile(path);
			}
		});

		final var resource = manager.getResource();
		final var data = resource.getData();
		final var m3 = ModelReader.read(data);
		gltfExporter.exportModel(manager.getOutputPath(), PathUtil.getFileName(resource.getFile()), m3);
	}

	private IdxFileLink find(String textureId) throws IOException {
		final var path = IdxPath.createPathFrom(textureId);
		for (final var container : this.archiveContainers) {
			final var entry = container.getArchive().find(path);
			if (entry.isPresent()) {
				return entry.get().isFile() ? entry.get().asFile() : null;
			}
		}
		return null;
	}

	private void findAndLoatTexture(Path outputDir, String textureId, ResourceBundle resourceBundle) throws IOException {
		final var textureLink = find(textureId);
		if (textureLink == null) {
			return;
		}

		outputDir = outputDir.resolve("textures");
		Files.createDirectories(outputDir);

		final var images = new ArrayList<Image>();
		images.add(TextureReader.readFirstImage(textureLink.getData()));

		for (int i = 0; i < images.size(); i++) {
			final String fileName = String.format("%s.%d.png", textureLink.getNameWithoutFileExtension(), i);
			final Path filePath = outputDir.resolve(Path.of(fileName));
			try (OutputStream writer = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING)) {
				final var bufferedImage = AwtImageConverter.convertToBufferedImage(images.get(i));
				ImageIO.write(bufferedImage, "PNG", writer);
			}

			resourceBundle.addTextureResource(new PathTextureResource(filePath));
		}
	}
}
