package nexusvault.cli.extensions.convert.converter.tbl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.format.tbl.TableReader;
import nexusvault.format.tbl.converter.CSV;

public final class Tbl2Csv implements Converter {

	private CSV csvConverter;
	private TableReader tblReader;

	public Tbl2Csv(CSV csv) {
		this.csvConverter = new CSV();
		this.tblReader = new TableReader();
	}

	@Override
	public void deinitialize() {
		this.csvConverter = null;
		this.tblReader = null;
	}

	@Override
	public void convert(ConversionManager manager) throws IOException {
		final var resource = manager.getResource();
		final var tbl = this.tblReader.read(resource.getData());

		final var outputPath = manager.resolveOutputPath(PathUtil.replaceFileExtension(resource.getFile(), "csv"));

		Files.createDirectories(outputPath.getParent());
		try (var writer = Files.newBufferedWriter(outputPath, Charset.forName("UTF8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.WRITE)) {
			this.csvConverter.write(tbl, writer);
		}
		manager.addCreatedFile(outputPath);
	}

}
