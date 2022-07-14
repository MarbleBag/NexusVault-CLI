package nexusvault.cli.extensions.base;

import java.nio.file.Files;
import java.nio.file.Path;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.exception.FileNotFoundException;
import nexusvault.cli.core.exception.FileNotReadableException;
import nexusvault.cli.core.extension.AbstractExtension;
import nexusvault.cli.core.extension.ExtensionInfo;

@AutoInstantiate
@ExtensionInfo(priority = Integer.MAX_VALUE)
public class AppBaseExtension extends AbstractExtension {

	@Override
	protected void initializeExtension(InitializationHelper initializationHelper) {

	}

	@Override
	protected void deinitializeExtension() {

	}

	public AppBaseExtension() {
		// final List<Command> cmds = new ArrayList<>();
		// cmds.add(new ConfigPathCmd());
		// cmds.add(new ConfigReloadCmd());
		// cmds.add(new ConfigNoSaveCmd());
		// cmds.add(new OutputPathCmd());
		// cmds.add(new DebugModeCmd());
		// setCommands(cmds);

		// setCommands(//
		// new OutputPathHandle()//
		// );
		//
		// setArguments( //
		// new AppRootDirectory(), //
		// new OutputPathHandle(), //
		// new AppConfigPath()//
		// );
	}

	public void setApplicationPath(Path value) {
		App.getInstance().getAppConfig().setApplicationPath(value);
	}

	public boolean getDebugMode() {
		return App.getInstance().getAppConfig().getDebugMode();
	}

	public void setDebugMode(boolean value) {
		App.getInstance().getAppConfig().setDebugMode(value);
	}

	public void setOutputPath(Path path) {
		// App.getInstance().getAppConfig().setOutputPath(path);
	}

	public void setConfigNotSaveable(boolean b) {
		// TODO Auto-generated method stub

	}

	public void setHeadlessMode() {
		App.getInstance().getAppConfig().setHeadlessMode(true);
	}

	public void setConfigPath(Path newPath) {
		if (!Files.exists(newPath)) {
			throw new FileNotFoundException("Config file not found: " + newPath);
		}

		if (!Files.isReadable(newPath)) {
			throw new FileNotReadableException("Config file not readable: " + newPath);
		}

		// App.getInstance().getAppConfig().setConfigPath(newPath);

		// TODO after verification, load config

		// final AppModel model = App.getInstance().getAppModel();
		// final Path oldPath = model.getConfigPath();
		// model.setConfigPath(newPath);
		// App.getInstance().getEventSystem().postEvent(new ConfigPathChangedEvent(oldPath, newPath));
	}

	public void reloadConfig() {
		// TODO Auto-generated method stub

	}

}
