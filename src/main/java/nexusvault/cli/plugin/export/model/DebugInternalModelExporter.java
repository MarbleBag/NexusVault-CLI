package nexusvault.cli.plugin.export.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import nexusvault.archive.IdxPath;
import nexusvault.cli.plugin.export.PathUtil;
import nexusvault.format.m3.Model;
import nexusvault.format.m3.v100.debug.ModelDebugger;
import nexusvault.format.m3.v100.debug.Table;
import nexusvault.format.m3.v100.debug.Table.TableColumn;
import nexusvault.format.m3.v100.debug.Table.TableRow;

final class DebugInternalModelExporter implements InternalModelExporter {
	private static final class TableJob {
		private final String fileRef;
		private final List<DebugInternalModelExporter.TableRef> tableRefs;

		public TableJob(String fileRef, List<DebugInternalModelExporter.TableRef> tableRefs) {
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
	public void export(Model model, Path dstFolder, IdxPath filePath) throws IOException {
		final ModelDebugger debuger = ModelDebugger.createDefaultModelDebugger();
		final Table table = debuger.debugModel(model);
		final String modelName = PathUtil.getNameWithoutExtension(filePath);
		dstFolder = dstFolder.resolve(modelName + "_debug");
		Files.createDirectories(dstFolder);
		writeTable2CSV(modelName, table, dstFolder);
	}

	private static void writeTable2CSV(String modelName, final Table startTable, final Path csvOut) throws IOException {
		final Queue<DebugInternalModelExporter.TableJob> queue = new LinkedList<>();
		queue.add(new TableJob(modelName, Collections.singletonList(new TableRef(modelName, startTable))));

		while (!queue.isEmpty()) {
			final DebugInternalModelExporter.TableJob job = queue.poll();
			if (job.tableRefs.isEmpty()) {
				continue;
			}

			final boolean atLeastOneTableContainsSomething = job.tableRefs.stream().anyMatch(t -> t.table.getRowCount() != 0);
			if (!atLeastOneTableContainsSomething) {
				continue;
			}

			final Map<String, List<DebugInternalModelExporter.TableRef>> nextTable = new HashMap<>();

			final String fileRef = job.fileRef;
			try (BufferedWriter writer = Files.newBufferedWriter(csvOut.resolve(fileRef + ".csv"), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING)) {

				final List<DebugInternalModelExporter.TableRef> tableRefs = job.tableRefs;

				for (int i = 0; i < tableRefs.size(); ++i) {
					final DebugInternalModelExporter.TableRef tableRef = tableRefs.get(i);
					if (i == 0) {
						writeTableHeader2CSV(writer, tableRef.tableRef, tableRef.table);
					}
					writeTableRows2CSV(writer, tableRef.tableRef, tableRef.table, nextTable);
				}
			}

			for (final Entry<String, List<DebugInternalModelExporter.TableRef>> entry : nextTable.entrySet()) {
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

	private static void writeTableRows2CSV(BufferedWriter writer, String referenceName, final Table table,
			Map<String, List<DebugInternalModelExporter.TableRef>> todo) throws IOException {

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