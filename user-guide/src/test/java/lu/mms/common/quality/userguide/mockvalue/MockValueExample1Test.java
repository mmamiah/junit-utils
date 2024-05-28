package lu.mms.common.quality.userguide.mockvalue;

import lu.mms.common.quality.userguide.models.mockvalue.Identity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// tag::example[]
class MockValueExample1Test {

    private final Identity sut = new Identity();

    @Test
    void shouldFindIdWhenIdManuallySet() {
        // Arrange
        assumeTrue(sut.getId() == null);

        final String id = "id_123";
        ReflectionTestUtils.setField(sut, "id", id);

        // Act / Assert
        assertThat(sut.getId(), equalTo(id));
    }
}
// end::example[]
