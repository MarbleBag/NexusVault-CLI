package nexusvault.cli.core;

import nexusvault.cli.core.extension.Extension;

public interface ExtensionManager {

	<T extends Extension> void register(T plugin);

	<T extends Extension> boolean unregister(T plugin);

	<T extends Extension> T getExtension(Class<T> pluginClass);

	boolean hasPlugin(Class<? extends Extension> pluginClass);

}