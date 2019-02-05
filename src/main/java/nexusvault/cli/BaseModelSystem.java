package nexusvault.cli;

import java.util.HashMap;
import java.util.Map;

final class BaseModelSystem implements ModelSystem {

	private final Map<Class<? extends Object>, Object> store;

	public BaseModelSystem() {
		store = new HashMap<>();
	}

	@Override
	public <U> void registerModel(Class<? super U> modelClass, U model) throws ModelAlreadyRegisteredException {
		if (modelClass == null) {
			throw new IllegalArgumentException("'modelClass' must not be null");
		}
		if (Object.class.equals(modelClass)) {
			throw new IllegalArgumentException("'modelClass' must not be Object.class");
		}
		if (model == null) {
			throw new IllegalArgumentException("'model' must not be null");
		}

		if (store.containsKey(modelClass)) {
			throw new ModelAlreadyRegisteredException("Model for model class " + modelClass + " already defined");
		}

		store.put(modelClass, model);
	}

	@Override
	public <U> U getModel(Class<? extends U> modelClass) throws ModelNotFoundException {
		if (modelClass == null) {
			throw new IllegalArgumentException("'modelClass' must not be null");
		}
		if (Object.class.equals(modelClass)) {
			throw new IllegalArgumentException("'modelClass' must not be Object.class");
		}

		final Object m = store.get(modelClass);
		if (m == null) {
			throw new ModelNotFoundException("Model for model class " + modelClass + " not found");
		}

		return (U) m;
	}

	@Override
	public boolean hasModel(Class<?> modelClass) {
		return store.containsKey(modelClass);
	}

}
