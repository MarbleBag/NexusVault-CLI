package nexusvault.cli.extensions.convert.converter.tbl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import nexusvault.cli.core.App;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.format.tbl.TableReader;

// @IsFactory(id = "tbl2debug", fileExtensions = "tbl")
public class Tbl2DebugFactory implements ConverterFactory {

	@Override
	public void applyArguments(ConverterArgs args) {

	}

	@Override
	public Converter createConverter() {
		return new Converter() {

			private final Path outputPath = App.getInstance().getAppConfig().getOutputPath().resolve("TblColumnsOverview.csv");
			private final Map<String, Set<Long>> samples = new HashMap<>();

			@Override
			public void deinitialize() {
				final var builder = new StringBuilder();
				for (final var e : this.samples.entrySet()) {
					final var values = e.getValue().stream().map(f -> f.toString()).collect(Collectors.joining(", "));
					builder.append(e.getKey()).append(";").append(values).append("\n");
				}

				try {
					try (var writer = Files.newBufferedWriter(this.outputPath, Charset.forName("UTF8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND,
							StandardOpenOption.WRITE)) {
						writer.append(builder.toString());
					}
				} catch (final IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			@Override
			public void convert(ConversionManager manager) throws IOException {
				final var resource = manager.getResource();
				final var tbl = TableReader.read(resource.getData());
				for (final var c : tbl.columns) {
					final var set = this.samples.computeIfAbsent(c.type.toString(), k -> new HashSet<>());
					set.add(c.unk2);
				}
			}

		};
	}
}
