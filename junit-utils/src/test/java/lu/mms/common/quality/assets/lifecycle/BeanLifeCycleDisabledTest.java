package lu.mms.common.quality.assets.lifecycle;

import lu.mms.common.quality.assets.unittest.UnitTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.MockUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@UnitTest(lifeCycle = false)
class BeanLifeCycleDisabledTest {

    private static final List<LifeCycled> TO_CHECK = new ArrayList<>();

    @InjectMocks
    private Car sut;

    @Spy
    private Engine engineSpy;

    @Mock
    private Tyre tyreMock;

    @AfterEach
    void registerMembers() {
        // register members for later destruction check (see @AfterAll method)
        TO_CHECK.addAll(List.of(sut, engineSpy, tyreMock));
    }

    @AfterAll
    static void assertAllObjectHaveBeenDestroyed() {
        TO_CHECK.forEach(item -> {
            try {
                if (MockUtil.isMock(item)) {
                    // assert mocks
                    verify(item, never()).destroy();
                    verify(item, never()).preDestroy();
                } else if (item instanceof Car) {
                    final Car car = (Car) item;
                    assertThat(car.isDestroyExecuted(), equalTo(false));
                    assertThat(car.isPreDestroyExecuted(), equalTo(false));
                }
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        });
        TO_CHECK.clear();
    }

    @Test
    void shouldNotCallSutLifecycleMethods() throws Exception {
        // Arrange
        assumeTrue(sut.getEngine() != null);
        assumeTrue(sut.getTyre() != null);

        // Act
        // nothing to do as the method are called before and after the test

        // Assert
        assertThat(sut.isPostConstructExecuted(), equalTo(false));
        assertThat(sut.isAfterPropertiesSetExecuted(), equalTo(false));

        assertThat(sut.isDestroyExecuted(), equalTo(false));
        assertThat(sut.isPreDestroyExecuted(), equalTo(false));
    }

    @Test
    void shouldNotCallSpyMemberLifecycleMethods() throws Exception {
        // Arrange
        assumeTrue(sut.getEngine().equals(engineSpy));

        // Act
        // nothing to do as the method are called before and after the test

        // Assert
        // Ensure that spy method are executed
        verify(engineSpy, never()).init();
        verify(engineSpy, never()).afterPropertiesSet();

        verify(engineSpy, never()).destroy();
        verify(engineSpy, never()).preDestroy();
    }

    @Test
    void shouldNeverCallMockMemberLifecycleMethods() throws Exception {
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

    private static class Engine implements LifeCycled {
        @PostConstruct
        public void init() {
        }
        @PreDestroy
        public void preDestroy() {
        }
        @Override
        public void afterPropertiesSet() throws Exception {
        }
        @Override
        public void destroy() throws Exception {
        }
    }

    private static class Tyre implements LifeCycled {
        @PostConstruct
        public void init() {
        }
        @PreDestroy
        public void preDestroy() {
        }
        @Override
        public void afterPropertiesSet() throws Exception {
        }
        @Override
        public void destroy() throws Exception {
        }
    }

}
