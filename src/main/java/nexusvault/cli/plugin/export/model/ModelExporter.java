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

import kreed.util.property.PropertyChangedEvent;
import kreed.util.property.PropertyListener;
import kreed.util.property.provider.BoolProvider;
import nexusvault.archive.IdxPath;
import nexusvault.archive.util.DataHeader;
import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.EventSystem;
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
				return opt;
			}
		}

		private final ModelSet<Key> data;

		public ModelConfigModel() {
			data = new ModelSet<>(Arrays.asList(Key.values()));

			data.setListener(new PropertyListener<Key>() {
				@Override
				public void onPropertyChange(PropertyChangedEvent<Key> property) {
					final EventSystem eventSystem = App.getInstance().getEventSystem();
					if (eventSystem == null) {
						return;
					}
					final String eventName = String.valueOf(property.key).toLowerCase();
					App.getInstance().getEventSystem()
							.postEvent(new ModelPropertyChangedEvent<>(eventName, String.valueOf(property.oldValue), String.valueOf(property.newValue)) {
							});
				}
			});
		}

		public boolean isIncludeTexture() {
			return data.getProperty(Key.INCLUDE_TEXTURE);
		}

		public boolean setIncludeTextureh(boolean value) {
			return data.setProperty(Key.INCLUDE_TEXTURE, value);
		}
	}

	public static enum ModelSetting {
		INCLUDE_TEXTURE
	}

	private ModelReader modelReader;
	private ExporterType exporterType;
	private ModelConfigModel configModel;
	private List<Command> cmds;

	@Override
	public void initialize() {
		modelReader = new ModelReader();
		exporterType = ExporterType.GLTF;
		configModel = new ModelConfigModel();

		cmds = new ArrayList<>();
		cmds.add(new ModelExportTypeCmd(this));
		cmds.add(new ModelExportSettingCmd(this));

		final var cli = App.getInstance().getCLI();
		cmds.forEach(cli::registerCommand);
	}

	@Override
	public void deinitialize() {
		final var cli = App.getInstance().getCLI();
		cmds.forEach(cli::unregisterCommand);

		configModel = null;
		modelReader = null;
		cmds = null;
	}

	private InternalModelExporter getExporter() {
		switch (exporterType) {
			case DEBUG:
				return new DebugInternalModelExporter();
			case GLTF:
				return new GlTFInternalModelExporter(this);
			case OBJ:
				return new ObjInternalModelExporter();
			default:
				throw new IllegalStateException("Exporter not available: " + exporterType);
		}
	}

	public ModelConfigModel getConfig() {
		return configModel;
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
		return modelReader.acceptFileSignature(header.getSignature()) && modelReader.acceptFileVersion(header.getVersion());
	}

	@Override
	public void export(Path outputFolder, ByteBuffer data, IdxPath dataName) throws IOException {
		final Model model = modelReader.read(data);

		final Path modelFolder = outputFolder.resolve(PathUtil.getFolder(dataName));
		Files.createDirectories(modelFolder);
		getExporter().export(model, modelFolder, dataName);
	}

}
