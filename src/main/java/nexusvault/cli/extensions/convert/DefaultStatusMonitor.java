package nexusvault.cli.extensions.convert;

import nexusvault.cli.core.App;
import nexusvault.cli.core.Console;
import nexusvault.cli.core.Console.Level;
import nexusvault.cli.extensions.worker.StatusMonitor;

public class DefaultStatusMonitor implements StatusMonitor {

	protected long startAt;
	protected long endAt;
	protected final Console console;

	public DefaultStatusMonitor() {
		this.console = App.getInstance().getConsole();
	}

	protected void sendMsg(String msg) {
		this.console.println(Level.CONSOLE, msg);
	}

	@Override
	public void start() {
		this.startAt = System.currentTimeMillis();
		sendMsg("Start converting.");
	}

	@Override
	public void processed(int completedTasks, int totalNumberOfTasks) {
		final float percentage = completedTasks / (totalNumberOfTasks + 0f) * 100;
		final String msg = String.format("Processed files %d of %d (%.2f%%).", completedTasks, totalNumberOfTasks, percentage);
		sendMsg(msg);
	}

	@Override
	public void end() {
		this.endAt = System.currentTimeMillis();
		sendMsg(String.format("Convert done in %.2fs", (this.endAt - this.startAt) / 1000f));
	}

}
