package nexusvault.cli.extensions.convert.converter.m3;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.IdxFileLink;
import nexusvault.archive.IdxPath;
import nexusvault.cli.core.App;
import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.archive.ArchiveExtension;
import nexusvault.cli.extensions.archive.NexusArchiveContainer;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.format.m3.export.gltf.GlTFExportMonitor;
import nexusvault.format.m3.export.gltf.PathTextureResource;
import nexusvault.format.m3.export.gltf.ResourceBundle;
import nexusvault.format.m3.v100.ModelReader;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageSplitter;
import nexusvault.format.tex.TextureReader;
import nexusvault.format.tex.util.AwtImageConverter;

public final class M32Gltf implements Converter {

	private ModelReader modelReader;
	private TextureReader textureReader;
	private TextureImageSplitter textureSplitter;
	private List<NexusArchiveContainer> archiveContainers;
	private final boolean includeTextures;

	public M32Gltf(boolean includeTextures) {
		this.modelReader = new ModelReader();
		this.textureReader = TextureReader.buildDefault();
		this.textureSplitter = new nexusvault.format.tex.TextureImageSplitter();
		this.archiveContainers = App.getInstance().getExtension(ArchiveExtension.class).getArchives();
		this.includeTextures = includeTextures;
	}

	@Override
	public void deinitialize() {
		this.modelReader = null;
		this.textureReader = null;
		this.textureSplitter = null;
		this.archiveContainers = null;
	}

	@Override
	public void convert(ConversionManager manager) throws IOException {
		final var gltfExporter = nexusvault.format.m3.export.gltf.GlTFExporter.makeExporter();
		gltfExporter.setGlTFExportMonitor(new GlTFExportMonitor() {
			@Override
			public void requestTexture(String textureId, ResourceBundle resourceBundle) {
				if (M32Gltf.this.includeTextures) {
					findAndLoatTexture(manager.getOutputPath(), textureId, resourceBundle);
				}
			}

			@Override
			public void newFileCreated(Path path) {
				manager.addCreatedFile(path);
			}
		});

		final var resource = manager.getResource();
		final var m3 = this.modelReader.read(resource.getDataAsBuffer());
		gltfExporter.exportModel(manager.getOutputPath(), PathUtil.getFileName(resource.getFile()), m3);
	}

	private IdxFileLink find(String textureId) {
		final IdxPath path = IdxPath.createPathFrom(textureId);
		for (final var container : this.archiveContainers) {
			final IdxDirectory root = container.getArchive().getRootDirectory();
			if (path.isResolvable(root)) {
				final IdxEntry entry = path.resolve(root);
				return entry.isFile() ? entry.asFile() : null;
			}
		}
		return null;
	}

	private void findAndLoatTexture(Path outputDir, String textureId, ResourceBundle resourceBundle) {
		final var textureLink = find(textureId);
		if (textureLink == null) {
			return;
		}

		try {
			final var textureObject = this.textureReader.read(textureLink.getData());
			final var origin = textureObject.getImage(0);
			final var images = new ArrayList<TextureImage>();

			if (this.textureSplitter.isSplitable(textureObject)) {
				images.addAll(this.textureSplitter.split(origin, textureObject.getTextureDataType()));
			} else {
				images.add(origin);
			}

			outputDir = outputDir.resolve("textures");
			Files.createDirectories(outputDir);

			for (int i = 0; i < images.size(); i++) {
				final String fileName = String.format("%s.%d.png", textureLink.getNameWithoutFileExtension(), i);
				final Path filePath = outputDir.resolve(Paths.get(fileName));
				try (OutputStream writer = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
						StandardOpenOption.TRUNCATE_EXISTING)) {

					final var bufferedImage = AwtImageConverter.convertToBufferedImage(images.get(i));
					ImageIO.write(bufferedImage, "PNG", writer);

				}

				resourceBundle.addTextureResource(new PathTextureResource(filePath));
			}
		} catch (final IOException e1) {
			throw new IllegalStateException(e1);
		}
	}
}
