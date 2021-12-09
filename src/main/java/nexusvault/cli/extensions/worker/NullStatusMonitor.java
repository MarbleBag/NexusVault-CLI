package nexusvault.cli.extensions.worker;

public final class NullStatusMonitor implements StatusMonitor {

	@Override
	public void start() {
	}

	@Override
	public void processed(int completedTasks, int totalNumberOfTasks) {
	}

	@Override
	public void end() {
	}

}
