package nexusvault.cli.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nexusvault.cli.core.Console.Level;
import nexusvault.cli.core.exception.AppInitializationException;
import nexusvault.cli.core.extension.Extension;
import nexusvault.cli.core.extension.ExtensionInfo;

final class BaseExtensionManager implements ExtensionManager {

	private final static Logger logger = LogManager.getLogger(BaseExtensionManager.class);

	private final Map<Class<? extends Extension>, Extension> extensions;
	private final App app;

	public BaseExtensionManager(App app) {
		this.extensions = new HashMap<>();
		this.app = app;
	}

	public void loadExtensions(String classPath) {
		try {
			final List<Class<Extension>> extensionClasses = ReflectionHelper.findClasses(classPath, Extension.class);

			extensionClasses.sort(Collections.reverseOrder((a, b) -> {
				final var annotationA = a.getAnnotation(ExtensionInfo.class);
				final var annotationB = b.getAnnotation(ExtensionInfo.class);

				final var priorityA = annotationA != null ? annotationA.priority() : Integer.MIN_VALUE;
				final var priorityB = annotationB != null ? annotationB.priority() : Integer.MIN_VALUE;
				if (priorityA < priorityB) {
					return -1;
				}
				if (priorityA > priorityB) {
					return 1;
				}
				return 0;
			}));

			for (final var extensionClass : extensionClasses) {
				if (!extensionClass.isAnnotationPresent(AutoInstantiate.class)) {
					continue;
				}

				try {
					final var extension = ReflectionHelper.initialize(extensionClass);
					this.register(extension);
					extension.initialize(this.app);
				} catch (final Exception e) {
					logger.error(String.format("Unable to load extension: '%s'", extensionClass), e);
					final var writer = this.app.getConsole().getWriter(Level.CONSOLE);
					writer.format("Unable to load extension: '%s'", extensionClass);
					e.printStackTrace(writer);
					writer.flush();
				}
			}

			this.app.getConsole().println(Level.DEBUG, String.format("Plugin: %d plugin(s) found.", extensionClasses.size()));
		} catch (final Exception e) {
			throw new AppInitializationException(e);
		}
	}

	@Override
	public <T extends Extension> void register(T extension) {
		if (extension == null) {
			throw new IllegalArgumentException("'extension' must not be null");
		}

		final var clazz = extension.getClass();
		if (this.extensions.containsKey(clazz)) {
			throw new IllegalStateException(String.format("Extension of type '%s' iss already registered.", clazz));
		}

		this.extensions.put(clazz, extension);
	}

	@Override
	public <T extends Extension> boolean unregister(T extension) {
		if (extension == null) {
			throw new IllegalArgumentException("'extension' must not be null");
		}
		final var clazz = extension.getClass();
		return this.extensions.remove(clazz) != null;
	}

	@Override
	public <T extends Extension> T getExtension(Class<T> extensionClass) {
		if (extensionClass == null) {
			throw new IllegalArgumentException("'extensionClass' must not be null");
		}

		// TODO will do for now
		final T extension = (T) this.extensions.get(extensionClass);
		if (extension == null) {
			throw new IllegalStateException("meh");
			// TODO throw exception
		}
		return extension;
	}

	@Override
	public boolean hasPlugin(Class<? extends Extension> extensionClass) {
		if (extensionClass == null) {
			throw new IllegalArgumentException("'extensionClass' must not be null");
		}
		return this.extensions.containsKey(extensionClass);
	}

}
