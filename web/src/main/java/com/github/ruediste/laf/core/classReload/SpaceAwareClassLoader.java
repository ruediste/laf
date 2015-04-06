package com.github.ruediste.laf.core.classReload;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.google.common.io.ByteStreams;

public class SpaceAwareClassLoader extends ClassLoader {

	private Class<?> classSpace;
	private Set<Class<?>> allowedSpaces;
	private ClassSpaceCache cache;

	public SpaceAwareClassLoader(ClassLoader parent, Class<?> classSpace,
			ClassSpaceCache cache, Class<?>... allowedSpaces) {
		super(parent);
		this.classSpace = classSpace;
		this.allowedSpaces = new HashSet<>(Arrays.asList(allowedSpaces));
		this.cache = cache;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		if (!name.startsWith("java.")) {
			Class<?> space = cache.getClassSpace(name);
			if (classSpace.equals(space)) {
				Class<?> result;
				try (InputStream in = getResourceAsStream(name
						.replace('.', '/') + ".class")) {
					byte[] bb = ByteStreams.toByteArray(in);
					result = defineClass(name, bb, 0, bb.length);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				if (resolve) {
					resolveClass(result);
				}
				return result;
			}

			if (!allowedSpaces.contains(space)) {
				throw new ClassNotFoundException(
						"May not reference class in space "
								+ space.getSimpleName()
								+ " from class in space "
								+ classSpace.getSimpleName());
			}
		}

		Class<?> result = getParent().loadClass(name);
		if (resolve) {
			resolveClass(result);
		}
		return result;
	}
}