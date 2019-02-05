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

import nexusvault.archive.IdxFileLink;
import nexusvault.archive.util.DataHeader;
import nexusvault.cli.App;

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
	public void extract(IdxFileLink file, ByteBuffer data) throws IOException {
		final Path filePath = Paths.get(file.fullName());
		final Path outputFolder = App.getInstance().getPlugIn(ExportPlugIn.class).getOutputFolder();

		final Path exportPath = outputFolder.resolve(filePath);
		Files.createDirectories(exportPath);
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
