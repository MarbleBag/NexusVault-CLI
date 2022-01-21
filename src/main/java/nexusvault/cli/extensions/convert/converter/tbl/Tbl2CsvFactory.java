package nexusvault.cli.extensions.convert.converter.tbl;

import java.io.IOException;
import java.io.Writer;

import nexusvault.cli.core.cmd.ArgumentHelper;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsArgument;
import nexusvault.cli.extensions.convert.IsFactory;
import nexusvault.export.tbl.csv.CsvComplete;
import nexusvault.export.tbl.csv.CsvSimple;
import nexusvault.format.tbl.Table;

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
		Tbl2Csv.CSVWriter writer;
		if (this.simple) {
			writer = new Tbl2Csv.CSVWriter() {
				private final CsvSimple writer = new CsvSimple(getCellDelimiter());

				@Override
				public void write(Table tbl, Writer writer) throws IOException {
					this.writer.write(tbl, writer);
				}
			};
		} else {
			writer = new Tbl2Csv.CSVWriter() {
				private final CsvComplete writer = new CsvComplete(getCellDelimiter());

				@Override
				public void write(Table tbl, Writer writer) throws IOException {
					this.writer.write(tbl, writer);
				}
			};
		}

		return new Tbl2Csv(writer);
	}

}
