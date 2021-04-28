package lu.mms.common.quality.assets;

/**
 * The NONE purpose is to be used when a class declaration is mandatory by definition, but is not required at moment.
 * Typical example is in custom annotation, where a default value is required.
 * <pre>
 *     public @interface MyCustomAnnotation {
 *          Class&lt;?&gt; withSelectedClass() default None.class;
 *     }
 * </pre>
 */
public interface None {

}
