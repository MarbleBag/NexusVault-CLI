package nexusvault.cli.model;

import kreed.util.property.PropertyDefaultProvider;
import kreed.util.property.PropertyMapper;
import kreed.util.property.PropertyValidator;
import kreed.util.property.validator.PropertyIsNotNull;
import kreed.util.property.validator.PropertyIsOfType;
import kreed.util.property.validator.ValidatorChain;

public final class PropertyOption<T extends PropertyKey<T>> {
	private final String code;

	private final boolean saveable;

	private final PropertyValidator validator;
	private final PropertyDefaultProvider<T, ?> provider;
	private final PropertyMapper mapper;

	public PropertyOption(String code, boolean saveable, Class<?> type) {
		this(code, saveable, new ValidatorChain(new PropertyIsNotNull(), new PropertyIsOfType(type)));
	}

	public PropertyOption(String code, boolean saveable, PropertyValidator validator) {
		this(code, saveable, validator, null);
	}

	public PropertyOption(String code, boolean saveable, Class<?> type, PropertyDefaultProvider<T, ?> provider) {
		this(code, saveable, new ValidatorChain(new PropertyIsNotNull(), new PropertyIsOfType(type)), provider);
	}

	public PropertyOption(String code, boolean saveable, PropertyValidator validator, PropertyDefaultProvider<T, ?> provider) {
		this(code, saveable, validator, provider, null);
	}

	public PropertyOption(String code, boolean saveable, Class<?> type, PropertyDefaultProvider<T, ?> provider, PropertyMapper mapper) {
		this(code, saveable, new ValidatorChain(new PropertyIsNotNull(), new PropertyIsOfType(type)), provider, mapper);
	}

	public PropertyOption(String code, boolean saveable, PropertyValidator validator, PropertyDefaultProvider<T, ?> provider, PropertyMapper mapper) {
		if (code == null) {
			throw new IllegalArgumentException("'code' must not be null");
		}

		this.code = code;
		this.saveable = saveable;
		this.validator = validator;
		this.provider = provider;
		this.mapper = mapper;
	}

	public String getCode() {
		return code;
	}

	public boolean isSaveable() {
		return saveable;
	}

	public PropertyValidator getValidator() {
		return validator;
	}

	public PropertyDefaultProvider<T, ?> getProvider() {
		return provider;
	}

	public PropertyMapper getMapping() {
		return mapper;
	}
}