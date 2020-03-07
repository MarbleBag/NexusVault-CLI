package nexusvault.cli.plugin.make;

import nexusvault.cli.plugin.AbstractPlugIn;

public final class MakePlugIn extends AbstractPlugIn {

	public MakePlugIn() {
		setCommands( //
				new MakeTextureCmd()//
		);
		setNoArguments();
	}

	public void initialze() {
		super.initialize();
		// TODO
	}

	@Override
	public void deinitialize() {
		super.deinitialize();
		// TODO
	}

}
