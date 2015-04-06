package com.github.ruediste.laf.core.base.configuration;

import java.util.*;

import javax.inject.Inject;

import com.google.common.reflect.TypeToken;
import com.google.inject.Injector;

/**
 * Service for parsing configuration values from strings.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ConfigurationValueParsingService {
	/**
	 * Parse the provided string. If the string cannot be parsed an error is
	 * thrown.
	 */
	public <V> V parse(TypeToken<V> targetType, String value) {
		Class<?> rawType = targetType.getRawType();

		if (rawType.equals(List.class)) {
			return (V) new ArrayList(parseElements(
					targetType.resolveType(List.class.getTypeParameters()[0]),
					value));
		} else if (rawType.equals(Deque.class)) {
			return (V) new ArrayDeque(parseElements(
					targetType.resolveType(Deque.class.getTypeParameters()[0]),
					value));
		} else {
			return parseSingleValue(targetType, value);
		}
	}

	private <T> Collection<T> parseElements(TypeToken<T> elementType,
			String value) {
		int comma = ",".codePointAt(0);
		int backSlash = "\\".codePointAt(0);
		ArrayList<T> result = new ArrayList<>();

		StringBuilder sb = new StringBuilder();
		final int length = value.length();
		for (int offset = 0; offset < length;) {
			int codepoint = value.codePointAt(offset);
			if (codepoint == backSlash) {
				offset += Character.charCount(codepoint);
				if (offset >= value.length()) {
					throw new RuntimeException(
							"unfinished escape sequence at the end of " + value);
				}
				codepoint = value.codePointAt(offset);
				if (codepoint == comma) {
					sb.appendCodePoint(comma);
				} else if (codepoint == backSlash) {
					sb.appendCodePoint(backSlash);
				} else {
					throw new RuntimeException("Unknown escape sequence \""
							+ String.valueOf(Character.toChars(codepoint)));
				}
			} else if (codepoint == comma) {
				result.add(parseSingleValue(elementType, sb.toString().trim()));
				sb.setLength(0);
			} else {
				sb.appendCodePoint(codepoint);
			}
			offset += Character.charCount(codepoint);
		}
		result.add(parseSingleValue(elementType, sb.toString().trim()));

		return result;
	}

	@Inject
	Injector injector;

	private <T> T parseSingleValue(TypeToken<T> targetType, String value) {
		Class<?> rawType = targetType.getRawType();
		if (rawType.equals(String.class)) {
			return (T) value;
		}

		if (rawType.equals(Short.class)) {
			return (T) Short.valueOf(value);
		}
		if (rawType.equals(Integer.class)) {
			return (T) Integer.valueOf(value);
		}
		if (rawType.equals(Long.class)) {
			return (T) Long.valueOf(value);
		}

		if (rawType.equals(Double.class)) {
			return (T) Double.valueOf(value);
		}
		if (rawType.equals(Float.class)) {
			return (T) Float.valueOf(value);
		}

		// try to load the class using CDI
		{
			ClassLoader classLoader = Thread.currentThread()
					.getContextClassLoader();

			try {
				Class<?> objectClass = classLoader.loadClass(value);
				return (T) injector.getInstance(objectClass);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Unable to load class " + value
						+ " as configuration value", e);
			}
		}
	}
}