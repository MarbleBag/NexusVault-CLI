package nexusvault.cli.plugin.export.model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
import nexusvault.format.m3.Model;
import nexusvault.format.m3.v100.ModelReader;

public final class ModelExporter implements Exporter {

	public static class ModelConfigModel {
		private static enum Key implements PropertyKey<Key> {
			INCLUDE_TEXTURE(new PropertyOption<Key>("model.texture.include", false, Boolean.class, new BoolProvider<>(true)));

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

		public ModelConfigModel() {
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

		public boolean isIncludeTexture() {
			return this.data.getProperty(Key.INCLUDE_TEXTURE);
		}

		public boolean setIncludeTextures(boolean value) {
			return this.data.setProperty(Key.INCLUDE_TEXTURE, value);
		}
	}

	public static enum ModelSetting {
		INCLUDE_TEXTURE
	}

	private ModelReader modelReader;
	private ExporterType exporterType;
	private ModelConfigModel configModel;
	private List<CommandHandler> cmds;

	@Override
	public void initialize() {
		this.modelReader = new ModelReader();
		this.exporterType = ExporterType.GLTF;
		this.configModel = new ModelConfigModel();

		this.cmds = new ArrayList<>();
		this.cmds.add(new ModelExportTypeCmd(this));
		this.cmds.add(new ModelExportSettingCmd(this));

		final var cli = App.getInstance().getCLI();
		this.cmds.forEach(cli::registerCommand);
	}

	@Override
	public void deinitialize() {
		final var cli = App.getInstance().getCLI();
		this.cmds.forEach(cli::unregisterCommand);

		this.configModel = null;
		this.modelReader = null;
		this.cmds = null;
	}

	private InternalModelExporter getExporter() {
		switch (this.exporterType) {
			case DEBUG:
				return new DebugInternalModelExporter();
			case GLTF:
				return new GlTFInternalModelExporter(this);
			case OBJ:
				return new ObjInternalModelExporter();
			default:
				throw new IllegalStateException("Exporter not available: " + this.exporterType);
		}
	}

	public ModelConfigModel getConfig() {
		return this.configModel;
	}

	public void setExportType(ExporterType exporterType) {
		final ExporterType oldValue = this.exporterType;
		this.exporterType = exporterType;

		App.getInstance().getEventSystem().postEvent(new ModelPropertyChangedEvent<>("m3-type", String.valueOf(oldValue), String.valueOf(this.exporterType)) {
		});
	}

	@Override
	public Set<String> getAcceptedFileEndings() {
		return Collections.singleton("m3");
	}

	@Override
	public boolean accepts(DataHeader header) {
		return this.modelReader.acceptFileSignature(header.getSignature()) && this.modelReader.acceptFileVersion(header.getVersion());
	}

	@Override
	public void export(Path outputFolder, ByteBuffer data, IdxPath dataName) throws IOException {
		final Model model = this.modelReader.read(data);

		final Path modelFolder = outputFolder.resolve(PathUtil.getFolder(dataName));
		Files.createDirectories(modelFolder);
		getExporter().export(model, modelFolder, dataName);
	}

}
