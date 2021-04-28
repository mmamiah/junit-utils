package lu.mms.common.quality.samples.assets.reinforcement;

import lu.mms.common.quality.assets.mock.reinforcement.ReinforceMockExtension;
import lu.mms.common.quality.samples.models.Customer;
import lu.mms.common.quality.samples.models.Identity;
import lu.mms.common.quality.samples.models.Report;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

// tag::example[]
@ExtendWith({MockitoExtension.class, ReinforceMockExtension.class})
class ReinforceMockExtensionExampleTest {

    @Mock
    private Report reportMock;

    @Mock
    private Customer customerMock;

    @Mock
    private Identity identityMock;

    @Test
    void shouldConfirmThatReportMockPropertiesAreAllDefaultedWenRelevant() {
        // Arrange

        // Act
        final Customer customer = (Customer) ReflectionTestUtils.getField(reportMock,"customer");

        // Assert
        assertThat(customer, notNullValue());
        assertThat(customer, equalTo(customerMock));
        assertThat(MockUtil.isMock(customer), equalTo(true));
    }

    @Test
    void shouldConfirmThatMockPropertiesAreAllDefaultedWenRelevant() {
        // Arrange

        // Act
        final Identity identity = (Identity) ReflectionTestUtils.getField(customerMock,"identity");

        // Assert
        assertThat(identity, notNullValue());
        assertThat(identity, equalTo(identityMock));
        assertThat(MockUtil.isMock(identity), equalTo(true));
    }
// end::example[]

}
