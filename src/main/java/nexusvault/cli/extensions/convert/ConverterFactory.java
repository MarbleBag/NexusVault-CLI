package nexusvault.cli.extensions.convert;

import java.util.Set;

public interface ConverterFactory {

	String getId();

	Set<String> getAcceptedFileExtensions();

	Converter createConverter(ConverterOptions options);

}
