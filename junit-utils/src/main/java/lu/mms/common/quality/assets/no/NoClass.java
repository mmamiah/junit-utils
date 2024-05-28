package lu.mms.common.quality.assets.no;

/**
 * The {@link NoClass} purpose is to be used when a class declaration is mandatory by definition, but is not required
 * at moment. Typical example is in custom annotation, where a default value is required.
 * <pre>
 *     public @interface MyCustomAnnotation {
 *          Class&lt;?&gt; withSelectedClass() default {@link NoClass}.class;
 *     }
 * </pre>
 */
public interface NoClass {

}
