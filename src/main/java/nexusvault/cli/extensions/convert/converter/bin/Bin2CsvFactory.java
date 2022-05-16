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

			nexusvault.export.bin.csv.Csv exporter = new nexusvault.export.bin.csv.Csv();

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
					this.exporter.write(dictionary, writer);
				}

				manager.addCreatedFile(outputPath);
			}
		};
	}

}
