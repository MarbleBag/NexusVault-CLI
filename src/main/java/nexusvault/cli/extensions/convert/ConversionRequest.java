package nexusvault.cli.extensions.convert;

import java.nio.file.Path;

import nexusvault.cli.extensions.convert.resource.Resource;

public final class ConversionRequest {
	public final Resource input;
	public final Path outputDir;

	public ConversionRequest(Resource input, Path output) {
		this.input = input;
		this.outputDir = output;
	}
}