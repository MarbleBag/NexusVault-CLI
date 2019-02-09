package nexusvault.cli.plugin.config;

import java.nio.file.Path;
import java.util.Arrays;

import kreed.util.property.PropertyChangedEvent;
import kreed.util.property.PropertyListener;
import kreed.util.property.provider.BoolProvider;
import kreed.util.property.provider.PathExtensionProvider;
import nexusvault.cli.App;
import nexusvault.cli.EventSystem;
import nexusvault.cli.model.ModelPropertyChangedEvent;
import nexusvault.cli.model.ModelSet;
import nexusvault.cli.model.PropertyKey;
import nexusvault.cli.model.PropertyOption;

public final class AppConfigModel {

	public static abstract class AppConfigChangedEvent<T> extends ModelPropertyChangedEvent<T> {
		private AppConfigChangedEvent(String eventName, T oldValue, T newValue) {
			super(eventName, oldValue, newValue);
		}
	}

	public static final class AppConfigAppPathChangedEvent extends AppConfigChangedEvent<Path> {
		private AppConfigAppPathChangedEvent(Path oldPath, Path newPath) {
			super("Application Path", oldPath, newPath);
		}
	}

	public static final class AppConfigDebugModeChangedEvent extends AppConfigChangedEvent<Boolean> {
		private AppConfigDebugModeChangedEvent(Boolean oldValue, Boolean newValue) {
			super("Debug Mode", oldValue, newValue);
		}
	}

	public final class AppConfigHeadlessModeChangedEvent extends AppConfigChangedEvent<Boolean> {
		private AppConfigHeadlessModeChangedEvent(Boolean oldValue, Boolean newValue) {
			super("Headless Mode", oldValue, newValue);
		}
	}

	public final class AppConfigOutputPathChangedEvent extends AppConfigChangedEvent<Path> {
		private AppConfigOutputPathChangedEvent(Path oldValue, Path newValue) {
			super("Output Path", oldValue, newValue);
		}
	}

	public final class AppConfigPathChangedEvent extends AppConfigChangedEvent<Path> {
		private AppConfigPathChangedEvent(Path oldValue, Path newValue) {
			super("Config Path", oldValue, newValue);
		}
	}

	private static enum Key implements PropertyKey<Key> {
		APP_PATH(new PropertyOption<Key>("app.path", false, Path.class)),
		OUTPUT_PATH(new PropertyOption<Key>("out.path", true, Path.class)),
		CONFIG_PATH(new PropertyOption<Key>("config.path", true, Path.class)),
		REPORT_PATH(new PropertyOption<Key>("report.path", false, Path.class)),
		HEADLESS_MODE(new PropertyOption<Key>("app.headless", false, Boolean.class, new BoolProvider<>(false))),
		DEBUG_MODE(new PropertyOption<Key>("app.debug", false, Boolean.class, new BoolProvider<>(false))),
		DONT_SAVE_CONFIG(new PropertyOption<Key>("config.save.deactivate", false, Boolean.class, new BoolProvider<>(false)));

		private final PropertyOption<Key> opt;

		private Key(PropertyOption<Key> opt) {
			// new FunctionProvider<Key,Path>(FunctionProvider.FLAG_ON_MISS, (original) ->
			// App.getInstance().getAppConfig().getApplicationPath().resolve("output"))

			this.opt = opt;
		}

		@Override
		public PropertyOption<Key> getOptions() {
			return opt;
		}
	}

	private final ModelSet<Key> data;

	public AppConfigModel() {
		data = new ModelSet<>(Arrays.asList(Key.values()));

		data.setPropertyProvider(Key.OUTPUT_PATH, new PathExtensionProvider<AppConfigModel.Key>(this::getApplicationPath, "output"));
		data.setPropertyProvider(Key.CONFIG_PATH, new PathExtensionProvider<AppConfigModel.Key>(this::getApplicationPath, "config.json"));
		data.setPropertyProvider(Key.REPORT_PATH, new PathExtensionProvider<AppConfigModel.Key>(this::getApplicationPath, "report"));

		data.setListener(new PropertyListener<AppConfigModel.Key>() {
			@Override
			public void onPropertyChange(PropertyChangedEvent<Key> property) {
				final EventSystem eventSystem = App.getInstance().getEventSystem();
				if (eventSystem == null) {
					return;
				}

				switch (property.key) {
					case APP_PATH:
						eventSystem.postEvent(new AppConfigAppPathChangedEvent((Path) property.oldValue, (Path) property.newValue));
						break;
					case DONT_SAVE_CONFIG:
						break;
					case CONFIG_PATH:
						eventSystem.postEvent(new AppConfigPathChangedEvent((Path) property.oldValue, (Path) property.newValue));
						break;
					case DEBUG_MODE:
						eventSystem.postEvent(new AppConfigDebugModeChangedEvent((Boolean) property.oldValue, (Boolean) property.newValue));
						break;
					case HEADLESS_MODE:
						eventSystem.postEvent(new AppConfigHeadlessModeChangedEvent((Boolean) property.oldValue, (Boolean) property.newValue));
						break;
					case OUTPUT_PATH:
						eventSystem.postEvent(new AppConfigOutputPathChangedEvent((Path) property.oldValue, (Path) property.newValue));
						break;
					case REPORT_PATH:
						break;
					default:
						break;
				}
			}
		});

	}

	public Path getApplicationPath() {
		return data.getProperty(Key.APP_PATH);
	}

	public boolean setApplicationPath(Path value) {
		return data.setProperty(Key.APP_PATH, value);
	}

	public Path getConfigPath() {
		return data.getProperty(Key.CONFIG_PATH);
	}

	protected boolean setConfigPath(Path value) {
		return data.setProperty(Key.CONFIG_PATH, value);
	}

	public boolean getDebugMode() {
		return data.getProperty(Key.DEBUG_MODE);
	}

	public boolean setDebugMode(Boolean value) {
		return data.setProperty(Key.DEBUG_MODE, value);
	}

	protected void setOutputPath(Path value) {
		data.setProperty(Key.OUTPUT_PATH, value);
	}

	public Path getOutputPath() {
		return data.getProperty(Key.OUTPUT_PATH);
	}

	/**
	 * This property will be set at startup and can't be changed afterwards
	 */
	public void setHeadlessMode(boolean value) {
		if (data.isPropertySet(Key.HEADLESS_MODE)) {
			return;
		}
		data.setProperty(Key.HEADLESS_MODE, value);
	}

	public boolean getHeadlessMode() {
		return data.getProperty(Key.HEADLESS_MODE);
	}

	public Path getReportFolder() {
		return data.getProperty(Key.REPORT_PATH);
	}

}
