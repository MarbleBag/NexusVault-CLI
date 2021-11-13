package nexusvault.cli.extensions.convert;

import java.io.IOException;

public interface Converter {

	void deinitialize();

	void convert(ConversionManager manager) throws IOException;

}
