package nexusvault.cli.plugin.export.tex;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import kreed.util.property.provider.BoolProvider;
import nexusvault.archive.IdxPath;
import nexusvault.archive.util.DataHeader;
import nexusvault.cli.core.App;
import nexusvault.cli.core.EventManager;
import nexusvault.cli.core.cmd.CommandHandler;
import nexusvault.cli.model.ModelPropertyChangedEvent;
import nexusvault.cli.model.ModelSet;
import nexusvault.cli.model.PropertyKey;
import nexusvault.cli.model.PropertyOption;
import nexusvault.cli.plugin.export.Exporter;
import nexusvault.cli.plugin.export.PathUtil;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureObject;
import nexusvault.format.tex.TextureReader;
import nexusvault.format.tex.util.TextureImageAwtConverter;

public final class TextureExporter implements Exporter {

	public static class TexConfigModel {
		private static enum Key implements PropertyKey<Key> {
			SPLIT_TEXTURE(new PropertyOption<Key>("texture.split", true, Boolean.class, new BoolProvider<>(true)));

			private final PropertyOption<Key> opt;

			private Key(PropertyOption<Key> opt) {
				this.opt = opt;
			}

			@Override
			public PropertyOption<Key> getOptions() {
				return this.opt;
			}
		}

		private final ModelSet<Key> data;

		public TexConfigModel() {
			this.data = new ModelSet<>(Arrays.asList(Key.values()));

			this.data.setListener(property -> {
				final EventManager eventSystem = App.getInstance().getEventSystem();
				if (eventSystem == null) {
					return;
				}
				final String eventName = String.valueOf(property.key).toLowerCase();
				App.getInstance().getEventSystem()
						.postEvent(new ModelPropertyChangedEvent<>(eventName, String.valueOf(property.oldValue), String.valueOf(property.newValue)) {
						});
			});
		}

		public boolean isSplitTexture() {
			return this.data.getProperty(Key.SPLIT_TEXTURE);
		}

		public boolean setSplitTexture(boolean value) {
			return this.data.setProperty(Key.SPLIT_TEXTURE, value);
		}
	}

	private TextureReader reader;
	private TexConfigModel configModel;
	private List<CommandHandler> cmds;

	@Override
	public void initialize() {
		this.reader = TextureReader.buildDefault();
		this.configModel = new TexConfigModel();

		this.cmds = new ArrayList<>();
		this.cmds.add(new TextureExportSplitCmd(this));

		final var cli = App.getInstance().getCLI();
		this.cmds.forEach(cli::registerCommand);
	}

	@Override
	public void deinitialize() {
		final var cli = App.getInstance().getCLI();
		this.cmds.forEach(cli::unregisterCommand);

		this.configModel = null;
		this.reader = null;
	}

	public TexConfigModel getConfig() {
		return this.configModel;
	}

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

		if (getConfig().isSplitTexture()) {
			final var textureSplitter = new nexusvault.format.tex.TextureImageSplitter();
			if (textureSplitter.isSplitable(texture.getTextureDataType())) {
				final List<TextureImage> textureComponents = textureSplitter.split(textureImage, texture.getTextureDataType());
				for (int i = 0; i < textureComponents.size(); ++i) {
					saveImage(textureComponents.get(i), destination, String.format("%s.%d.png", imageName, i));
				}
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

}
