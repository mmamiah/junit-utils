package lu.mms.common.quality.junit.assets.mock.reinforcement;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Functional interface for the mock injection via field injection or setter injection.
 */
@FunctionalInterface
public interface MockReinforcementHandler {

    /**
     * Inject the mocks to provided fields, via field injection or setter injection.
     * @param needingInjection Fields needing mock injection
     * @param mocks The mocks to inject
     * @param ofInstance Instance owning the <code>field</code>
     */
    void injectMocksOnFields(Set<Field> needingInjection, Set<Object> mocks, Object ofInstance);

}
