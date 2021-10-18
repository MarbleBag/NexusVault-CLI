package nexusvault.cli.core;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.common.reflect.Reflection;

public final class ReflectionHelper {
	private ReflectionHelper() {
	}

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

	public static <T> List<T> initialize(List<Class<T>> classes) {
		Reflection.initialize(classes.toArray(Class<?>[]::new));
		return classes.stream().map(c -> initialize(c)).collect(Collectors.toList());
	}

	public static <T> T initialize(Class<? extends T> clazz) {
		try {
			final Constructor<? extends T> constructor = clazz.getDeclaredConstructor();
			final boolean wasAccessible = constructor.isAccessible();
			constructor.setAccessible(true);
			final T obj = constructor.newInstance();
			constructor.setAccessible(wasAccessible);
			return obj;
		} catch (InstantiationException | NoSuchMethodException | IllegalAccessException | SecurityException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new IllegalStateException("Unable to instantiate classes", e);
		}
	}

}
