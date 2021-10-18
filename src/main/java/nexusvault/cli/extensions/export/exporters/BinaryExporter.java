package nexusvault.cli.extensions.export.exporters;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;

import nexusvault.archive.IdxPath;
import nexusvault.archive.util.DataHeader;
import nexusvault.cli.extensions.export.ExportExtension;
import nexusvault.cli.extensions.export.Exporter;
import nexusvault.cli.extensions.export.PathUtil;

public final class BinaryExporter implements Exporter {

	public BinaryExporter() {
	}

	@Override
	public boolean accepts(DataHeader header) {
		return header != null;
	}

	@Override
	public Set<String> getAcceptedFileEndings() {
		return Collections.singleton("*");
	}

	@Override
	public void export(Path outputFolder, ByteBuffer data, IdxPath dataName) throws IOException {
		final Path filePath = Paths.get(PathUtil.getFullName(dataName));
		final Path exportPath = outputFolder.resolve(filePath);
		Files.createDirectories(exportPath.getParent());
		try (SeekableByteChannel channel = Files.newByteChannel(exportPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			while (data.hasRemaining()) {
				channel.write(data);
			}
			channel.close();
		}
	}

	@Override
	public String getId() {
		return "binary";
	}

	@Override
	public void loaded(ExportExtension extension) {
	}

	@Override
	public void unload() {
	}

	@Override
	public void beginExports() {
	}

	@Override
	public void finishedExports() {
	}

}
