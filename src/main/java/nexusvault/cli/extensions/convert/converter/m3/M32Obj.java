package nexusvault.cli.extensions.convert.converter.m3;

import java.io.IOException;

import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.export.m3.obj.ObjExporter;
import nexusvault.format.m3.ModelReader;

public class M32Obj implements Converter {

	public M32Obj() {

	}

	@Override
	public void deinitialize() {

	}

	@Override
	public void convert(ConversionManager manager) throws IOException {
		final var resource = manager.getResource();
		final var m3 = ModelReader.read(resource.getData());
		final var fileName = PathUtil.getFileName(resource.getFile());
		final ObjExporter objExporter = new ObjExporter();
		objExporter.exportModel(manager.getOutputPath(), fileName, m3);
		manager.addCreatedFile(manager.getOutputPath().resolve(PathUtil.addFileExtension(fileName, "obj")));
		manager.addCreatedFile(manager.getOutputPath().resolve(PathUtil.addFileExtension(fileName, "mtl")));
	}

}
