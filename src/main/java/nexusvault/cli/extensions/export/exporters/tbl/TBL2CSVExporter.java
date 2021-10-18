package nexusvault.cli.extensions.export.exporters.tbl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import kreed.io.util.ByteBufferBinaryReader;
import nexusvault.archive.IdxPath;
import nexusvault.archive.util.DataHeader;
import nexusvault.cli.extensions.export.ExportExtension;
import nexusvault.cli.extensions.export.Exporter;
import nexusvault.cli.extensions.export.PathUtil;
import nexusvault.format.tbl.RawTable;
import nexusvault.format.tbl.TableReader;
import nexusvault.format.tbl.TableRecord;

final class TBL2CSVExporter implements Exporter {

	private static final String elementDelimiter = ";";

	private TableReader tableReader;

	@Override
	public void export(Path outputFolder, ByteBuffer data, IdxPath dataName) throws IOException {
		final RawTable table = this.tableReader.read(new ByteBufferBinaryReader(data));

		final List<String> toOut = new LinkedList<>();
		final int fieldCount = table.getFieldCount();
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < fieldCount; ++i) {
			if (i != 0) {
				builder.append(elementDelimiter);
			}
			builder.append(table.getFieldName(i));
		}
		toOut.add(builder.toString());
		builder.setLength(0);

		for (final TableRecord record : table.getRecords()) {
			for (int j = 0; j < fieldCount; ++j) {
				if (j != 0) {
					builder.append(elementDelimiter);
				}
				builder.append(record.get(j));
			}
			toOut.add(builder.toString());
			builder.setLength(0);
		}

		final Path dstPath = outputFolder.resolve(Paths.get(PathUtil.getFullName(dataName) + ".csv"));
		Files.createDirectories(dstPath.getParent());
		Files.write(dstPath, toOut, Charset.forName("UTF8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
	}

	@Override
	public boolean accepts(DataHeader header) {
		return this.tableReader.acceptFileSignature(header.getSignature()) && this.tableReader.acceptFileVersion(header.getVersion());
	}

	@Override
	public Set<String> getAcceptedFileEndings() {
		return Collections.singleton("tbl");
	}

	@Override
	public String getId() {
		return "tbl2csv";
	}

	@Override
	public void loaded(ExportExtension extension) {
	}

	@Override
	public void unload() {
	}

	@Override
	public void beginExports() {
		this.tableReader = new TableReader();
	}

	@Override
	public void finishedExports() {
		this.tableReader = null;
	}

}
