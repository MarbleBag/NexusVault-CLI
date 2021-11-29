package nexusvault.cli.extensions.export;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Set;

import nexusvault.archive.IdxPath;
import nexusvault.archive.util.DataHeader;

public interface Exporter {
	String getId();

	Set<String> getAcceptedFileEndings();

	boolean accepts(DataHeader header);

	/**
	 * Called once, when {@link ExportPlugIn} loads this {@link Exporter}. Use this function to register necessary resources
	 */
	void load(ExportExtension extension);

	/**
	 * Called once, when {@link ExportPlugIn} unloads this {@link Exporter}. Make sure to unload all resources which are only used by this {@link Exporter}.
	 */
	void unload();

	void beginExports();

	void finishedExports();

	void export(Path outputFolder, ByteBuffer data, IdxPath dataLocation) throws IOException;
}
