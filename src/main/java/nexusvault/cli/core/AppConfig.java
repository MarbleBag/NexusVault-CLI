package nexusvault.cli.core;

import java.nio.file.Path;

public final class AppConfig {

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

	AppConfig() {

	}

	private String applicationVersion;
	private Path applicationPath;
	private Path configPath;
	private Path reportPath;
	private Path outputPath;
	private boolean debugMode;
	private boolean headlessMode;

	public Path getApplicationPath() {
		return this.applicationPath;
	}

	public void setApplicationPath(Path value) {
		if (this.applicationPath == null || !this.applicationPath.equals(value)) {
			final var oldValue = this.applicationPath;
			this.applicationPath = value;
			App.getInstance().getEventSystem().postEvent(new AppConfigAppPathChangedEvent(oldValue, value));
		}
	}

	public Path getConfigPath() {
		return this.configPath == null ? getApplicationPath() : this.configPath;
	}

	public void setConfigPath(Path value) {
		if (this.configPath == null || !this.configPath.equals(value)) {
			final var oldValue = this.configPath;
			this.configPath = value;
			App.getInstance().getEventSystem().postEvent(new AppConfigPathChangedEvent(oldValue, value));
		}
	}

	public boolean getDebugMode() {
		return this.debugMode;
	}

	public void setDebugMode(boolean value) {
		if (!this.debugMode != value) {
			final var oldValue = this.debugMode;
			this.debugMode = value;
			App.getInstance().getEventSystem().postEvent(new AppConfigDebugModeChangedEvent(oldValue, value));
		}
	}

	public Path getOutputPath() {
		return this.outputPath == null ? getApplicationPath() : this.outputPath;
	}

	public void setOutputPath(Path value) {
		if (this.outputPath == null || !this.outputPath.equals(value)) {
			final var oldValue = this.outputPath;
			this.outputPath = value;
			App.getInstance().getEventSystem().postEvent(new AppConfigOutputPathChangedEvent(oldValue, value));
		}
	}

	public String getApplicationVersion() {
		return this.applicationVersion;
	}

	public void setApplicationVersion(String value) {
		if (this.applicationVersion == null || !this.applicationVersion.equals(value)) {
			final var oldValue = this.applicationVersion;
			this.applicationVersion = value;
			App.getInstance().getEventSystem().postEvent(new ModelPropertyChangedEvent<>("Application version", oldValue, value));
		}
	}

	public void setConfigNotSaveable(boolean value) {
		// TODO Auto-generated method stub

	}

	public void reloadConfig() {
		// TODO Auto-generated method stub

	}

	public void setHeadlessMode(boolean value) {
		this.headlessMode = value;
	}

	public Path getReportFolder() {
		return this.reportPath == null ? getApplicationPath() : this.reportPath;
	}

}