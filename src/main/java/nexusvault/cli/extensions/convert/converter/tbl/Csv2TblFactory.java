package nexusvault.cli.extensions.convert.converter.tbl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import kreed.io.util.SeekableByteChannelBinaryWriter;
import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsArgument;
import nexusvault.cli.extensions.convert.IsFactory;
import nexusvault.export.tbl.csv.CsvComplete;
import nexusvault.format.tbl.TableWriter;

@IsFactory(id = "csv2tbl", fileExtensions = "csv")
public final class Csv2TblFactory implements ConverterFactory {

	private String cellDelimiter = ";";

	@IsArgument(name = "csv2tbl-delimiter")
	public void setCellDelimiter(String str) {
		this.cellDelimiter = str;
	}

	public String getCellDelimiter() {
		return this.cellDelimiter;
	}

	@Override
	public void applyArguments(ConverterArgs args) {
		args.onHas("csv2tbl-delimiter", value -> setCellDelimiter(value));
	}

	@Override
	public Converter createConverter() {
		return new Converter() {
			private final CsvComplete csvConverter = new CsvComplete(getCellDelimiter());

			@Override
			public void convert(ConversionManager manager) throws IOException {
				final var resource = manager.getResource();
				final var tbl = this.csvConverter.read(resource.getDataAsReader(Charset.forName("UTF8")));

				final var outputPath = manager.resolveOutputPath(PathUtil.replaceFileExtension(resource.getFile(), "tbl"));

				try (var channel = Files.newByteChannel(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
						var writer = new SeekableByteChannelBinaryWriter(channel, ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN))) {
					TableWriter.write(tbl, writer);
				}

				manager.addCreatedFile(outputPath);
			}

			@Override
			public void deinitialize() {
			}
		};
	}

}
