package nexusvault.cli.extensions.convert.converter.tbl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import nexusvault.cli.core.PathUtil;
import nexusvault.cli.core.cmd.ArgumentHelper;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsArgument;
import nexusvault.cli.extensions.convert.IsFactory;
import nexusvault.export.tbl.csv.Csv;
import nexusvault.format.tbl.TableReader;

@IsFactory(id = "tbl2csv", fileExtensions = "tbl")
public final class Tbl2CsvFactory implements ConverterFactory {

	private String cellDelimiter = ";";
	private boolean simple = false;

	@IsArgument(name = "tbl2csv-delimiter")
	public void setCellDelimiter(String str) {
		this.cellDelimiter = str;
	}

	public String getCellDelimiter() {
		return this.cellDelimiter;
	}

	@IsArgument(name = "tbl2csv-simple", description = "Export only column names and values. Can be set to 'yes' or 'no'. A simple csv can't be converted back to a tbl!", isArgumentOptional = true)
	public void setSimpleCSV(boolean simple) {
		this.simple = simple;
	}

	public boolean getSimpleCSV() {
		return this.simple;
	}

	@Override
	public void applyArguments(ConverterArgs args) {
		args.onHas("tbl2csv-delimiter", value -> setCellDelimiter(value));
		args.onHas("tbl2csv-simple", value -> setSimpleCSV(ArgumentHelper.toBoolean(value, getSimpleCSV())));
	}

	@Override
	public Converter createConverter() {
		return new Converter() {
			private final Csv csv = new Csv(getCellDelimiter());
			private final boolean isSimple = Tbl2CsvFactory.this.simple;

			@Override
			public void convert(ConversionManager manager) throws IOException {
				final var resource = manager.getResource();
				final var tbl = TableReader.read(resource.getData());

				final var outputPath = manager.resolveOutputPath(PathUtil.replaceFileExtension(resource.getFile(), "csv"));

				try (var writer = Files.newBufferedWriter(outputPath, Charset.forName("UTF8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
						StandardOpenOption.WRITE)) {
					if (this.isSimple) {
						this.csv.writeSimple(tbl, writer);
					} else {
						this.csv.write(tbl, writer);
					}
				}
				manager.addCreatedFile(outputPath);
			}

			@Override
			public void deinitialize() {
			}
		};
	}

}
