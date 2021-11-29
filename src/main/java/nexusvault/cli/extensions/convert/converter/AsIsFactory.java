package nexusvault.cli.extensions.convert.converter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsFactory;

@AutoInstantiate
@IsFactory(id = "direct", fileExtensions = { "ttf", "lua", "xml" }, priority = 0)
public final class AsIsFactory implements ConverterFactory {

	@Override
	public void applyArguments(ConverterArgs arg) {
	}

	@Override
	public Converter createConverter() {
		return new Converter() {
			@Override
			public void deinitialize() {
			}

			@Override
			public void convert(ConversionManager manager) throws IOException {
				final var resource = manager.getResource();
				final var outputPath = manager.resolveOutputPath(resource.getFile());
				try (var channel = Files.newByteChannel(outputPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
						StandardOpenOption.TRUNCATE_EXISTING)) {
					final var data = resource.getDataAsBuffer();
					while (data.hasRemaining()) {
						channel.write(data);
					}
				}
				manager.addCreatedFile(outputPath);
			}
		};
	}

}
