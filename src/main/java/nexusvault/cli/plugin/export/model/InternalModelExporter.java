package nexusvault.cli.plugin.export.model;

import java.io.IOException;
import java.nio.file.Path;

import nexusvault.archive.IdxPath;
import nexusvault.format.m3.Model;

public interface InternalModelExporter {
	void export(Model model, Path dstFolder, IdxPath filePath) throws IOException;
}