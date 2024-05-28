package lu.mms.common.quality.assets.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.testutils.ExtendWithTestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.MockUtil;
import org.reflections.ReflectionUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Test the bean lifecycle method execution when the {@link WithBeanLifeCycle#beforeEach()} == true. <br>
 */
@ExtendWithTestUtils
@WithBeanLifeCycle
class WithBeanLifeCycleWhenSutWithMockOrSpyTest {

    private static final List<LifeCycled> TO_CHECK = new ArrayList<>();

    @InjectMocks
    private Car sut;

    @Spy
    private Engine engineSpy;

    @Mock
    private Tyre tyreMock;

    @Mock
    private Manufacturer manufacturerMock;

    @AfterEach
    void registerMembers() {
        // register members for later destruction check (see @AfterAll method)
        TO_CHECK.addAll(List.of(sut, engineSpy, tyreMock));
    }

    @AfterAll
    static void assertAllObjectHaveBeenDestroyed() {
        TO_CHECK.forEach(item -> {
            try {
                if (MockUtil.isSpy(item)) {
                    // assert spies
                    verify(item).destroy();
                    verify(item).preDestroy();
                } else if (MockUtil.isMock(item)) {
                    // assert mocks
                    verify(item, never()).destroy();
                    verify(item, never()).preDestroy();
                } else if (item instanceof Car) {
                    final Car car = (Car) item;
                    assertThat(car.isDestroyExecuted(), equalTo(true));
                    assertThat(car.isPreDestroyExecuted(), equalTo(true));
                }
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        });
        TO_CHECK.clear();
    }

    @Test
    void shouldCallSutLifecycleMethods(final InternalMocksContext mocksContext) {
        // Arrange
        assumeTrue(sut.getEngine() != null);
        assumeTrue(sut.getTyre() != null);

        // Act
        // nothing to do as the method are called before and after the test

        // Assert
        assertThat(sut.isPostConstructExecuted(), equalTo(true));
        assertThat(sut.isAfterPropertiesSetExecuted(), equalTo(true));

        assertThat(sut.isDestroyExecuted(), equalTo(false));
        assertThat(sut.isPreDestroyExecuted(), equalTo(false));

        // ensure that all classes are properly prepared
        assertNotNullMembers(mocksContext, Car.class);
        assertNotNullMembers(mocksContext, Engine.class);
        assertNotNullMembers(mocksContext, Tyre.class);
        assertNotNullMembers(mocksContext, Manufacturer.class);
    }

    @Test
    void shouldCallSpyMemberLifecycleMethods(final InternalMocksContext mocksContext) throws Exception {
        // Arrange
        assumeTrue(sut.getEngine().equals(engineSpy));

        // Act
        // nothing to do as the method are called before and after the test

        // Assert
        // Ensure that spy method are executed
        verify(engineSpy).init();
        verify(engineSpy).afterPropertiesSet();

        verify(engineSpy, never()).destroy();
        verify(engineSpy, never()).preDestroy();

        // ensure that all classes are properly prepared
        assertNotNullMembers(mocksContext, Car.class);
        assertNotNullMembers(mocksContext, Engine.class);
        assertNotNullMembers(mocksContext, Tyre.class);
        assertNotNullMembers(mocksContext, Manufacturer.class);
    }

    @Test
    void shouldNeverCallMockMemberLifecycleMethods(final InternalMocksContext mocksContext) throws Exception {
        // Arrange
        assumeTrue(sut.getTyre().equals(tyreMock));

        // Act
        // nothing to do as the method are called before and after the test

        // Assert
        // Ensure that spy method are executed
        verify(tyreMock, never()).init();
        verify(tyreMock, never()).destroy();
        verify(tyreMock, never()).preDestroy();
        verify(tyreMock, never()).afterPropertiesSet();

        // ensure that all classes are properly prepared
        assertNotNullMembers(mocksContext, Car.class);
        assertNotNullMembers(mocksContext, Engine.class);
        assertNotNullMembers(mocksContext, Tyre.class);
        assertNotNullMembers(mocksContext, Manufacturer.class);
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

    private interface LifeCycled extends InitializingBean, DisposableBean {
        void init();
        void preDestroy();
    }

    private static class Car implements LifeCycled {
        private Engine engine;
        private Tyre tyre;
        private boolean postConstructExecuted = false;
        private boolean preDestroyExecuted = false;
        private boolean afterPropertiesSetExecuted = false;
        private boolean destroyExecuted = false;

        public Engine getEngine() {
            return engine;
        }

        public Tyre getTyre() {
            return tyre;
        }

        @PostConstruct
        public void init() {
            postConstructExecuted = true;
        }
        @PreDestroy
        public void preDestroy() {
            preDestroyExecuted = true;
        }
        @Override
        public void afterPropertiesSet() throws Exception {
            afterPropertiesSetExecuted = true;
        }
        @Override
        public void destroy() throws Exception {
            destroyExecuted = true;
        }

        public boolean isPostConstructExecuted() {
            return postConstructExecuted;
        }

        public boolean isPreDestroyExecuted() {
            return preDestroyExecuted;
        }

        public boolean isAfterPropertiesSetExecuted() {
            return afterPropertiesSetExecuted;
        }

        public boolean isDestroyExecuted() {
            return destroyExecuted;
        }
    }

    private static class Engine extends AbstractLifeCycled {
    }

    private static class Tyre extends AbstractLifeCycled {
    }

    /**
     * <b>Warning:</b> If this class will not be declared in out test and mocked (@Mock), the corresponding field
     * will not be recognized by the mocks context and therefore will not be injected in relevant
     * field (see BeanLifecycleExtension).
     */
    private static class Manufacturer {
        private static final String name = "default name";
    }

    private static abstract class AbstractLifeCycled implements LifeCycled {
        private Manufacturer manufacturer;
        private String description;
        @PostConstruct
        public void init(){
            description = manufacturer.toString(); // just to be alerted in case of NPE
        }
        @PreDestroy
        public void preDestroy(){
            description = manufacturer.toString(); // just to be alerted in case of NPE
        }
        @Override
        public void afterPropertiesSet() throws Exception {
            description = manufacturer.toString(); // just to be alerted in case of NPE
        }
        @Override
        public void destroy() throws Exception {
            description = manufacturer.toString(); // just to be alerted in case of NPE
        }
    }

}
