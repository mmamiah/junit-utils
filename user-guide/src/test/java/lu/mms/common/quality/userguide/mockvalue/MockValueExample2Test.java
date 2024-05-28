package lu.mms.common.quality.userguide.mockvalue;

import lu.mms.common.quality.assets.mockvalue.MockValue;
import lu.mms.common.quality.assets.mockvalue.MockValueExtension;
import lu.mms.common.quality.userguide.models.mockvalue.Identity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

// tag::example[]
@ExtendWith({MockitoExtension.class, MockValueExtension.class})
class MockValueExample2Test {

    @InjectMocks
    private Identity sut;

    @MockValue("${identity-default-value}")
    private String idDefaultValue = "id_123";

    @Test
    void shouldFindTheIdWhenInitializedWithMockValue() {
        // Act / Assert
        assertThat(sut.getId(), equalTo(idDefaultValue));
    }
}
// end::example[]
