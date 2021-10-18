package nexusvault.cli.core;

import java.nio.file.Path;

public final class AppConfig {
	AppConfig() {

	}

	public Path getApplicationPath() {
		return null;
	}

	public Path getConfigPath() {
		return null;
	}

	public boolean getDebugMode() {
		return null;
	}

	public Path getOutputPath() {
		return null;
	}

	public String getApplicationVersion() {
		return this.data.getProperty(Key.APP_VERSION);
	}
}
