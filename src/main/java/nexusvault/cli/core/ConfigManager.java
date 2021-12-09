package nexusvault.cli.core;

import nexusvault.cli.ModelSystem.ModelNotFoundException;

public interface ConfigManager {

	Config loadConfig(String configId) throws ModelNotFoundException;

}
