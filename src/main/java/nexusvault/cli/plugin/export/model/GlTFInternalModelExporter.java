package nexusvault.cli.plugin.export.model;

import java.awt.image.BufferedImage;
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
import nexusvault.cli.App;
import nexusvault.cli.plugin.archive.ArchivePlugIn;
import nexusvault.cli.plugin.archive.NexusArchiveWrapper;
import nexusvault.cli.plugin.export.PathUtil;
import nexusvault.cli.plugin.export.model.ModelExporter.ModelConfigModel;
import nexusvault.format.m3.Model;
import nexusvault.format.m3.export.gltf.GlTFExportMonitor;
import nexusvault.format.m3.export.gltf.GlTFExporter;
import nexusvault.format.m3.export.gltf.PathTextureResource;
import nexusvault.format.m3.export.gltf.ResourceBundle;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureObject;
import nexusvault.format.tex.TextureReader;
import nexusvault.format.tex.util.TextureImageAwtConverter;

final class GlTFInternalModelExporter implements InternalModelExporter {

	private final ModelConfigModel config;

	public GlTFInternalModelExporter(ModelExporter modelExporter) {
		this.config = modelExporter.getConfig();
	}

	private IdxFileLink find(List<NexusArchiveWrapper> wrappers, String textureId) {
		for (final NexusArchiveWrapper wrapper : wrappers) {
			final IdxDirectory root = wrapper.getArchive().getRootDirectory();
			final IdxPath path = IdxPath.createPathFrom(textureId);
			if (path.isResolvable(root)) {
				final IdxEntry entry = path.resolve(root);
				return entry.isFile() ? entry.asFile() : null;
			}
		}
		return null;
	}

	@Override
	public void export(Model model, Path dstFolder, IdxPath filePath) throws IOException {
		final GlTFExporter gltfExporter = nexusvault.format.m3.export.gltf.GlTFExporter.makeExporter();
		final String modelName = PathUtil.getNameWithoutExtension(filePath);

		final List<NexusArchiveWrapper> wrappers = App.getInstance().getPlugIn(ArchivePlugIn.class).getArchives();
		final boolean searchTexture = this.config.isIncludeTexture();

		gltfExporter.setGlTFExportMonitor(new GlTFExportMonitor() {
			@Override
			public void requestTexture(String textureId, ResourceBundle resourceBundle) {
				if (searchTexture) {
					findAndSetTexture(dstFolder, modelName, wrappers, textureId, resourceBundle);
				}
			}

			private void findAndSetTexture(Path dstFolder, final String modelName, final List<NexusArchiveWrapper> wrappers, String textureId,
					ResourceBundle resourceBundle) {
				final IdxFileLink textureLink = find(wrappers, textureId);
				if (textureLink == null) {
					return;
				}

				try {
					final TextureReader textureReader = TextureReader.buildDefault();
					final TextureObject textureObject = textureReader.read(textureLink.getData());
					final TextureImage origin = textureObject.getImage(0);

					final List<TextureImage> images = new ArrayList<>();

					final var textureSplitter = new nexusvault.format.tex.TextureImageSplitter();

					if (textureSplitter.isSplitable(textureObject)) {
						images.addAll(textureSplitter.split(origin, textureObject.getTextureDataType()));
					} else {
						images.add(origin);
					}

					final Path dst = dstFolder.resolve(modelName + "_textures"); // TODO for now
					Files.createDirectories(dst);

					for (int i = 0; i < images.size(); i++) {
						final TextureImage image = images.get(i);
						final String newfileName = String.format("%s.%d.png", textureLink.getNameWithoutFileExtension(), i);
						final BufferedImage bufferedImage = TextureImageAwtConverter.convertToBufferedImage(image);

						final Path texDst = dst.resolve(Paths.get(newfileName));
						try (OutputStream writer = Files.newOutputStream(texDst, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
								StandardOpenOption.TRUNCATE_EXISTING)) {
							ImageIO.write(bufferedImage, "PNG", writer);
						}

						resourceBundle.addTextureResource(new PathTextureResource(texDst));
					}
				} catch (final IOException e1) {
					throw new IllegalStateException(e1);
				}
			}

			@Override
			public void newFileCreated(Path path) {
				// TODO Auto-generated method stub

			}
		});

		gltfExporter.exportModel(dstFolder, modelName, model);
	}
}