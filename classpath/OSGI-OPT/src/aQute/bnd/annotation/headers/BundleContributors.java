package aQute.bnd.annotation.headers;

import java.lang.annotation.*;

/**
 * Maven defines contributors and developers in the POM. This annotation will
 * generate a (not standardized by OSGi) Bundle-Contributors header.
 * <p>
 * This annotation can be used directly on a type or it can 'color' an
 * annotation. This coloring allows custom annotations that define a specific
 * contributor. For example:
 * 
 * <pre>
 *   {@code @}BundleContributor("Peter.Kriens@aQute.biz")
 *   {@code @}interface pkriens {}
 *   
 *   {@code @}pkriens
 *   public class MyFoo {
 *     ...
 *   }
 * </pre>
 * 
 * Duplicates are removed before the header is generated and the coloring does
 * not create an entry in the header, only an annotation on an actual type is
 * counted. This makes it possible to make a library of contributors without
 * then adding them all to the header.
 * <p>
 * See <a href=https://maven.apache.org/pom.html#Developers>Maven POM reference</a>
 */
@Retention(RetentionPolicy.CLASS)
@Target({
		ElementType.ANNOTATION_TYPE, ElementType.TYPE
})
public @interface BundleContributors {

	/**
	 * The email address of the developer.
	 */
	String value();

	/**
	 * The display name of the developer. If not specified, the {@link #value()}
	 * is used.
	 */
	String name() default "";

	/**
	 * The roles this contributor plays in the development.
	 */
	String[] roles() default {};

	/**
	 * The name of the organization where the contributor works for.
	 */
	String organization() default "";

	/**
	 * The url of the organization where the contributor works for.
	 */
	String organizationUrl() default "";

	/**
	 * Time offset in hours from UTC without Daylight savings
	 */
	int timezone() default 0;
}
