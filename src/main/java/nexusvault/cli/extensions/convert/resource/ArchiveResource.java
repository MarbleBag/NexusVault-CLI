package nexusvault.cli.extensions.convert.resource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.Path;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteArrayBinaryReader;
import nexusvault.vault.IdxEntry.IdxFileLink;

public final class ArchiveResource implements Resource {

	private final IdxFileLink fileLink;

	public ArchiveResource(IdxFileLink fileLink) {
		this.fileLink = fileLink;
	}

	@Override
	public String getFileExtension() {
		return this.fileLink.getFileEnding();
	}

	@Override
	public Path getFilePath() {
		return Path.of(this.fileLink.getFullName());
	}

	@Override
	public Path getFile() {
		return Path.of(this.fileLink.getName());
	}

	@Override
	public Path getDirectory() {
		return Path.of(this.fileLink.getParent().getFullName());
	}

	@Override
	public ByteBuffer getDataAsBuffer() throws IOException {
		return ByteBuffer.wrap(getData()).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public BufferedReader getDataAsReader(Charset cs) throws IOException {
		return new BufferedReader(new InputStreamReader(getDataAsStream(), cs));
	}

	@Override
	public BinaryReader getDataAsBinaryReader() throws IOException {
		return new ByteArrayBinaryReader(getData(), ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public InputStream getDataAsStream() throws IOException {
		return new ByteArrayInputStream(getData());
	}

	@Override
	public byte[] getData() throws IOException {
		return this.fileLink.getData();
	}

}
