package lu.mms.common.quality.userguide.lifecycle;

import lu.mms.common.quality.assets.lifecycle.BeanLifeCycleExtension;
import lu.mms.common.quality.userguide.models.lifecycle.Identity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

// tag::example[]
@ExtendWith(BeanLifeCycleExtension.class)
class BeanLifeCycleExtensionExample2Test {

    private final Identity sut = new Identity();

    @Test
    void shouldHaveValueInitializedWhenPostConstructExecuted() {
        // Ac / Assert
        assertThat(sut.getId(), notNullValue());
    }
}
// end::example[]
