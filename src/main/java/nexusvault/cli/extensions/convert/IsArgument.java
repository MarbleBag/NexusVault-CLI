package nexusvault.cli.extensions.convert;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
public @interface IsArgument {
	String description() default "";

	/**
	 * Integer.MAX_VALUE means any number is possible
	 *
	 * @return
	 */
	int numberOfArgs() default 1;

	String name();

}
