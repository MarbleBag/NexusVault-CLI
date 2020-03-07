package nexusvault.cli.plugin.export.model;

import java.io.IOException;
import java.nio.file.Path;

import nexusvault.archive.IdxPath;
import nexusvault.cli.plugin.export.PathUtil;
import nexusvault.format.m3.Model;
import nexusvault.format.m3.export.obj.ObjExporter;

final class ObjInternalModelExporter implements InternalModelExporter {

	@Override
	public void export(Model model, Path dstFolder, IdxPath filePath) throws IOException {
		final ObjExporter objExporter = new nexusvault.format.m3.export.obj.ObjExporter();
		final String modelName = PathUtil.getNameWithoutExtension(filePath);
		objExporter.exportModel(dstFolder, modelName, model);
	}

}