package nexusvault.cli.extensions.convert.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;

import kreed.io.util.BinaryReader;

public interface Resource {

	String getFileExtension();

	Path getDirectory();

	Path getFile();

	Path getFilePath();

	ByteBuffer getDataAsBuffer() throws IOException;

	BufferedReader getDataAsReader(Charset cs) throws IOException;

	InputStream getDataAsStream() throws IOException;

	BinaryReader getData() throws IOException;
}
