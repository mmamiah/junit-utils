package lu.mms.common.quality.assets.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.test.context.event.annotation.BeforeTestExecution;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@ExtendWith(MockitoExtension.class)
@WithBeanLifeCycle
public class WithBeanLifeCycleBeforeTestExecutionTest {

    private static final Map<Integer, Car> TO_CHECK = new HashMap<>();

    @InjectMocks
    private Car sut;

    @BeforeEach
    void beforeEach() {
        assertThat(sut.isAfterPropertiesSetExecuted(), equalTo(false));
        assertThat(sut.isPostConstructExecuted(), equalTo(false));
    }

    @BeforeTestExecution
    void beforeTestExecution() {
        assertThat(sut.isAfterPropertiesSetExecuted(), equalTo(true));
        assertThat(sut.isAfterPropertiesSetExecuted(), equalTo(true));
    }

    @AfterEach
    void afterEach() {
        TO_CHECK.put(sut.getId(), sut);
        assertThat(sut.isDestroyExecuted(), equalTo(false));
        assertThat(sut.isPreDestroyExecuted(), equalTo(false));
    }

    @AfterAll
    static void afterAll() {
        TO_CHECK.values().forEach(sut -> {
            assertThat(sut.isDestroyExecuted(), equalTo(true));
            assertThat(sut.isPreDestroyExecuted(), equalTo(true));
        });
    }

    @Test
    void shouldNotCallSutLifecycleMethods() {
        // Arrange

        // Act
        // nothing to do as the method are called before and after the test

        // Assert
        assertThat(sut.isPostConstructExecuted(), equalTo(true));
        assertThat(sut.isAfterPropertiesSetExecuted(), equalTo(true));

        assertThat(sut.isDestroyExecuted(), equalTo(false));
        assertThat(sut.isPreDestroyExecuted(), equalTo(false));
    }

    private interface LifeCycled extends InitializingBean, DisposableBean {
        void init();
        void preDestroy();
    }

    private static class Car implements LifeCycled {
        private final int id;
        private boolean postConstructExecuted = false;
        private boolean preDestroyExecuted = false;
        private boolean afterPropertiesSetExecuted = false;
        private boolean destroyExecuted = false;

        public Car() {
            id = RandomUtils.nextInt(1, 100);
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
        public void afterPropertiesSet() {
            afterPropertiesSetExecuted = true;
        }
        @Override
        public void destroy() {
            destroyExecuted = true;
        }

        public int getId() {
            return id;
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

}
