package nexusvault.cli.plugin.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import nexusvault.archive.IdxFileLink;
import nexusvault.archive.util.DataHeader;
import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;
import nexusvault.cli.ConsoleSystem.Level;
import nexusvault.cli.model.ModelPropertyChangedEvent;
import nexusvault.format.m3.Model;
import nexusvault.format.m3.export.gltf.GlTFExportMonitor;
import nexusvault.format.m3.export.gltf.GlTFExporter;
import nexusvault.format.m3.export.gltf.ResourceBundle;
import nexusvault.format.m3.v100.ModelReader;
import nexusvault.format.m3.v100.debug.ModelDebugger;
import nexusvault.format.m3.v100.debug.Table;
import nexusvault.format.m3.v100.debug.Table.TableColumn;
import nexusvault.format.m3.v100.debug.Table.TableRow;

final class ModelExporter implements Exporter {
	public static enum ExporterType {
		DEBUG,
		GLTF,
	}

	private static final class ModelExporterCmd implements Command {

		private final ModelExporter modelExporter;

		public ModelExporterCmd(ModelExporter modelExporter) {
			this.modelExporter = modelExporter;
		}

		@Override
		public CommandInfo getCommandInfo() {
			// @formatter:off
			return CommandInfo.newInfo()
					.setName("export-m3-type")
					.setDescription("Sets the exporter type. Use '?' to get more informations.")
					.setRequired(false)
					.setArguments(true)
					.setNumberOfArguments(1)
					.setNamesOfArguments("type")
					.build();
			//@formatter:on
		}

		@Override
		public void onCommand(CommandArguments args) {
			final String arg0 = args.getArg(0);

			ExporterType selectedType = null;
			for (final ExporterType type : ExporterType.values()) {
				if (type.name().equalsIgnoreCase(arg0)) {
					selectedType = type;
					break;
				}
			}

			if (selectedType == null) {
				App.getInstance().getConsole().println(Level.CONSOLE, String.format("'%s' is not a valid argument. Check '?' for more informations", arg0));
				return;
			}

			modelExporter.setExportType(selectedType);
		}

		@Override
		public void onHelp(CommandArguments args) {
			final String msg = "Available m3 exporter types: " + Arrays.toString(ExporterType.values());
			App.getInstance().getConsole().println(Level.CONSOLE, msg);
		}
	}

	public static interface InternalModelExporter {
		void export(Model model, Path dstFolder, IdxFileLink fileLink) throws IOException;
	}

	private ModelReader modelReader;
	private ExporterType exporterType;
	private ModelExporterCmd cmd;

	@Override
	public void initialize() {
		modelReader = new ModelReader();
		exporterType = ExporterType.GLTF;
		cmd = new ModelExporterCmd(this);

		App.getInstance().getCLI().registerCommand(cmd);
	}

	@Override
	public void deinitialize() {
		App.getInstance().getCLI().unregisterCommand(cmd);

		modelReader = null;
		cmd = null;
	}

	private InternalModelExporter getExporter() {
		switch (exporterType) {
			case DEBUG:
				return new DebugInternalModelExporter();
			case GLTF:
				return new GlTFInternalModelExporter();
			default:
				throw new IllegalStateException("Exporter not available: " + exporterType);
		}
	}

	public void setExportType(ExporterType exporterType) {
		this.exporterType = exporterType;

		App.getInstance().getEventSystem()
				.postEvent(new ModelPropertyChangedEvent<String>("m3-type", String.valueOf(this.exporterType), String.valueOf(exporterType)) {
				});
	}

	@Override
	public Set<String> getAcceptedFileEndings() {
		return Collections.singleton("m3");
	}

	@Override
	public boolean accepts(DataHeader header) {
		return modelReader.acceptFileSignature(header.getSignature()) && modelReader.acceptFileVersion(header.getVersion());
	}

	@Override
	public void extract(IdxFileLink fileLink, ByteBuffer data) throws IOException {
		final Path outputFolder = App.getInstance().getAppConfig().getOutputPath();
		final Path modelFolder = outputFolder.resolve(fileLink.fullName()).getParent();
		Files.createDirectories(modelFolder);
		final Model model = modelReader.read(data);
		getExporter().export(model, modelFolder, fileLink);
	}

	private static final class GlTFInternalModelExporter implements InternalModelExporter {
		@Override
		public void export(Model model, Path dstFolder, IdxFileLink fileLink) throws IOException {
			final GlTFExporter gltfExporter = new nexusvault.format.m3.export.gltf.GlTFExporter();

			gltfExporter.setGlTFExportMonitor(new GlTFExportMonitor() {
				@Override
				public void requestTextures(String textureId, ResourceBundle resourceBundle) {
					// TODO Auto-generated method stub

				}

				@Override
				public void newFile(Path path) {
					// TODO Auto-generated method stub

				}
			});

			final String modelName = fileLink.getName().substring(0, fileLink.getName().lastIndexOf('.'));
			gltfExporter.exportModel(dstFolder, modelName, model);
		}
	}

	private static final class DebugInternalModelExporter implements InternalModelExporter {
		private static final class TableJob {
			private final String fileRef;
			private final List<TableRef> tableRefs;

