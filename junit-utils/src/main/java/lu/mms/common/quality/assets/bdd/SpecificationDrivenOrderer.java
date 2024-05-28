package lu.mms.common.quality.assets.bdd;

import org.apiguardian.api.API;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.MethodOrdererContext;

import static lu.mms.common.quality.assets.bdd.MethodNameComparatorUtils.getMethodDescriptorComparator;

/**
 * Specification Driven test method ordering.
 */
@API(
        status = API.Status.EXPERIMENTAL,
        since = "1.0.0"
)
public class SpecificationDrivenOrderer implements MethodOrderer {

    @Override
    public void orderMethods(final MethodOrdererContext methodOrdererContext) {
        methodOrdererContext.getMethodDescriptors().sort(getMethodDescriptorComparator());
    }

}
