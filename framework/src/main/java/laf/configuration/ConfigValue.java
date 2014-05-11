package laf.configuration;

import java.lang.annotation.*;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * Marks a configuration value. The default value can be specified.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Qualifier
public @interface ConfigValue {
	/**
	 * Specifies the default value of this configuration parameter.
	 */
	@Nonbinding
	public String value() default "";
}
