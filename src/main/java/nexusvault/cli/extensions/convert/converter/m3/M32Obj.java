package nexusvault.cli.extensions.convert.converter.m3;

import java.io.IOException;

import nexusvault.cli.core.PathUtil;
import nexusvault.cli.extensions.convert.ConversionManager;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.format.m3.export.obj.ObjExporter;
import nexusvault.format.m3.v100.ModelReader;

public class M32Obj implements Converter {

	private ModelReader modelReader;

	public M32Obj() {
		this.modelReader = new ModelReader();
	}

	@Override
	public void deinitialize() {
		this.modelReader = null;
	}

	@Override
	public void convert(ConversionManager manager) throws IOException {
		final var resource = manager.getResource();
		final var m3 = this.modelReader.read(resource.getDataAsBuffer());
		final var fileName = PathUtil.getFileName(resource.getFile());
		final ObjExporter objExporter = new ObjExporter();
		objExporter.exportModel(manager.getOutputPath(), fileName, m3);
		manager.addCreatedFile(manager.getOutputPath().resolve(PathUtil.addFileExtension(fileName, "obj")));
		manager.addCreatedFile(manager.getOutputPath().resolve(PathUtil.addFileExtension(fileName, "mtl")));
	}

}
