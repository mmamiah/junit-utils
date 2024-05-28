package lu.mms.common.quality.userguide.lifecycle;

import lu.mms.common.quality.userguide.models.lifecycle.Identity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// tag::example[]
class BeanLifeCycleExtensionExample1Test {

    private final Identity sut = new Identity();

    @Test
    void shouldHaveValueInitializedWhenPostConstructExecuted() {
        // Arrange
        assumeTrue(sut.getId() == null);

        // Act
        ReflectionTestUtils.invokeMethod(sut, "init");

        // Assert
        assertThat(sut.getId(), notNullValue());
    }
}
// end::example[]
