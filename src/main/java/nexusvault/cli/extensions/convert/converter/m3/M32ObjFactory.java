package nexusvault.cli.extensions.convert.converter.m3;

import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.IsFactory;

@AutoInstantiate
@IsFactory(id = "m32obj", fileExtensions = "m3", priority = 2)
public final class M32ObjFactory implements ConverterFactory {

	@Override
	public Converter createConverter() {
		return new M32Obj();
	}

	@Override
	public void applyArguments(ConverterArgs args) {
	}

}
