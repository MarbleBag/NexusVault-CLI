package nexusvault.cli.extensions.convert;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ConversionResult {
	public static enum Status {
		PENDING,
		SUCCESSFUL,
		FAILED,
	}

	private final ConversionRequest request;
	private final Set<Path> createdFiles = new HashSet<>();
	private ConversionResult.Status status;
	private Throwable error;

	public ConversionResult(ConversionRequest request) {
		this.request = request;
		this.status = Status.PENDING;
	}

	protected void setError(Throwable error) {
		this.error = error;
		this.status = Status.FAILED;
	}

	protected void setSuccess() {
		this.status = Status.SUCCESSFUL;
	}

	public ConversionRequest getRequest() {
		return this.request;
	}

	public Throwable getError() {
		return this.error;
	}

	public ConversionResult.Status getStatus() {
		return this.status;
	}

	public boolean isSuccessful() {
		return Status.SUCCESSFUL == this.status;
	}

	public boolean isFailed() {
		return Status.FAILED == this.status;
	}

	public Set<Path> getCreatedFiles() {
		return Collections.unmodifiableSet(this.createdFiles);
	}

	protected void setOutput(Set<Path> createdOutputs) {
		this.createdFiles.addAll(createdOutputs);
	}
}