package nexusvault.cli.extensions.convert.converter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.ConverterOptions;

@AutoInstantiate
public final class AsIsFactory implements ConverterFactory {

	@Override
	public String getId() {
		return "direct";
	}

	@Override
	public Set<String> getAcceptedFileExtensions() {
		return new HashSet<>(Arrays.asList("ttf", "lua", "xml"));
	}

	@Override
	public Converter createConverter(ConverterOptions options) {
		return new Converter() {
			@Override
			public void deinitialize() {
			}

			@Override
			public void convert(ConversionManager manager) throws IOException {
				final var resource = manager.getResource();
				final var outputPath = manager.resolveOutputPath(resource.getFile());
				Files.createDirectories(outputPath.getParent());
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
