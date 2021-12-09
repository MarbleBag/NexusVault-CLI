package nexusvault.cli.extensions.convert;

public interface ConverterFactory {

	void applyArguments(ConverterArgs args);

	Converter createConverter();

}
