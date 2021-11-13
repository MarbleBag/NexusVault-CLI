package nexusvault.cli.extensions.convert.converter.m3;

import java.util.Collections;
import java.util.Set;

import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.ConverterOptions;

@AutoInstantiate
public final class M32CsvFactory implements ConverterFactory {

	@Override
	public String getId() {
		return "m32csv";
	}

	@Override
	public int getPriority() {
		return 1;
	}

	@Override
	public Set<String> getAcceptedFileExtensions() {
		return Collections.singleton("m3");
	}

	@Override
	public Converter createConverter(ConverterOptions options) {
		return new M32Csv();
	}

}
