package lu.mms.common.quality.assets.no;

/**
 * The {@link NoEnum} purpose is to be used when a class declaration is mandatory by definition, but is not required at moment.
 * Typical example is in custom annotation, where a default value is required.
 * <pre>
 *     public @interface MyCustomAnnotation {
 *          Class&lt;? extends Enum&lt;?&gt;&gt; enums() default {@link NoEnum}.class;
 *     }
 * </pre>
 */
public enum NoEnum {

}
