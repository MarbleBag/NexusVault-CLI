package nexusvault.cli.extensions.convert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import nexusvault.cli.core.App;
import nexusvault.cli.core.Console.Level;
import nexusvault.cli.core.PathUtil;
import nexusvault.cli.core.ReflectionHelper;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;
import nexusvault.cli.core.extension.AbstractExtension;
import nexusvault.cli.core.extension.ExtensionInitializationException;
import nexusvault.cli.extensions.convert.FactoryBuilder.FactoryContainer;
import nexusvault.cli.extensions.worker.NullStatusMonitor;
import nexusvault.cli.extensions.worker.StatusMonitor;
import nexusvault.cli.extensions.worker.WorkerExtension;

public final class ConverterExtension extends AbstractExtension {

	private final Map<String, FactoryContainer> factoryContainers = new HashMap<>();
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
		this.factoryContainers.clear();
		this.fileExtension2FactoryId.clear();
	}

	private void loadConverters(InitializationHelper helper) throws IOException {
		this.factoryContainers.putAll(FactoryBuilder.findAllFactories());
		this.fileExtension2FactoryId.putAll(FactoryBuilder.computeFactoriesByExtension(this.factoryContainers));
		this.preferredFactory.putAll(FactoryBuilder.computePreferredFactoryByExtension(this.factoryContainers, this.fileExtension2FactoryId));

		helper.addCommandHandler(new CommandHandler() {

			@Override
			public String onHelp() {
				return null;
			}

			@Override
			public void onCommand(Arguments args) {
				App.getInstance().getConsole().println(Level.CONSOLE, "Use as 'convert-help ?'");
			}

			@Override
			public CommandDescription getCommandDescription() {
				final var cmdBuilder = new CommandDescription.CompactBuilder();
				cmdBuilder.setCommandName("convert-help");
				cmdBuilder.setDescription("Lists all arguments which can be used for conversions.");
				cmdBuilder.ignoreUnnamedArguments(true);

				for (final var container : ConverterExtension.this.factoryContainers.values()) {
					final var args = container.args;
					for (final var arg : args) {
						cmdBuilder.addNamedArgument(arg);
					}
				}

				return cmdBuilder.build();
			}
		});
	}

	public Set<String> getSupportedFileExtensions() {
		return Collections.unmodifiableSet(this.fileExtension2FactoryId.keySet());
	}

	public Set<String> getConverterIdsForFileExtensions(String extension) {
		final var converters = this.fileExtension2FactoryId.get(extension);
		if (converters != null) {
			return Collections.unmodifiableSet(converters);
		}
		return Collections.emptySet();
	}

	public void setPreferredConverterIdForFileExtension(String fileExtension, String id) {
		if (id == null) {
			this.preferredFactory.remove(fileExtension);
			return;
		}

		final var container = this.factoryContainers.get(id);
		if (container == null) {
			throw new NoConverterFoundException(String.format("No converter found for id '%s'", id));
		}
		if (!container.extensions.contains(fileExtension)) {
			throw new ConverterException(String.format("Converter '%s' does not support file extension '%s'", id, fileExtension));
		}
		this.preferredFactory.put(fileExtension, id);
	}

	public String getConverterIdForFileExtension(String extension) {
		return this.preferredFactory.computeIfAbsent(extension,
				ext -> FactoryBuilder.findPreferredFactoryByExtension(this.factoryContainers, this.fileExtension2FactoryId, ext));
	}

	public Map<String, String> getConverterFactories(Set<String> extensions) {
		final var factoriesByExtension = new HashMap<String, String>();
		for (final var extension : extensions) {
			factoriesByExtension.computeIfAbsent(extension, ext -> {
				return getConverterIdForFileExtension(ext);
			});
		}
		return factoriesByExtension;
	}

	public Map<String, String> getConverterFactories(Collection<ConversionRequest> requests) {
		final var extensions = requests.parallelStream().map(e -> e.input.getFileExtension()).collect(Collectors.toSet());
		return getConverterFactories(extensions);
	}

	public ConverterFactory createConverterFactory(String id) {
		final var container = this.factoryContainers.get(id);
		if (container == null) {
			throw new NoConverterFoundException(String.format("No converter found for id '%s'", id));
		}

		final var constructor = ReflectionHelper.findMatchingConstructor(container.factoryClass);
		return ReflectionHelper.initialize(constructor);
	}

	public Map<String, ConverterFactory> createConverterFactory(Map<String, String> factories) {
		final var factoriesByExtension = new HashMap<String, ConverterFactory>();
		for (final var entry : factories.entrySet()) {
			factoriesByExtension.put(entry.getKey(), createConverterFactory(entry.getValue()));
		}
		return factoriesByExtension;
	}

	public ConversionResult convert(ConversionRequest request, ConverterArgs options) {
		return convert(Collections.singletonList(request), options, new NullStatusMonitor()).get(0);
	}

	public void applyConvertersArgs(Map<String, ConverterFactory> factories, ConverterArgs options) {
		applyConvertersArgs(factories.values(), options);
	}

	public void applyConvertersArgs(Collection<ConverterFactory> factories, ConverterArgs options) {
		for (final var factory : factories) {
			factory.applyArguments(options);
		}
	}

	public Map<String, Converter> createConverters(Map<String, ConverterFactory> factories) {
		final var convertersByExtension = new HashMap<String, Converter>();
		for (final var entry : factories.entrySet()) {
			convertersByExtension.put(entry.getKey(), entry.getValue().createConverter());
		}
		return convertersByExtension;
	}

	public List<ConversionResult> convert(Collection<ConversionRequest> requests, Map<String, Converter> converterByExtension, StatusMonitor callback) {
		final var tasks = new ArrayList<Runnable>(requests.size());
		final var conversionResult = new ConversionResult[requests.size()];
		final var rootOutputDir = App.getInstance().getAppConfig().getOutputPath();

		var counter = -1;
		for (final var request : requests) {
			final var index = ++counter;
			conversionResult[index] = new ConversionResult(request);

			final var converter = converterByExtension.get(request.input.getFileExtension());
			if (converter == null) {
				conversionResult[index].setError(new NoConverterFoundException());
				continue;
			}

			final var outputDir = makeOutputDir(rootOutputDir, request);
			final var input = new ConversionManager(request.input, outputDir);

			tasks.add(() -> {
				try {
					Files.createDirectories(input.getOutputPath());
					converter.convert(input);
					conversionResult[index].setOutput(input.getCreatedFiles());
				} catch (final Exception e) {
					conversionResult[index].setError(e);
				}
			});
		}

		final var statusMonitor = callback != null ? callback : new DefaultStatusMonitor();
		App.getInstance().getExtension(WorkerExtension.class).waitForWork(tasks, statusMonitor);
		return Arrays.asList(conversionResult);
	}

	public List<ConversionResult> convert(Collection<ConversionRequest> requests, ConverterArgs options, StatusMonitor callback) {
		final var factoryIds = getConverterFactories(requests);
		final var factories = createConverterFactory(factoryIds);
		applyConvertersArgs(factories, options);
		final var converters = createConverters(factories);
		return convert(requests, converters, callback);
	}

	private Path makeOutputDir(final Path rootOutputDir, final ConversionRequest request) {
		var outputDir = request.outputDir;
		if (outputDir != null && outputDir.isAbsolute()) {
			// pass
		} else {
			if (outputDir == null) {
				outputDir = rootOutputDir.resolve("convert");
			} else {
				outputDir = rootOutputDir.resolve(outputDir);
			}

			final var inputDir = request.input.getDirectory().resolve(PathUtil.getFileName(request.input.getFile()));
			if (!inputDir.isAbsolute()) {
				outputDir = outputDir.resolve(inputDir);
			} else {
				outputDir = outputDir.resolve(inputDir.getFileName());
			}
		}
		return outputDir;
	}
}
