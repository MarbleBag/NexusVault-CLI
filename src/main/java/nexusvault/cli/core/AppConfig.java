package nexusvault.cli.core;

import java.nio.file.Path;

import nexusvault.cli.model.ModelPropertyChangedEvent;

public final class AppConfig {
	AppConfig() {

	}

	private Path applicationPath;
	private Path configPath;
	private boolean debugMode;
	private Path outputPath;
	private String applicationVersion;

	public Path getApplicationPath() {
		return this.applicationPath;
	}

	public void setApplicationPath(Path value) {
		if (!this.applicationPath.equals(value)) {
			var oldValue = this.applicationPath;
			this.applicationPath = value;
			App.getInstance().getEventSystem().postEvent(new ModelPropertyChangedEvent<>("Application path", oldValue = value, value));
		}
	}

	public Path getConfigPath() {
		return this.configPath;
	}

	public void setConfigPath(Path value) {
		if (!this.configPath.equals(value)) {
			final var oldValue = this.configPath;
			this.configPath = value;
			App.getInstance().getEventSystem().postEvent(new ModelPropertyChangedEvent<>("Application config path", oldValue, value));
		}
	}

	public boolean getDebugMode() {
		return this.debugMode;
	}

	public Path getOutputPath() {
		return this.outputPath;
	}

	public void setOutputPath(Path value) {
		if (!this.outputPath.equals(value)) {
			final var oldValue = this.outputPath;
			this.outputPath = value;
			App.getInstance().getEventSystem().postEvent(new ModelPropertyChangedEvent<>("Application output path", oldValue, value));
		}
	}

	public String getApplicationVersion() {
		return this.applicationVersion;
	}

}