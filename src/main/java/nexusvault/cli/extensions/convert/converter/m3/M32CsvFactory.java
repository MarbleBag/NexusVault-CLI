package nexusvault.cli.extensions.convert.converter.m3;

import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsFactory;

@IsFactory(id = "m32csv", fileExtensions = "m3")
public final class M32CsvFactory implements ConverterFactory {

	@Override
	public Converter createConverter() {
		return new M32Csv();
	}

	@Override
	public void applyArguments(ConverterArgs args) {

	}

}
