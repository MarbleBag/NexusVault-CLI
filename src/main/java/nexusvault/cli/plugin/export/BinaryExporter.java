package nexusvault.cli.plugin.export;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nexusvault.archive.IdxPath;
import nexusvault.archive.util.DataHeader;

class BinaryExporter implements Exporter {

	private final Set<String> acceptedFileEndings;

	public BinaryExporter(List<String> acceptedFileEndings) {
		this.acceptedFileEndings = Collections.unmodifiableSet(new HashSet<>(acceptedFileEndings));
	}

	@Override
	public boolean accepts(DataHeader header) {
		return header != null;
	}

	@Override
	public Set<String> getAcceptedFileEndings() {
		return acceptedFileEndings;
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
	public void initialize() {

	}

	@Override
	public void deinitialize() {
	}

}
