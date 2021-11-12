package nexusvault.cli.extensions.convert.converter.tbl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import kreed.io.util.SeekableByteChannelBinaryWriter;
import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.format.tbl.TableWriter;
import nexusvault.format.tbl.converter.CSV;

public final class Csv2Tbl implements Converter {

	private CSV csvConverter;
	private TableWriter tblWriter;

	protected Csv2Tbl(CSV converter) {
		this.csvConverter = converter;
		this.tblWriter = new TableWriter();
	}

	@Override
	public void deinitialize() {
		this.csvConverter = null;
		this.tblWriter = null;
	}

	@Override
	public void convert(ConversionManager manager) throws IOException {
		final var resource = manager.getResource();
		final var tbl = this.csvConverter.read(resource.getDataAsReader(Charset.forName("UTF8")));

		final var outputPath = manager.resolveOutputPath(PathUtil.replaceFileExtension(resource.getFile(), "tbl"));

		Files.createDirectories(outputPath.getParent());
		try (var channel = Files.newByteChannel(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
				var writer = new SeekableByteChannelBinaryWriter(channel, ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN))) {
			this.tblWriter.write(tbl, writer);
		}

		manager.addCreatedFile(outputPath);
	}

}
