package nexusvault.cli.extensions.convert.converter.tbl;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.format.tbl.Table;
import nexusvault.format.tbl.TableReader;

public final class Tbl2Csv implements Converter {

	public static interface CSVWriter {
		void write(Table tbl, Writer writer) throws IOException;
	}

	private CSVWriter csvWriter;

	public Tbl2Csv(CSVWriter csvWriter) {
		this.csvWriter = csvWriter;
	}

	@Override
	public void deinitialize() {
		this.csvWriter = null;
	}

	@Override
	public void convert(ConversionManager manager) throws IOException {
		final var resource = manager.getResource();
		final var tbl = TableReader.read(resource.getData());

		final var outputPath = manager.resolveOutputPath(PathUtil.replaceFileExtension(resource.getFile(), "csv"));

		try (var writer = Files.newBufferedWriter(outputPath, Charset.forName("UTF8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.WRITE)) {
			this.csvWriter.write(tbl, writer);
		}
		manager.addCreatedFile(outputPath);
	}

}
