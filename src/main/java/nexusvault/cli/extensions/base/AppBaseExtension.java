package nexusvault.cli.extensions.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.extension.AbstractExtension;
import nexusvault.cli.core.extension.ExtensionInfo;

@AutoInstantiate
@ExtensionInfo(priority = Integer.MAX_VALUE)
public class AppBaseExtension extends AbstractExtension {

	private static final Logger logger = LogManager.getLogger(AppBaseExtension.class);

	@Override
	protected void initializeExtension(InitializationHelper initializationHelper) {

	}

	@Override
	protected void deinitializeExtension() {

	}

}
