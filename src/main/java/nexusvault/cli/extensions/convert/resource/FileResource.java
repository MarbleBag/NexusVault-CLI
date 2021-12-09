package nexusvault.cli.extensions.convert.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import kreed.io.util.BinaryReader;
import kreed.io.util.SeekableByteChannelBinaryReader;
import nexusvault.cli.core.PathUtil;

public final class FileResource implements Resource {
	private final Path path;

	public FileResource(Path path) {
		this.path = path;
	}

	@Override
	public String getFileExtension() {
		return PathUtil.getFileExtension(this.path);
	}

	@Override
	public Path getFilePath() {
		return this.path;
	}

	@Override
	public Path getDirectory() {
		return this.path.getParent();
	}

	@Override
	public Path getFile() {
		return this.path.getFileName();
	}

	@Override
	public ByteBuffer getDataAsBuffer() throws IOException {
		try (SeekableByteChannel channel = Files.newByteChannel(this.path, StandardOpenOption.READ)) {
			final ByteBuffer data = ByteBuffer.allocate((int) channel.size()).order(ByteOrder.LITTLE_ENDIAN);

			int numberOfReadbytes = 0;
			int counter = 0;
			do {
				numberOfReadbytes = channel.read(data);
				if (numberOfReadbytes == -1) {
					break;
				} else if (numberOfReadbytes == 0) {
					if (!data.hasRemaining()) {
						break;
					} else {
						counter += 1;
						if (counter > 100) {
							break; // we will have to work with what we got - for now
						}
					}
				} else {
					counter = 0;
				}
			} while (true);

			return data.flip();
		}
	}

	@Override
	public BufferedReader getDataAsReader(Charset cs) throws IOException {
		return Files.newBufferedReader(this.path, cs);
	}

	@Override
	public InputStream getDataAsStream() throws IOException {
		return Files.newInputStream(this.path, StandardOpenOption.READ);
	}

	@Override
	public BinaryReader getData() throws IOException {
		return new SeekableByteChannelBinaryReader(Files.newByteChannel(this.path, StandardOpenOption.READ),
				ByteBuffer.allocate(1024 * 8).order(ByteOrder.LITTLE_ENDIAN));
	}

}
