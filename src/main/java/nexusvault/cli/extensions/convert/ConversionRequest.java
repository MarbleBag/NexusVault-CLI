package nexusvault.cli.extensions.convert;

import java.nio.file.Path;

import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.convert.resource.Resource;

public final class ConversionRequest {
	public final Resource input;
	public final Path outputDir;

	public ConversionRequest(Resource input, Path output) {
		if (input == null) {
			throw new IllegalArgumentException("'input' must not be null");
		}

		this.input = input;

		if (output == null) {
			this.outputDir = Path.of(PathUtil.getFileName(input.getFile()));
		} else {
			this.outputDir = output;
		}
	}
}