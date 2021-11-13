package nexusvault.cli.extensions.convert;

import java.util.Set;

public interface ConverterFactory {

	String getId();

	int getPriority();

	Set<String> getAcceptedFileExtensions();

	Converter createConverter(ConverterOptions options);

}
