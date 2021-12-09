package nexusvault.cli.core;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.common.reflect.Reflection;

public final class ReflectionHelper {
	private ReflectionHelper() {
	}

	private static final Class<?>[] EMPTY_CLASSES = new Class<?>[0];

	@SuppressWarnings("unchecked")
	public static <T> List<Class<T>> findClasses(String packageName, Class<? extends T> scanForClass) throws IOException {
		final ClassPath classPathScanner = ClassPath.from(scanForClass.getClassLoader());
		final ImmutableSet<ClassInfo> allClasses = classPathScanner.getTopLevelClassesRecursive(packageName);

		final var filteredClasses = allClasses.stream().map(i -> i.load()).filter(c -> scanForClass.isAssignableFrom(c))
				.filter(c -> !Modifier.isInterface(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers())).collect(Collectors.toList());

		return (List<Class<T>>) filteredClasses;
	}

	public static <T> List<Class<? extends T>> filterByAnnotation(List<Class<? extends T>> classes, Class<? extends Annotation> annotation) {
		return classes.stream().filter(c -> c.getDeclaredAnnotation(annotation) != null).collect(Collectors.toList());
	}

	public static boolean isAssignableFrom(Class<?>[] types, Class<?>[] canAccept) {
		if (types.length != canAccept.length) {
			return false;
		}

		for (var i = 0; i < types.length; ++i) {
			if (!types[i].isAssignableFrom(canAccept[i])) {
				return false;
			}
		}

		return true;
	}

	public static <T extends Executable> T findMatchingExecutable(T[] executables, Class<?>... canAccept) {
		if (canAccept == null) {
			canAccept = EMPTY_CLASSES;
		}

		for (final var possibleMatch : executables) {
			final var supportedTypes = possibleMatch.getParameterTypes();
			if (isAssignableFrom(supportedTypes, canAccept)) {
				return possibleMatch;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> findMatchingConstructor(Class<T> clazz, Class<?>... canAccept) {
		return (Constructor<T>) findMatchingExecutable(clazz.getDeclaredConstructors(), canAccept);
	}

	public static <T> T initialize(Constructor<T> constructor, Object... args) {
		try {
			final boolean wasAccessible = constructor.isAccessible();
			constructor.setAccessible(true);
			final T obj = constructor.newInstance(args);
			constructor.setAccessible(wasAccessible);
			return obj;
		} catch (InstantiationException | IllegalAccessException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalArgumentException("Unable to instantiate classes", e);
		}
	}

	public static <T> List<T> initialize(List<Class<T>> classes) {
		Reflection.initialize(classes.toArray(Class<?>[]::new));
		return classes.stream().map(c -> {
			try {
				return initialize(c);
			} catch (final NoSuchMethodException e) {
				throw new IllegalArgumentException(e);
			}
		}).collect(Collectors.toList());
	}

	public static <T> T initialize(Class<? extends T> clazz, Object... args) throws NoSuchMethodException {
		final Class<?>[] expectedTypes = args != null ? Arrays.stream(args).map(e -> e.getClass()).toArray(Class<?>[]::new) : EMPTY_CLASSES;
		final Constructor<? extends T> constructor = findMatchingConstructor(clazz, expectedTypes);
		if (constructor == null) {
			throw new NoSuchMethodException(String.format("No constructor accepts: '%s'", Arrays.toString(expectedTypes)));
		}
		return initialize(constructor, args);
	}

}
