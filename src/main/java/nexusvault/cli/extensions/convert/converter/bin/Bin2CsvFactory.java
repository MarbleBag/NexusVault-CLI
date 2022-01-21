package nexusvault.cli.extensions.convert.converter.bin;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsFactory;
import nexusvault.format.bin.LanguageReader;

@IsFactory(id = "bin2csv", fileExtensions = "bin")
public final class Bin2CsvFactory implements ConverterFactory {

	@Override
	public void applyArguments(ConverterArgs args) {

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
				final var dictionary = LanguageReader.read(resource.getData());
				final var outputPath = manager.resolveOutputPath(PathUtil.replaceFileExtension(resource.getFile(), "csv"));

				try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
						StandardOpenOption.TRUNCATE_EXISTING)) {

					writer.append(dictionary.locale.tagName).append(";").append(dictionary.locale.longName).append(";").append(dictionary.locale.shortName)
							.append("\n");
					writer.append("Code").append(";").append("Text").append("\n");

					for (final var entry : dictionary.entries.entrySet()) {
						writer.append(String.valueOf(entry.getKey())).append(";");
						writer.append(entry.getValue()).append("\n");
					}
				}

				manager.addCreatedFile(outputPath);
			}
		};
	}

}
