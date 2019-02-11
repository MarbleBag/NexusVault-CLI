package nexusvault.cli.plugin.export;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;

import nexusvault.archive.IdxFileLink;
import nexusvault.archive.util.DataHeader;

interface Exporter {

	/**
	 * Called once, when {@link ExportPlugIn} loads this {@link Exporter}. Use this function to register necessary resources
	 */
	void initialize();

	/**
	 * Called once, when {@link ExportPlugIn} unloads this {@link Exporter}. Make sure to unload all resources which are only used by this {@link Exporter}.
	 */
	void deinitialize();

	Set<String> getAcceptedFileEndings();

	boolean accepts(DataHeader header);

	void export(IdxFileLink file, ByteBuffer data) throws IOException;

}
