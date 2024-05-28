package lu.mms.common;

import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.testutils.ExtendWithTestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.MockUtil;
import org.reflections.ReflectionUtils;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

@ExtendWithTestUtils
class AppControllerTest {

    @InjectMocks
    private AppController sut;

    @Spy
    private ArgumentsService argumentsServiceSpy;

    @Mock
    private EmptyVerifier verifierMock;

    @Test
    void shouldEnsureSutMembersAreProperlyInitialized(final InternalMocksContext mocksContext) {
        // Arrange

        // Act
        final ArgumentsService service = sut.getArgumentsService();

        // Assert
        assertThat(service, equalTo(argumentsServiceSpy));

        final EmptyVerifier verifier = service.getVerifier();
        assertThat(verifier, equalTo(verifierMock));

        // ensure that @InjectMocks member is properly prepared
        assertNotNullMembers(mocksContext, AppController.class);
        // ensure that @Spy member is properly prepared
        assertNotNullMembers(mocksContext, ArgumentsService.class);
    }

    @Test
    void shouldEnsureSutValuesAreProperlyInitialized() {
        // Arrange

        // Act
        final ArgumentsService service = sut.getArgumentsService();

        // Assert
        assertThat(service, equalTo(argumentsServiceSpy));

        final EmptyVerifier verifier = service.getVerifier();
        assertThat(verifier, equalTo(verifierMock));
    }

    private static void assertNotNullMembers(final InternalMocksContext mocksContext, final Object target) {
        Class<?> targetClass = target.getClass();
        if (MockUtil.isMock(target)) {
            targetClass = MockUtil.getMockSettings(target).getTypeToMock();
        }
        ReflectionUtils.getAllFields(targetClass).stream()
                .filter(field -> mocksContext.findMockByField(field) != null)
                .forEach(field -> {
                    final Object member = ReflectionTestUtils.getField(target, field.getName());
                    assertThat(member, notNullValue());
                });
    }

}
