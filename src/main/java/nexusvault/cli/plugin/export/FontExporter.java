package nexusvault.cli.plugin.export;

import java.util.Arrays;
import java.util.List;

final class FontExporter extends BinaryExporter {

	private final static List<String> ACCEPTED_FORMATS = Arrays.asList("ttf");

	public FontExporter() {
		super(ACCEPTED_FORMATS);
	}
}
