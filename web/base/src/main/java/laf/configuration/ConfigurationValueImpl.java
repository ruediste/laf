package laf.configuration;

import laf.configuration.ConfigurationParameter;
import laf.configuration.ConfigurationValue;

public class ConfigurationValueImpl<T extends ConfigurationParameter<?>>
		implements ConfigurationValue<T> {

	final private T value;

	public ConfigurationValueImpl(T value) {
		this.value = value;
	}

	@Override
	public T value() {
		return value;
	}

}
