package nexusvault.cli.extensions.worker;

public interface StatusMonitor {

	void start();

	void processed(int completedTasks, int totalNumberOfTasks);

	void end();

}