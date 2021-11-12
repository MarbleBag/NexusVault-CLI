package nexusvault.cli.extensions.export;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nexusvault.archive.IdxFileLink;
import nexusvault.archive.IdxPath;
import nexusvault.cli.core.App;
import nexusvault.cli.core.extension.AbstractExtension;
import nexusvault.cli.core.extension.ExtensionInitializationException;
import nexusvault.cli.extensions.archive.ArchiveExtension;
import nexusvault.cli.extensions.convert.ConversionRequest;
import nexusvault.cli.extensions.convert.ConverterExtension;
import nexusvault.cli.extensions.convert.ConverterOptions;
import nexusvault.cli.extensions.convert.resource.ArchiveResource;
import nexusvault.cli.extensions.worker.StatusMonitor;
import nexusvault.cli.extensions.worker.WorkerExtension;

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

	public void export(List<IdxPath> searchResults, boolean exportAsBinary) {
		final var archiveExtension = App.getInstance().getExtension(ArchiveExtension.class);
		final var archiveContainers = archiveExtension.getArchives();
		if (archiveContainers.isEmpty()) {
			sendMsg("No vaults are loaded. Use 'help' to learn how to load them");
			return;
		}

		final var exportableFiles = searchResults.parallelStream().map(p -> {
			for (final var container : archiveContainers) {
				final var optionalEntry = p.tryToResolve(container.getArchive().getRootDirectory());
				if (optionalEntry.isPresent()) {
					return optionalEntry.get();
				}
			}
			// TODO log error
			return null;
		}).filter(p -> p != null & p.isFile()).map(p -> p.asFile());

		if (exportAsBinary) {
			exportAsBinaries(exportableFiles);
		} else {
			exportAndConvert(exportableFiles);
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

	protected void exportAndConvert(final Stream<IdxFileLink> exportableFiles) {
		final var options = new ConverterOptions();
		final var outputPath = Paths.get("export");
		final var convertFiles = exportableFiles.map(ArchiveResource::new).map(e -> new ConversionRequest(e, outputPath)).collect(Collectors.toList());

		final var converterExtension = App.getInstance().getExtension(ConverterExtension.class);
		final var results = converterExtension.convert(convertFiles, options, new ExportStatusMonitor());

		final var errors = results.stream().map(result -> result.isFailed() ? new ExportError(result.getRequest().input.getFile(), result.getError()) : null)
				.filter(Objects::nonNull).collect(Collectors.toList());

		sendError(errors);
	}

	protected void exportAsBinaries(final Stream<IdxFileLink> exportableFiles) {
		final var outputFolder = App.getInstance().getAppConfig().getOutputPath().resolve("binaries");
		final var errors = new ConcurrentLinkedQueue<ExportError>();

		final var tasks = exportableFiles.map(file -> (Runnable) () -> {
			try {
				final var filePath = outputFolder.resolve(Path.of(file.getFullName()));
				Files.createDirectories(filePath.getParent());
				try (var channel = Files.newByteChannel(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
					final var data = file.getData();
					while (data.hasRemaining()) {
						channel.write(data);
					}
				}
			} catch (final Exception e) {
				errors.add(new ExportError(file, e));
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
