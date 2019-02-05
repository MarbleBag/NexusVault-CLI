package nexusvault.cli;

public interface ModelSystem {

	public static final class ModelAlreadyRegisteredException extends NexusvaultCLIBaseException {

		private static final long serialVersionUID = -4506449228495224517L;

		public ModelAlreadyRegisteredException() {
			super();
		}

		public ModelAlreadyRegisteredException(String message) {
			super(message);
		}

		public ModelAlreadyRegisteredException(String message, Throwable cause) {
			super(message, cause);
		}

		public ModelAlreadyRegisteredException(Throwable cause) {
			super(cause);
		}
	}

	public static final class ModelNotFoundException extends NexusvaultCLIBaseException {

		private static final long serialVersionUID = 3774077083264107365L;

		public ModelNotFoundException() {
			super();
		}

		public ModelNotFoundException(String message) {
			super(message);
		}

		public ModelNotFoundException(String message, Throwable cause) {
			super(message, cause);
		}

		public ModelNotFoundException(Throwable cause) {
			super(cause);
		}
	}

	<U> void registerModel(Class<? super U> modelClass, U model) throws ModelAlreadyRegisteredException;

	<U> U getModel(Class<? extends U> modelClass) throws ModelNotFoundException;

	boolean hasModel(Class<?> modelClass);

}
