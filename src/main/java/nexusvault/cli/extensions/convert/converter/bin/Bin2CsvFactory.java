package nexusvault.cli.extensions.convert.converter.bin;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;

import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.ConverterOptions;
import nexusvault.format.bin.LanguageEntry;
import nexusvault.format.bin.LanguageReader;

@AutoInstantiate
public final class Bin2CsvFactory implements ConverterFactory {

	@Override
	public String getId() {
		return "bin2csv";
	}

	@Override
	public int getPriority() {
		return 1;
	}

	@Override
	public Set<String> getAcceptedFileExtensions() {
		return Collections.singleton("bin");
	}

	@Override
	public Converter createConverter(ConverterOptions options) {
		return new Converter() {
			LanguageReader languageReader = new LanguageReader();

			@Override
			public void deinitialize() {
				this.languageReader = null;
			}

			@Override
			public void convert(ConversionManager manager) throws IOException {
				final var resource = manager.getResource();
				final var dictionary = this.languageReader.read(resource.getData());
				final var outputPath = manager.resolveOutputPath(PathUtil.replaceFileExtension(resource.getFile(), "csv"));

				try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
						StandardOpenOption.TRUNCATE_EXISTING)) {

					writer.append(dictionary.getLocaleTag()).append(";").append(dictionary.getLocaleLong()).append(";").append(dictionary.getLocaleShort())
							.append("\n");
					writer.append("Code").append(";").append("Text").append("\n");

					for (final LanguageEntry entry : dictionary) {
						writer.append(String.valueOf(entry.getId())).append(";");
						writer.append(entry.getText()).append("\n");
					}
				}

				manager.addCreatedFile(outputPath);
			}
		};
	}

}
