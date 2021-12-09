package nexusvault.cli.core.extension;

import nexusvault.cli.core.App;

public interface Extension {

	void initialize(App app) throws ExtensionInitializationException;

	void deinitialize();

}
