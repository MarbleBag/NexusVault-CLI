package nexusvault.cli.plugin.make;

import nexusvault.cli.core.extension.AbstractExtension;

public final class MakePlugIn extends AbstractExtension {

	@Override
	protected void initializeExtension(InitializationHelper initializationHelper) {
		initializationHelper.addCommandHandler(new MakeTextureCmd());

	}

	@Override
	protected void deinitializeExtension() {
		// TODO Auto-generated method stub

	}

}
