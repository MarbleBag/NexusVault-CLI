package nexusvault.cli.extensions.convert.converter.tbl;

import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsArgument;
import nexusvault.cli.extensions.convert.IsFactory;
import nexusvault.format.tbl.converter.CSV;

@AutoInstantiate
@IsFactory(id = "tbl2csv", fileExtensions = "tbl")
public final class Tbl2CsvFactory implements ConverterFactory {

	private String cellDelimiter = ";";

	@IsArgument(name = "tbl2csv-delimiter")
	public void setCellDelimiter(String str) {
		this.cellDelimiter = str;
	}

	public String getCellDelimiter() {
		return this.cellDelimiter;
	}

	@Override
	public void applyArguments(ConverterArgs args) {
		setCellDelimiter(args.getOrElse("tbl2csv-delimiter", getCellDelimiter()));
	}

	@Override
	public Converter createConverter() {
		return new Tbl2Csv(new CSV(getCellDelimiter()));
	}

}
