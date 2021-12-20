package nexusvault.cli.extensions.convert;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nexusvault.cli.core.ReflectionHelper;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.extension.ExtensionInitializationException;

final class FactoryBuilder {

	public static final class FactoryContainer {
		public final String id;
		public final int priority;
		public final Set<String> extensions;
		public final Class<? extends ConverterFactory> factoryClass;
		public final List<ArgumentDescription> args;

		public FactoryContainer(String id, int priority, String[] extensions, Class<ConverterFactory> factoryClass, List<ArgumentDescription> args) {
			if (id == null || id.isBlank()) {
				throw new ExtensionInitializationException(String.format("Converter '%s' has no id", factoryClass));
			}
			this.id = id;
			this.priority = priority;
			this.extensions = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(extensions)));
			this.factoryClass = factoryClass;
			this.args = Collections.unmodifiableList(args);
		}

		public int getPriority() {
			return this.priority;
		}

		public String getId() {
			return this.id;
		}
	}

	public static Map<String, Set<String>> computeFactoriesByExtension(Map<String, FactoryContainer> factories) {
		final var extensionToFactory = new HashMap<String, Set<String>>();
		for (final var container : factories.values()) {
			for (final var fileExtension : container.extensions) {
				extensionToFactory.computeIfAbsent(fileExtension, k -> new HashSet<>()).add(container.id);
			}
		}
		return extensionToFactory;
	}

	public static Map<String, String> computePreferredFactoryByExtension(Map<String, FactoryContainer> factories, Map<String, Set<String>> extensionMapping) {
		final var extensionToPrefFactory = new HashMap<String, String>();
		for (final var extension : extensionMapping.keySet()) {
			extensionToPrefFactory.computeIfAbsent(extension, ext -> findPreferredFactoryByExtension(factories, extensionMapping, ext));
		}
		return extensionToPrefFactory;
	}

	public static String findPreferredFactoryByExtension(Map<String, FactoryContainer> factories, Map<String, Set<String>> extensionMapping, String extension) {
		final var ids = extensionMapping.get(extension);
		if (ids == null || ids.isEmpty()) {
			return null;
		}
		return ids.stream().map(factories::get).sorted((f1, f2) -> f2.getPriority() - f1.getPriority()).map(FactoryContainer::getId).findFirst().orElse(null);
	}

	public static Map<String, FactoryContainer> findAllFactories() throws IOException {
		final var containerById = new HashMap<String, FactoryContainer>();

		final var factoryClasses = ReflectionHelper.findClasses(ConverterExtension.class.getPackageName(), ConverterFactory.class);

		for (final var factoryClass : factoryClasses) {
			final var annotation = factoryClass.getAnnotation(IsFactory.class);
			if (annotation == null) {
				continue;
			}

			final var id = annotation.id();
			final var priority = annotation.priority();
			final var extensions = annotation.fileExtensions();
			final var args = fetchArgs(factoryClass);
			final var container = new FactoryContainer(id, priority, extensions, factoryClass, args);

			final var oldContainer = containerById.put(container.id, container);
			if (oldContainer != null) {
				throw new ExtensionInitializationException(
						String.format("Converter '%s' uses same id '%s' as converter '%s'.", container.factoryClass, id, oldContainer.factoryClass));
			}
		}

		return containerById;
	}

	// -----------------------------------------------------------------

	private static List<ArgumentDescription> fetchArgs(Class<?> factoryClass) {
		final var args = new LinkedList<ArgumentDescription>();
		for (final var method : factoryClass.getMethods()) {
			final var argumentAnnotation = method.getAnnotation(IsArgument.class);
			if (argumentAnnotation == null) {
				continue;
			}

			final var argumentBuilder = new ArgumentDescription.CompactBuilder();
			argumentBuilder.setName(getArgumentName(method, argumentAnnotation));
			argumentBuilder.setDescription(argumentAnnotation.description());
			argumentBuilder.setRequired(false);
			argumentBuilder.isArgumentOptional(argumentAnnotation.isArgumentOptional());

			if (argumentAnnotation.numberOfArgs() != Integer.MAX_VALUE) {
				argumentBuilder.setNumberOfArguments(argumentAnnotation.numberOfArgs());
			} else {
				argumentBuilder.setNumberOfArgumentsUnlimited();
			}

			args.add(argumentBuilder.build());
		}

		return args.isEmpty() ? Collections.emptyList() : args;
	}

	private static String getArgumentName(Method method, IsArgument argumentAnnotation) {
		if (!argumentAnnotation.name().isBlank()) {
			return argumentAnnotation.name();
		}

		final var name = method.getName();
		return name; // TODO remove set, add hyphens, etc
	}
}
