package lu.mms.common.quality.assets;

/**
 * This functional interface implement the visitor pattern at lib asset level.
 * @param <T> The class to visit
 */
@FunctionalInterface
public interface AssetVisitor<T> {

    /**
     * @param element The element to visit.
     */
    void visit(T element);

}
