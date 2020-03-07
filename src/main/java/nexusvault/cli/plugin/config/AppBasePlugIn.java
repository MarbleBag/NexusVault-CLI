package nexusvault.cli.plugin.config;

import java.nio.file.Files;
import java.nio.file.Path;

import nexusvault.cli.App;
import nexusvault.cli.core.exception.FileNotFoundException;
import nexusvault.cli.core.exception.FileNotReadableException;
import nexusvault.cli.plugin.AbstractPlugIn;

public class AppBasePlugIn extends AbstractPlugIn {

	public AppBasePlugIn() {
		// final List<Command> cmds = new ArrayList<>();
		// cmds.add(new ConfigPathCmd());
		// cmds.add(new ConfigReloadCmd());
		// cmds.add(new ConfigNoSaveCmd());
		// cmds.add(new OutputPathCmd());
		// cmds.add(new DebugModeCmd());
		// setCommands(cmds);

		setCommands(//
				new OutputPathHandle()//
		);

		setArguments( //
				new AppPathArgHandler(), //
				new OutputPathHandle(), //
				new ConfigPathArgHandler()//
		);
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
		App.getInstance().getAppConfig().setOutputPath(path);
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

		App.getInstance().getAppConfig().setConfigPath(newPath);

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
