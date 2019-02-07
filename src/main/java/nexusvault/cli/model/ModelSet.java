package nexusvault.cli.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import kreed.util.property.BasePropertySet;
import kreed.util.property.PropertyDefaultProvider;
import kreed.util.property.PropertyListener;
import kreed.util.property.PropertySet;

public class ModelSet<T extends PropertyKey<T>> {

	private final PropertySet<T> properties;
	private final PropertySet<T> saveProperties;

	public ModelSet(List<T> keys) {
		properties = new BasePropertySet<>();
		saveProperties = new BasePropertySet<>();

		for (final T key : keys) {
			defineNewProperty(key);
		}
	}

	public void defineNewProperty(T key) throws PropertyAlreadyDefinedException {
		final PropertySet<T> set = getPropertySet(key);
		if (set.hasProperty(key)) {
			throw new PropertyAlreadyDefinedException(key.toString());
		}

		set.setPropertyValidator(key, key.getOptions().getValidator());
		set.setPropertyProvider(key, key.getOptions().getProvider());
		set.setPropertyMapping(key, key.getOptions().getMapping());
	}

	public void setPropertyProvider(T type, PropertyDefaultProvider<T, ?> provider) {
		getPropertySet(type).setPropertyProvider(type, provider);
	}

	private PropertySet<T> getPropertySet(T type) {
		return type.getOptions().isSaveable() ? saveProperties : properties;
	}

	public <U> U getProperty(T type) {
		final PropertySet<T> collection = getPropertySet(type);
		return collection.getProperty(type);
	}

	public boolean hasProperty(T type) {
		final PropertySet<T> collection = getPropertySet(type);
		return collection.hasProperty(type);
	}

	public boolean isPropertySet(T type) {
		final PropertySet<T> collection = getPropertySet(type);
		return collection.isPropertySet(type);
	}

	public boolean setProperty(T type, Object value) {
		return getPropertySet(type).setProperty(type, value);
	}

	public boolean clearProperty(T type) {
		return getPropertySet(type).clearProperty(type);
	}

	public Set<T> getAllConfigKeys() {
		final Set<T> set = new HashSet<>();
		set.addAll(properties.getKeys());
		set.addAll(saveProperties.getKeys());
		return set;
	}

	public void setListener(PropertyListener<T> listener) {
		properties.setListener(listener);
		saveProperties.setListener(listener);
	}

	public void removeListener() {
		properties.removeListener();
		saveProperties.removeListener();
	}

	public PropertyListener<T> getListener() {
		return properties.getListener();
	}

	public void saveSet() {
		final JsonObject base = Json.object();
		final Set<T> keys = getAllConfigKeys();

		for (final T key : keys) {
			final PropertyOption<T> opt = key.getOptions();
			if (!opt.isSaveable()) {
				continue;
			}
			if (!isPropertySet(key)) {
				continue;
			}

			// if(Path.class.equals(opt.)) {
			//
			// }
		}
	}

}
