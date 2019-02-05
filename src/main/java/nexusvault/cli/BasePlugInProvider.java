package nexusvault.cli;

import java.util.HashMap;
import java.util.Map;

final class BasePlugInProvider implements PlugInSystem {

	private final Map<Class<? extends PlugIn>, PlugIn> plugIns;

	public BasePlugInProvider() {
		plugIns = new HashMap<>();
	}

	@Override
	public void registerPlugIn(Class<? extends PlugIn> plugInClass, PlugIn plugIn) {
		if (plugInClass == null) {
			throw new IllegalArgumentException("'plugInClass' must not be null");
		}
		if (plugIn == null) {
			throw new IllegalArgumentException("'plugIn' must not be null");
		}

		if (plugIns.containsKey(plugInClass)) {
			throw new IllegalStateException("bla bla");
			// TODO throw exception
		}
		plugIns.put(plugInClass, plugIn);
	}

	@Override
	public <T extends PlugIn> boolean unregisterPlugIn(Class<T> plugInClass) {
		if (plugInClass == null) {
			throw new IllegalArgumentException("'plugInClass' must not be null");
		}
		return plugIns.remove(plugInClass) != null;
	}

	@Override
	public <T extends PlugIn> T getPlugIn(Class<T> plugInClass) {
		if (plugInClass == null) {
			throw new IllegalArgumentException("'plugInClass' must not be null");
		}

		// TODO will do for now
		final T service = (T) plugIns.get(plugInClass);
		if (service == null) {
			throw new IllegalStateException("meh");
			// TODO throw exception
		}
		return service;
	}

	@Override
	public boolean hasPlugIn(Class<? extends PlugIn> plugInClass) {
		if (plugInClass == null) {
			throw new IllegalArgumentException("'plugInClass' must not be null");
		}
		return plugIns.containsKey(plugInClass);
	}

}
