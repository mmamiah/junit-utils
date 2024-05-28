package lu.mms.common.quality.userguide.mockvalue;

import lu.mms.common.quality.assets.mockvalue.MockValue;
import lu.mms.common.quality.assets.mockvalue.MockValueExtension;
import lu.mms.common.quality.userguide.models.mockvalue.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

// tag::example[]
@ExtendWith({MockitoExtension.class, MockValueExtension.class})
class MockValueExample4Test {

    @InjectMocks
    private Customer sut;

    @MockValue(
        value = {"${customer-mother-name}", "${customer-father-name}"},
        testcase = "shouldFindTheIdWhenInitializedWithMockValue"
    )
    private String familyName = "no_name";

    @Test
    void shouldFindTheIdWhenInitializedWithMockValue() {
        // Act / Assert
        assertThat(sut.getFatherName(), equalTo(familyName));
        assertThat(sut.getMotherName(), equalTo(familyName));
    }

    @Test
    void shouldNotInitPropertyWhenTestcaseNotMatching() {
        // Act / Assert
        assertThat(sut.getFatherName(), nullValue());
        assertThat(sut.getMotherName(), nullValue());
    }
}
// end::example[]
