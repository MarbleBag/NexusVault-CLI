package nexusvault.cli.extensions.convert;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface IsFactory {
	String id();

	int priority() default 1;

	String[] fileExtensions();

	String description() default "";

}
