package nexusvault.cli.extensions.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nexusvault.cli.core.ReflectionHelper;
import nexusvault.cli.core.extension.AbstractExtension;
import nexusvault.cli.core.extension.ExtensionInitializationException;

public final class ExportExtension extends AbstractExtension {

	private final Map<String, Exporter> exporters = new HashMap<>();
	private final Map<String, String> preferredExporters = new HashMap<>();
	private final Map<String, List<String>> fileExtension2Exporter = new HashMap<>();

	private ExecutorService threadPool;

	@Override
	protected void initializeExtension(InitializationHelper initializationHelper) throws ExtensionInitializationException {
		try {
			loadExporters();
		} catch (final Exception e) {
			throw new ExtensionInitializationException(e);
		}

		// TODO load config

		this.threadPool = Executors.newWorkStealingPool();
	}

	private void loadExporters() throws IOException {
		final var exporterClasses = ReflectionHelper.findClasses(ExportExtension.class.getPackageName(), Exporter.class);
		final var exporters = ReflectionHelper.initialize(exporterClasses);
		for (final var exporter : exporters) {
			exporter.loaded(this);
		}

		// TODO define default exporters, etc

		for (final var exporter : exporters) {
			final var exporterId = exporter.getId();
			if (exporterId == null) {
				throw new ExtensionInitializationException(String.format("Exporter '%s' has no id", exporter.getClass()));
			}

			final var oldExporter = this.exporters.put(exporter.getId(), exporter);
			if (oldExporter != null) {
				// TODO ERROR
			}

			final var fileExtensions = exporter.getAcceptedFileEndings();
			for (final var fileExtension : fileExtensions) {
				final var supportedBy = this.fileExtension2Exporter.computeIfAbsent(fileExtension, k -> new ArrayList<>());
				supportedBy.add(exporterId);
			}
		}
	}

	@Override
	protected void deinitializeExtension() {
		// TODO save config

		for (final var exporter : this.exporters.values()) {
			exporter.unload();
		}

		this.exporters.clear();
		this.preferredExporters.clear();
		this.fileExtension2Exporter.clear();
		this.threadPool.shutdown();
	}

}
