package nexusvault.cli.plugin.export;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import kreed.io.util.ByteBufferBinaryReader;
import nexusvault.archive.IdxFileLink;
import nexusvault.archive.util.DataHeader;
import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.plugin.AbstPlugIn;
import nexusvault.cli.plugin.archive.ArchivePlugIn;
import nexusvault.cli.plugin.archive.SourcedVaultReader;
import nexusvault.cli.plugin.search.SearchPlugIn;

public final class ExportPlugIn extends AbstPlugIn {

	private static final class ExportError {
		public final String path;
		public final IdxFileLink link;
		public final Exception error;

		public ExportError(IdxFileLink link, Exception error) {
			super();
			this.path = null;
			this.link = link;
			this.error = error;
		}

		public ExportError(String path, Exception error) {
			super();
			this.path = path;
			this.link = null;
			this.error = error;
		}

		public String getTarget() {
			if (link == null) {
				return path;
			}
			return link.fullName();
		}
	}

	public static final class ExportRequest {

		public void exportAsBinary(boolean b) {
			// TODO Auto-generated method stub

		}

	}

	private List<Exporter> exporters;

	public ExportPlugIn() {
		final List<Command> cmds = new ArrayList<>();
		cmds.add(new ExportCmd());
		setCommands(cmds);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (exporters == null) {
			exporters = new ArrayList<>();
			loadExporters(exporters);
			exporters = Collections.unmodifiableList(exporters);
			for (final Exporter exporter : exporters) {
				exporter.initialize();
			}
		}
	}

	@Override
	public void deinitialize() {
		super.deinitialize();
		if (exporters != null) {
			for (final Exporter exporter : exporters) {
				exporter.deinitialize();
			}
		}
		exporters = null;
	}

	private void loadExporters(List<Exporter> exporters) {
		exporters.add(new ModelExporter());
		exporters.add(new TextureExporter());
		exporters.add(new TBL2CSVExporter());
		exporters.add(new FontExporter());
		exporters.add(new TextExporter());
		exporters.add(new LocaleExporter());
	}

	public void export(ExportRequest request) {
		final ArchivePlugIn archivePlugIn = App.getInstance().getPlugIn(ArchivePlugIn.class);
		final List<SourcedVaultReader> vaults = archivePlugIn.loadArchives();
		if (vaults.isEmpty()) {
			sendMsg("No vaults are loaded. Use 'help' to learn how to load them");
			return;
		}

		final Map<Path, Set<IdxFileLink>> bla = App.getInstance().getPlugIn(SearchPlugIn.class).getLastSearchResults();

		final Map<SourcedVaultReader, Set<IdxFileLink>> exportFiles = prepareExport(bla, vaults);
		final List<ExportError> exportErrors = new LinkedList<>();
		export(exportFiles, exportErrors);

		if (!exportErrors.isEmpty()) {
			sendMsg(() -> String.format("Unable to export %d files", exportErrors.size()));
			sendMsg(() -> {
				final StringBuilder b = new StringBuilder();
				for (final ExportError error : exportErrors) {
					b.append(error.error.getClass()).append(" in ").append(error.link.fullName()).append("\n");
					b.append("Msg: ").append(error.error.getMessage()).append("\n");
				}
				return b.toString();
			});
		}
		// TODO error handling
	}

	private Map<SourcedVaultReader, Set<IdxFileLink>> prepareExport(final Map<Path, Set<IdxFileLink>> bla, final List<SourcedVaultReader> vaults) {
		final Map<SourcedVaultReader, Set<IdxFileLink>> exportFiles = new HashMap<>(vaults.size());
		for (final SourcedVaultReader vault : vaults) {
			final Set<IdxFileLink> files = bla.get(vault.getSource());
			if (files == null) {
				// TODO
			} else if (!files.isEmpty()) {
				exportFiles.put(vault, files);
			}
		}
		return exportFiles;
	}

	private void export(final Map<SourcedVaultReader, Set<IdxFileLink>> exportFiles, final List<ExportError> exportError) {

		final int numberOfFilesToUnpack = exportFiles.values().stream().mapToInt(set -> set.size()).sum();
		final int reportAfterNFiles = Math.max(1, Math.min(1000, numberOfFilesToUnpack / 20));

		sendMsg(() -> "Start exporting to: " + getOutputFolder());
		int seenFiles = 0;
		int reportIn = 0;
		final long exportAllStart = System.currentTimeMillis();
		for (final Entry<SourcedVaultReader, Set<IdxFileLink>> entry : exportFiles.entrySet()) {
			if (entry.getValue().isEmpty()) {
				continue;
			}

			final SourcedVaultReader vault = entry.getKey();
			for (final IdxFileLink fileLink : entry.getValue()) {

				export(vault, fileLink, exportError);

				seenFiles += 1;
				reportIn += 1;
				if (reportIn >= reportAfterNFiles) {
					final float percentage = (seenFiles / (numberOfFilesToUnpack + 0f)) * 100;
					final String msg = String.format("Processed files %d of %d (%.2f%%).", seenFiles, numberOfFilesToUnpack, percentage);
					sendMsg(msg);
					reportIn = 0;
				}
			}
		}
		final long exportAllEnd = System.currentTimeMillis();
		sendMsg(() -> String.format("Processed files %1$d of %1$d (100%%).", numberOfFilesToUnpack));
		sendMsg(() -> String.format("Exporting done in %.2fs", (exportAllEnd - exportAllStart) / 1000f));
	}

	private void export(final SourcedVaultReader vault, final IdxFileLink fileLink, final List<ExportError> exportError) {
		try {
			final ByteBuffer data = vault.getReader().getData(fileLink);

			final DataHeader dataHeader = new DataHeader(new ByteBufferBinaryReader(data));
			final Exporter exporter = findExporter(fileLink, dataHeader);
			if (exporter == null) {
				throw new UnsupportedOperationException("File type '" + fileLink.getFileEnding() + "' not supported.");
			}

			final long exportStart = System.currentTimeMillis();
			exporter.extract(fileLink, data);
			final long exportEnd = System.currentTimeMillis();
			sendDebug(() -> String.format("Unpacking of %s in %.2fs", fileLink.fullName(), (exportEnd - exportStart) / 1000f));
		} catch (final Exception e) {
			exportError.add(new ExportError(fileLink.fullName(), e));
		}
	}

	private Exporter findExporter(IdxFileLink fileLink, final DataHeader dataHeader) {
		final String fileEnding = fileLink.getFileEnding();
		for (final Exporter exporter : getExporters()) {
			final Set<String> acceptedFileEndings = exporter.getAcceptedFileEndings();
			if (acceptedFileEndings.contains(fileEnding.toLowerCase()) && exporter.accepts(dataHeader)) {
				return exporter;
			}
		}
		return null;
	}

	protected List<Exporter> getExporters() {
		return exporters;
	}

	public Path getOutputFolder() {
		return App.getInstance().getAppConfig().getOutputPath();
	}

}
