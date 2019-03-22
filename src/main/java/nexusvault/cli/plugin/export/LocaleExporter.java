package nexusvault.cli.plugin.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;

import nexusvault.archive.IdxPath;
import nexusvault.archive.util.DataHeader;
import nexusvault.format.bin.LanguageDictionary;
import nexusvault.format.bin.LanguageReader;
import nexusvault.format.bin.LanguageReader.LanguageEntry;

final class LocaleExporter implements Exporter {
	private LanguageReader languageReader;

	@Override
	public void initialize() {
		languageReader = new LanguageReader();
	}

	@Override
	public void deinitialize() {
		languageReader = null;
	}

	@Override
	public Set<String> getAcceptedFileEndings() {
		return Collections.singleton("bin");
	}

	@Override
	public boolean accepts(DataHeader header) {
		return header != null;
	}

	@Override
	public void export(Path outputFolder, ByteBuffer data, IdxPath dataName) throws IOException {
		final LanguageDictionary dictionary = languageReader.read(data);
		final Path outputFile = outputFolder.resolve(PathUtil.getFullName(dataName) + ".csv");
		Files.createDirectories(outputFile.getParent());
		try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			writer.append(dictionary.getLocaleTag()).append(";").append(dictionary.getLocaleLong()).append(";").append(dictionary.getLocaleShort())
					.append("\n");
			writer.append("Code").append(";").append("Text").append("\n");

			for (final LanguageEntry entry : dictionary) {
				writer.append(String.valueOf(entry.getId())).append(";");
				writer.append("\"").append(entry.getText()).append("\"\n");
			}
		}
	}

}
