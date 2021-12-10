package nexusvault.cli.extensions.convert.converter.tbl;

import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsArgument;
import nexusvault.cli.extensions.convert.IsFactory;
import nexusvault.format.tbl.converter.CSV;

@AutoInstantiate
@IsFactory(id = "csv2tbl", fileExtensions = "csv")
public final class Csv2TblFactory implements ConverterFactory {

	private String cellDelimiter = ";";

	@IsArgument(name = "csv2tbl-delimiter")
	public void setCellDelimiter(String str) {
		this.cellDelimiter = str;
	}

	public String getCellDelimiter() {
		return this.cellDelimiter;
	}

	@Override
	public void applyArguments(ConverterArgs args) {
		setCellDelimiter(args.getOrElse("csv2tbl-delimiter", getCellDelimiter()));
	}

	@Override
	public Converter createConverter() {
		return new Csv2Tbl(new CSV(getCellDelimiter()));
	}

}