			public TableJob(String fileRef, List<TableRef> tableRefs) {
				this.fileRef = fileRef;
				this.tableRefs = tableRefs;
			}
		}

		private static final class TableRef {
			private final String tableRef;
			private final Table table;

			public TableRef(String tableRef, Table table) {
				this.tableRef = tableRef;
				this.table = table;
			}
		}

		@Override
		public void export(Model model, Path dstFolder, IdxFileLink fileLink) throws IOException {
			final ModelDebugger debuger = ModelDebugger.createDefaultModelDebugger();
			final Table table = debuger.debugModel(model);
			final String modelName = fileLink.getName().substring(0, fileLink.getName().lastIndexOf('.'));
			writeTable2CSV(modelName, table, dstFolder);
		}

		private static void writeTable2CSV(String modelName, final Table startTable, final Path csvOut) throws IOException {
			final Queue<TableJob> queue = new LinkedList<>();
			queue.add(new TableJob(modelName, Collections.singletonList(new TableRef(modelName, startTable))));

			while (!queue.isEmpty()) {
				final TableJob job = queue.poll();
				if (job.tableRefs.isEmpty()) {
					continue;
				}

				final boolean atLeastOneTableContainsSomething = job.tableRefs.stream().anyMatch(t -> t.table.getRowCount() != 0);
				if (!atLeastOneTableContainsSomething) {
					continue;
				}

				final Map<String, List<TableRef>> nextTable = new HashMap<>();

				final String fileRef = job.fileRef;
				try (BufferedWriter writer = Files.newBufferedWriter(csvOut.resolve(fileRef + ".csv"), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
						StandardOpenOption.TRUNCATE_EXISTING)) {

					final List<TableRef> tableRefs = job.tableRefs;

					for (int i = 0; i < tableRefs.size(); ++i) {
						final TableRef tableRef = tableRefs.get(i);
						if (i == 0) {
							writeTableHeader2CSV(writer, tableRef.tableRef, tableRef.table);
						}
						writeTableRows2CSV(writer, tableRef.tableRef, tableRef.table, nextTable);
					}
				}

				for (final Entry<String, List<TableRef>> entry : nextTable.entrySet()) {
					queue.add(new TableJob(entry.getKey(), entry.getValue()));
				}
			}
		}

		private static void writeTableHeader2CSV(BufferedWriter writer, String referenceName, final Table table) throws IOException {
			writer.append("Row#").append(";");
			writer.append("Origin Ref").append(";");
			for (int i = 0; i < table.getColumnCount(); ++i) {
				final TableColumn column = table.getColumn(i);
				writer.append(column.getColumnName());
				if (column.getMaxNumberOfEntries() > 1) {
					writer.append(" [").append(String.valueOf(column.getMaxNumberOfEntries())).append("]");
				}
				writer.append(";");
				for (int j = 1; j < column.getMaxNumberOfEntries(); ++j) {
					writer.append(";");
				}
			}
			writer.append("\n");
		}

		private static void writeTableRows2CSV(BufferedWriter writer, String referenceName, final Table table, Map<String, List<TableRef>> todo)
				throws IOException {

			final int rowCount = table.getRowCount();
			final int colCount = table.getColumnCount();

			for (int i = 0; i < rowCount; ++i) {
				writer.append(String.valueOf(i)).append(";");
				writer.append(referenceName).append(";");
				final TableRow row = table.getRow(i);
				for (int j = 0; j < colCount; ++j) {
					final TableColumn column = table.getColumn(j);
					final List<Object> entries = column.getCell(row).getEntries();
					for (int k = 0; k < entries.size(); ++k) {
						final Object entry = entries.get(k);
						if (entry instanceof Table) {
							final String fileRef = buildTableFileRef(referenceName, column.getColumnName(), k, entries.size());
							final String tableRef = buildTableRefName(referenceName, i, rowCount, column.getColumnName(), k, entries.size());

							final Table tableEntry = (Table) entry;

							writer.append(tableRef).append(" (").append(String.valueOf(tableEntry.getRowCount())).append(")");

							todo.computeIfAbsent(fileRef, (key) -> new LinkedList<>());
							todo.get(fileRef).add(new TableRef(tableRef, tableEntry));
						} else {
							writer.append(String.valueOf(entry));
						}
						writer.append(";");
					}
					for (int k = entries.size(); k < column.getMaxNumberOfEntries(); ++k) {
						writer.append(";");
					}
				}
				writer.append("\n");
			}
		}

		private static String buildTableFileRef(String baseRef, String columnName, int columnIndex, int columnCount) {
			final StringBuilder builder = new StringBuilder(baseRef);
			builder.append(".");
			builder.append(columnName);
			if (columnCount > 1) {
				builder.append("_").append(columnIndex);
			}
			return builder.toString();
		}

		private static String buildTableRefName(String baseRef, int rowIndex, int rowCount, String columnName, int columnIndex, int columnCount) {
			final StringBuilder builder = new StringBuilder(baseRef);
			builder.append(".");
			builder.append(columnName);
			if (columnCount > 1) {
				builder.append("_").append(columnIndex);
			}
			if (rowCount > 1) {
				builder.append("[").append(rowIndex).append("]");
			}
			return builder.toString();
		}
	}

}
