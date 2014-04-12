package laf.module;

import java.lang.annotation.*;

/**
 * A Module is defined by putting this annotation to a class, which represents
 * the module.
 *
 * <p>
 * By default all classes in the package of the module representing class are
 * part of the module, excluding sub packages. All other classes are excluded.
 * This can be modified using the various include and exclude specifications.
 * All these specifications are turned into a pattern representation (see
 * below). Then each available class is evaluated against all patterns. The
 * include or exclude rule with the highest specificness score determines the
 * module membership. A single class may only be a member of a single module. It
 * is an error if an inclusion and an exclusion pattern with the same
 * specificness match a single class.
 * </p>
 *
 * <p>
 * Patterns are evaluated against the fully qualified name of a class. A single
 * star matches everything except a period. A double start matches everything
 * including periods. If a pattern starts with a period, the current package is
 * prepended. Examples
 * <dl>
 * <dt>foo.bar.App</dt>
 * <dd>Matches the class foo.bar.App</dd>
 * <dt>foo.bar.*</dt>
 * <dd>Matches all classes in the foo.bar package</dd>
 * <dt>**Test</dt>
 * <dd>Matches all classes in all packages ending with Test</dd>
 * <dt>foo.**Test</dt>
 * <dd>Matches all classes ending with Test, in any subpackage of foo</dd>
 * <dt>foo.*.*Test</dt>
 * <dd>Matches all classes ending with Test, in any direct subpackage of foo</dd>
 * <dt>foo.**.Test*</dt>
 * <dd>Matches all classes starting with Test, in any subpackage of foo</dd>
 * </dl>
 * </p>
 *
 * <p>
 * Patterns are associated with a specificness score. The score is calculated by
 * first splitting the pattern at each period. Each part can fall into one of
 * four categories, each associated with a base score: identifier (128), mixed
 * star(64), mixed double star(32), star (16), double star (8). The pattern
 * parts are processed from left to right. For each part the base score is added
 * to the specificness score. As soon as the first non-identifier is
 * encountered, all base scores are divided by two. By appending an exclamation
 * mark, the score is multiplied with 1000.
 * </p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Module {

	/**
	 * List of modules this module depends on. These modules are not exported.
	 * Classes of this module may only use classes from modules this module
	 * depends on.
	 */
	Class<?>[] depends() default {};

	/**
	 * List of modules which are exported to clients of this module. All listed
	 * modules are as well regarded as dependencies of this module. Exports are
	 * transitive. If module A exports module B and module B exports module C,
	 * clients of A also have a dependency on C.
	 */
	Class<?>[] exports() default {};

	/**
	 * Additional classes to include
	 */
	Class<?>[] include() default {};

	/**
	 * Additional packages to include. If a package starts with a dot, it is
	 * interpreted relative to the current package. If it ends with a star, all
	 * sub packages are included (use ".*" to include all sub packages of the
	 * current package)
	 */
	String[] includePattern() default {};

	/**
	 * Classes to exclude
	 */
	Class<?>[] exclude() default {};

	/**
	 * Packages to exclude. If a package starts with a dot, it is interpreted
	 * relative to the current package.
	 */
	String[] excludePattern() default {};
}
