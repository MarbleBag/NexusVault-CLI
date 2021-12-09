package nexusvault.cli.extensions.convert.converter.m3;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.format.m3.v100.ModelReader;
import nexusvault.format.m3.v100.debug.ModelDebugger;
import nexusvault.format.m3.v100.debug.Table;
import nexusvault.format.m3.v100.debug.Table.DataType;
import nexusvault.format.m3.v100.debug.Table.TableColumn;

public final class M32Csv implements Converter {

	private ModelDebugger debuger;
	private ModelReader modelReader;

	public M32Csv() {
		this.modelReader = new ModelReader();
		this.debuger = ModelDebugger.createDefaultModelDebugger();
	}

	@Override
	public void deinitialize() {
		this.modelReader = null;
		this.debuger = null;
	}

	@Override
	public void convert(ConversionManager manager) throws IOException {
		final var resource = manager.getResource();
		final var m3 = this.modelReader.read(resource.getDataAsBuffer());
		final var table = this.debuger.debugModel(m3);

		final var tablesToWrite = collectReferences(table, PathUtil.getFileName(resource.getFile()));

		for (final var entry : tablesToWrite.entrySet()) {
			final var outputFile = manager.resolveOutputPath(PathUtil.addFileExtension(entry.getKey(), "csv"));
			try (BufferedWriter writer = Files.newBufferedWriter(outputFile, Charset.forName("UTF8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING)) {

				boolean writeCSVColumns = true;
				for (final var tableRef : entry.getValue()) {
					if (writeCSVColumns) {
						writeCSVColumns(writer, tableRef.table);
						writeCSVColumns = false;
					}
					writeCSVRows(writer, tableRef);
				}
			}
		}
	}

	private static final class TableRef {
		private final String tableName;
		private final Table table;
		private final String referencedBy;

		public TableRef(String tableName, Table table, String referencedBy) {
			this.tableName = tableName;
			this.table = table;
			this.referencedBy = referencedBy == null ? "" : referencedBy;
		}
	}

	private static HashMap<String, List<TableRef>> collectReferences(Table table, String tableName) {
		final Queue<TableRef> todo = new LinkedList<>();
		todo.add(new TableRef(tableName, table, null));
		final var references = new HashMap<String, List<TableRef>>();
		while (!todo.isEmpty()) {
			final var nextRef = todo.poll();
			if (nextRef.table.getRowCount() > 0) {
				references.computeIfAbsent(nextRef.tableName, key -> new LinkedList<>()).add(nextRef);
			}
			collectReferences(nextRef.table, nextRef.tableName, todo);
		}

		return references;
	}

	private static void collectReferences(Table table, String tableName, Collection<TableRef> references) {
		final int rowCount = table.getRowCount();
		final int colCount = table.getColumnCount();
		for (var rowIdx = 0; rowIdx < rowCount; ++rowIdx) {
			final var row = table.getRow(rowIdx);
			for (var colIdx = 0; colIdx < colCount; ++colIdx) {
				final var column = table.getColumn(colIdx);
				final var entries = column.getCell(row).getEntries();
				for (int i = 0; i < entries.size(); ++i) {
					final var entry = entries.get(i);
					if (entry instanceof Table) {
						final var childTableName = createTableName(tableName, column, entries, i);
						final var backReference = tableName + "[" + rowIdx + "]";
						references.add(new TableRef(childTableName, (Table) entry, backReference));
					}
				}
			}
		}
	}

	protected static String createTableName(String tableName, final TableColumn column, final List<Object> entries, int i) {
		return tableName + "." + column.getColumnName() + (entries.size() > 1 ? "[" + i + "]" : "");
	}

	private static void writeCSVColumns(BufferedWriter writer, Table table) throws IOException {
		writer.append("Row#").append(";");
		writer.append("Referenced By").append(";"); // TODO ???
		for (int i = 0; i < table.getColumnCount(); ++i) {
			final var column = table.getColumn(i);
			writer.append(column.getColumnName());
			if (column.getDataType() != DataType.NONE) {
				writer.append(" [").append(String.valueOf(column.getDataType()));
				if (column.getMaxNumberOfEntries() > 1) {
					writer.append(", ").append(String.valueOf(column.getMaxNumberOfEntries()));
				}
				writer.append("]");
			} else if (column.getMaxNumberOfEntries() > 1) {
				writer.append(" [").append(String.valueOf(column.getMaxNumberOfEntries())).append("]");
			}
			writer.append(";");
			for (int j = 1; j < column.getMaxNumberOfEntries(); ++j) {
				writer.append(";");
			}
		}
		writer.append("\n");
	}

	private static void writeCSVRows(BufferedWriter writer, TableRef ref) throws IOException {
		final var table = ref.table;
		final int rowCount = table.getRowCount();
		final int colCount = table.getColumnCount();
		for (var rowIdx = 0; rowIdx < rowCount; ++rowIdx) {
			writer.append(String.valueOf(rowIdx)).append(";");
			writer.append(ref.referencedBy).append(";");
			final var row = table.getRow(rowIdx);
			for (var colIdx = 0; colIdx < colCount; ++colIdx) {
				final var column = table.getColumn(colIdx);
				final var entries = column.getCell(row).getEntries();
				for (int i = 0; i < entries.size(); ++i) {
					final var entry = entries.get(i);
					if (entry instanceof Table) {
						if (((Table) entry).getRowCount() > 0) {
							writer.append(createTableName(ref.tableName, column, entries, i));
						}
					} else {
						writer.append(String.valueOf(entry));
					}
					writer.append(";");
				}
				for (int i = entries.size(); i < column.getMaxNumberOfEntries(); ++i) {
					writer.append(";");
				}
			}
			writer.append("\n");
		}
	}

}
