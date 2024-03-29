package nexusvault.cli.extensions.export;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.PathUtil;
import nexusvault.cli.core.extension.AbstractExtension;
import nexusvault.cli.core.extension.ExtensionInitializationException;
import nexusvault.cli.extensions.archive.ArchiveExtension;
import nexusvault.cli.extensions.convert.ConversionRequest;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterExtension;
import nexusvault.cli.extensions.convert.resource.ArchiveResource;
import nexusvault.cli.extensions.worker.StatusMonitor;
import nexusvault.cli.extensions.worker.WorkerExtension;
import nexusvault.vault.IdxPath;

@AutoInstantiate
public final class ExportExtension extends AbstractExtension {

	private static final Logger logger = LogManager.getLogger(ExportExtension.class);

	private ExecutorService threadPool;

	@Override
	protected void initializeExtension(InitializationHelper helper) throws ExtensionInitializationException {
		this.threadPool = Executors.newWorkStealingPool();
	}

	@Override
	protected void deinitializeExtension() {
		this.threadPool.shutdown();
	}

	public void exportAsBinary(List<IdxPath> searchResults, boolean separateExports) {
		export(searchResults, true, separateExports, null, null);
	}

	public void exportViaConverters(List<IdxPath> searchResults, boolean separateExports, Map<String, String> defaultConvertersForExt, ConverterArgs args) {
		export(searchResults, false, separateExports, defaultConvertersForExt, args);
	}

	private void export(List<IdxPath> searchResults, boolean asBinary, boolean separateExports, Map<String, String> defaultConvertersForExt,
			ConverterArgs args) {
		final var archiveExtension = App.getInstance().getExtension(ArchiveExtension.class);
		final var archives = archiveExtension.getArchives();
		if (archives.isEmpty()) {
			sendMsg("No vaults are loaded. Use 'help' to learn how to load them");
			return;
		}

		final var outputPath = Path.of("export");

		final var exportableFiles = searchResults.parallelStream().map(p -> {
			for (final var archive : archives) {
				final var optionalEntry = archive.find(p);
				if (optionalEntry.isPresent()) {
					return optionalEntry.get();
				}
			}
			// TODO log error
			return null;
		}).filter(p -> p != null & p.isFile()).map(p -> p.asFile()).map(ArchiveResource::new).map(e -> {
			var target = outputPath.resolve(e.getFilePath().resolveSibling(""));
			if (separateExports) {
				target = target.resolve(PathUtil.getFileName(e.getFile()));
			}
			return new ConversionRequest(e, target);
		});

		if (asBinary) {
			exportAsBinaries(exportableFiles);
		} else {
			exportAndConvert(exportableFiles, defaultConvertersForExt, args);
		}
	}

	private final class ExportStatusMonitor implements StatusMonitor {

		private long startAt;
		private long endAt;

		@Override
		public void start() {
			this.startAt = System.currentTimeMillis();
			sendMsg(String.format("Start export to: %s", App.getInstance().getAppConfig().getOutputPath().resolve("export")));
		}

		@Override
		public void processed(int completedTasks, int totalNumberOfTasks) {
			final float percentage = completedTasks / (totalNumberOfTasks + 0f) * 100;
			final String msg = String.format("Processed files %d of %d (%.2f%%).", completedTasks, totalNumberOfTasks, percentage);
			sendMsg(msg);
		}

		@Override
		public void end() {
			this.endAt = System.currentTimeMillis();
			sendMsg(String.format("Export done in %.2fs", (this.endAt - this.startAt) / 1000f));
		}
	}

	private void exportAndConvert(final Stream<ConversionRequest> exportableFiles, Map<String, String> defaultConvertersForExt, ConverterArgs args) {
		args = args == null ? new ConverterArgs() : args;

		final var converterExtension = App.getInstance().getExtension(ConverterExtension.class);
		final var convertFiles = exportableFiles.collect(Collectors.toList());

		final var converters = new HashMap<String, Converter>();
		for (final var entry : converterExtension.getConverterFactories(convertFiles).entrySet()) {
			final var id = defaultConvertersForExt.getOrDefault(entry.getKey(), entry.getValue());
			final var factory = converterExtension.createConverterFactory(id);
			factory.applyArguments(args);
			converters.put(entry.getKey(), factory.createConverter());
		}

		try {
			final var results = converterExtension.convert(convertFiles, converters, new ExportStatusMonitor());
			final var errors = results.stream()
					.map(result -> result.isFailed() ? new ExportError(result.getRequest().input.getFile(), result.getError()) : null).filter(Objects::nonNull)
					.collect(Collectors.toList());
			sendError(errors);
		} finally {
			converters.forEach((k, v) -> v.deinitialize());
		}
	}

	private void exportAsBinaries(final Stream<ConversionRequest> exportableFiles) {
		final var outputFolder = App.getInstance().getAppConfig().getOutputPath();
		final var errors = new ConcurrentLinkedQueue<ExportError>();

		final var tasks = exportableFiles.map(file -> (Runnable) () -> {
			try {
				final var filePath = outputFolder.resolve(file.outputDir).resolve(file.input.getFile());
				Files.createDirectories(filePath.getParent());
				try (var channel = Files.newByteChannel(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
					final var data = ByteBuffer.wrap(file.input.getData());
					while (data.hasRemaining()) {
						channel.write(data);
					}
				}
			} catch (final Throwable e) {
				errors.add(new ExportError(file.input.getFilePath(), e));
			}
		}).collect(Collectors.toList());

		App.getInstance().getExtension(WorkerExtension.class).waitForWork(tasks, new ExportStatusMonitor());

		sendError(errors);
	}

	private void sendError(Collection<ExportError> exportErrors) {
		if (!exportErrors.isEmpty()) {
			sendMsg(() -> {
				final StringBuilder msg = new StringBuilder();
				msg.append(String.format("Unable to export %d file(s)\n", exportErrors.size()));
				msg.append("Error(s)\n");
				for (final var error : exportErrors) {
					msg.append(error.getFile()).append("\n");
					msg.append("->").append(error.getError().getClass());
					msg.append(" : ").append(error.getError().getMessage());
					msg.append("\n");

					var cause = error.getError().getCause();
					while (cause != null) {
						msg.append("\t\t->").append(cause.getClass()).append(" : ").append(cause.getMessage()).append("\n");
						cause = cause.getCause();
					}
				}
				return msg.toString();
			});

			logger.error(String.format("Unable to export %d file(s)\n", exportErrors.size()));
			for (final var error : exportErrors) {
				logger.error(error.getFile(), error.getError());
			}
		}
	}

}
