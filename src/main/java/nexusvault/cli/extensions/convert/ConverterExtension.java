package nexusvault.cli.extensions.convert;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.reflect.Reflection;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.ReflectionHelper;
import nexusvault.cli.core.extension.AbstractExtension;
import nexusvault.cli.core.extension.ExtensionInitializationException;
import nexusvault.cli.extensions.worker.NullStatusMonitor;
import nexusvault.cli.extensions.worker.StatusMonitor;
import nexusvault.cli.extensions.worker.WorkerExtension;

public final class ConverterExtension extends AbstractExtension {

	private final Map<String, ConverterFactory> factories = new HashMap<>();
	private final Map<String, Set<String>> fileExtension2FactoryId = new HashMap<>();
	private final Map<String, String> preferredFactory = new HashMap<>();

	@Override
	protected void initializeExtension(InitializationHelper helper) {
		try {
			loadConverters(helper);
		} catch (final IOException e) {
			throw new ExtensionInitializationException(e);
		}
	}

	@Override
	protected void deinitializeExtension() {
		this.factories.clear();
		this.fileExtension2FactoryId.clear();
	}

	private ConverterFactory initializeFactory(Class<ConverterFactory> factoryClass, InitializationHelper helper) {
		Constructor<ConverterFactory> constructor;

		constructor = ReflectionHelper.findMatchingConstructor(factoryClass, ConverterExtension.class, InitializationHelper.class);
		if (constructor != null) {
			return ReflectionHelper.initialize(constructor, this, helper);
		}

		constructor = ReflectionHelper.findMatchingConstructor(factoryClass, InitializationHelper.class, ConverterExtension.class);
		if (constructor != null) {
			return ReflectionHelper.initialize(constructor, helper, this);
		}

		constructor = ReflectionHelper.findMatchingConstructor(factoryClass, InitializationHelper.class);
		if (constructor != null) {
			return ReflectionHelper.initialize(constructor, helper);
		}

		constructor = ReflectionHelper.findMatchingConstructor(factoryClass, ConverterExtension.class);
		if (constructor != null) {
			return ReflectionHelper.initialize(constructor, this);
		}

		constructor = ReflectionHelper.findMatchingConstructor(factoryClass);
		if (constructor != null) {
			return ReflectionHelper.initialize(constructor);
		}

		return null;
	}

	private void loadConverters(InitializationHelper helper) throws IOException {
		final var factoryClasses = ReflectionHelper.findClasses(ConverterExtension.class.getPackageName(), ConverterFactory.class);

		for (final var factoryClass : factoryClasses) {
			if (!factoryClass.isAnnotationPresent(AutoInstantiate.class)) {
				continue;
			}

			Reflection.initialize(factoryClass);

			final ConverterFactory factory = initializeFactory(factoryClass, helper);
			if (factory == null) {
				// TODO ERROR
				continue;
			}

			final var id = factory.getId();
			if (id == null) {
				throw new ExtensionInitializationException(String.format("Converter '%s' has no id", factory.getClass()));
			}

			final var oldFactory = this.factories.put(id, factory);
			if (oldFactory != null) {
				throw new ExtensionInitializationException(
						String.format("Converter '%s' uses same id '%s' as exporter '%s'.", factory.getClass(), id, oldFactory.getClass()));
			}

			for (final var fileExtension : factory.getAcceptedFileExtensions()) {
				final var supportedBy = this.fileExtension2FactoryId.computeIfAbsent(fileExtension, k -> new HashSet<>());
				supportedBy.add(id);
			}
		}
	}

	public Set<String> getSupportedFileExtensions() {
		return Collections.unmodifiableSet(this.fileExtension2FactoryId.keySet());
	}

	public Set<String> getConvertersForFileExtensions(String extension) {
		final var converters = this.fileExtension2FactoryId.get(extension);
		if (converters != null) {
			return Collections.unmodifiableSet(converters);
		}
		return Collections.emptySet();
	}

	public String getPreferredConverterForFileExtension(String extension) {
		var id = this.preferredFactory.get(extension);
		if (id == null) {
			final var ids = this.fileExtension2FactoryId.get(extension);
			id = ids.stream().findAny().orElse(null);
			if (id != null) {
				this.preferredFactory.put(extension, id);
			}
		}
		return id;
	}

	public void setPreferredConverterForFileExtension(String extension, String id) {
		final var factory = this.factories.get(id);
		if (factory == null) {
			throw new NoConverterFoundException(String.format("No converter found for id '%s'", id));
		}
		if (!factory.getAcceptedFileExtensions().contains(extension)) {
			throw new ConverterException(String.format("Converter '%s' does not support file extension '%s'", id, extension));
		}
		this.preferredFactory.put(extension, id);
	}

	public ConverterFactory getConverter(String id) {
		return this.factories.get(id);
	}

	public ConversionResult convert(ConversionRequest request, ConverterOptions options) {
		return convert(Collections.singletonList(request), options, new NullStatusMonitor()).get(0);
	}

	public List<ConversionResult> convert(Collection<ConversionRequest> requests, ConverterOptions options, StatusMonitor callback) {
		final var tasks = new ArrayList<Runnable>(requests.size());
		final var converterByExtension = new HashMap<String, Converter>();
		final var conversionResult = new ConversionResult[requests.size()];
		final var rootOutputDir = App.getInstance().getAppConfig().getOutputPath();

		{
			var counter = -1;
			for (final var request : requests) {
				final var index = ++counter;
				conversionResult[index] = new ConversionResult(request);

				final var converter = converterByExtension.computeIfAbsent(request.input.getFileExtension(), extension -> {
					final var factoryId = getPreferredConverterForFileExtension(extension);
					return factoryId != null ? getConverter(factoryId).createConverter(options) : null;
				});

				if (converter == null) {
					conversionResult[index].setError(new NoConverterFoundException());
					continue;
				}

				var outputDir = request.outputDir;
				if (outputDir != null && outputDir.isAbsolute()) {
					// pass
				} else {
					if (outputDir == null) {
						outputDir = rootOutputDir.resolve("convert");
					} else {
						outputDir = rootOutputDir.resolve(outputDir);
					}

					final var inputDir = request.input.getDirectory();
					if (!inputDir.isAbsolute()) {
						outputDir = outputDir.resolve(inputDir);
					} else {
						outputDir = outputDir.resolve(inputDir.getFileName());
					}
				}

				final var input = new ConversionManager(request.input, outputDir);

				tasks.add(() -> {
					try {
						converter.convert(input);
						conversionResult[index].setOutput(input.getCreatedFiles());
					} catch (final Exception e) {
						conversionResult[index].setError(e);
					}
				});
			}
		}

		final var statusMonitor = callback != null ? callback : new DefaultStatusMonitor();
		App.getInstance().getExtension(WorkerExtension.class).waitForWork(tasks, statusMonitor);
		return Arrays.asList(conversionResult);
	}

}
