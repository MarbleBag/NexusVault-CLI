package nexusvault.cli.plugin.export;

import java.util.Arrays;
import java.util.List;

final class TextExporter extends BinaryExporter {

	private final static List<String> ACCEPTED_FORMATS = Arrays.asList("lua", "xml");

	public TextExporter() {
		super(ACCEPTED_FORMATS);
	}

}
