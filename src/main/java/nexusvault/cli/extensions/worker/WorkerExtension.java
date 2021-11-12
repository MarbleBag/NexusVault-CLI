package nexusvault.cli.extensions.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import nexusvault.cli.core.extension.AbstractExtension;

public final class WorkerExtension extends AbstractExtension {

	private ExecutorService threadPool;

	@Override
	protected void initializeExtension(InitializationHelper helper) {
		this.threadPool = Executors.newWorkStealingPool(); // runs as many threads as cores are available
	}

	@Override
	protected void deinitializeExtension() {
		this.threadPool.shutdownNow();
	}

	public void waitForWork(Collection<? extends Runnable> tasks, StatusMonitor callback) {
		callback.start();

		final var futures = tasks.parallelStream().map(task -> this.threadPool.submit(task)).collect(Collectors.toList());

		final var numberOfTasks = futures.size();
		final var reportAfterNTasks = Math.max(1, Math.min(500, numberOfTasks / 20));

		int processedTasks = 0;
		int reportIn = 0;
		while (!futures.isEmpty()) {
			final var iterator = futures.iterator();
			while (iterator.hasNext()) {
				final var future = iterator.next();
				if (future.isDone()) {
					iterator.remove();
					++reportIn;
				}
			}

			if (reportIn >= reportAfterNTasks) {
				processedTasks += reportIn;
				reportIn = 0;
				callback.processed(processedTasks, numberOfTasks);
			}

			if (futures.size() > 100) {
				try {
					Thread.sleep((int) (futures.size() * 0.2));
				} catch (final InterruptedException e) {
				}
			} else {
				Thread.yield();
			}
		}

		callback.processed(numberOfTasks, numberOfTasks);
		callback.end();
	}

	public static final class Report<T> {
		public static enum Status {
			SUCCESSFUL,
			ERROR,
			CANCELLED
		}

		private final T result;
		private final Status status;
		private final Exception error;

		public Report(Future<? extends T> future) {
			Status status = Status.SUCCESSFUL;
			Exception error = null;
			T result = null;

			try {
				result = future.get();
			} catch (final CancellationException e) {
				status = Status.CANCELLED;
			} catch (final InterruptedException | ExecutionException e) {
				status = Status.ERROR;
				error = e;
			}

			this.result = result;
			this.error = error;
			this.status = status;
		}

		public T getResult() {
			return this.result;
		}

		public Status getStatus() {
			return this.status;
		}

		public Exception getError() {
			return this.error;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Report<T>[] waitAndReportWork(Collection<? extends Callable<? extends T>> tasks, StatusMonitor callback) {

		callback.start();
		final var<Future<? extends T>> futures = new ArrayList<Future<? extends T>>(tasks.size());
		for (final var task : tasks) {
			futures.add(this.threadPool.submit(task));
		}

		final var result = new Report[futures.size()];
		final var<Integer> indices = new LinkedList<Integer>();
		for (var i = 0; i < result.length; ++i) {
			indices.add(Integer.valueOf(i));
		}

		final var numberOfTasks = futures.size();
		final var reportAfterNTasks = Math.max(1, Math.min(500, numberOfTasks / 20));

		int processedTasks = 0;
		int reportIn = 0;
		while (!indices.isEmpty()) {
			final var<Integer> iterator = indices.iterator();
			while (iterator.hasNext()) {
				final var index = iterator.next().intValue();
				final var future = futures.get(index);
				if (future.isDone()) {
					iterator.remove();
					++reportIn;
					result[index] = new Report<T>(future);
				}
			}

			if (reportIn >= reportAfterNTasks) {
				processedTasks += reportIn;
				reportIn = 0;
				callback.processed(processedTasks, numberOfTasks);
			}

			if (futures.size() > 100) {
				try {
					Thread.sleep((int) (futures.size() * 0.2));
				} catch (final InterruptedException e) {
				}
			} else {
				Thread.yield();
			}
		}

		callback.processed(numberOfTasks, numberOfTasks);
		callback.end();

		return result;
	}

}
