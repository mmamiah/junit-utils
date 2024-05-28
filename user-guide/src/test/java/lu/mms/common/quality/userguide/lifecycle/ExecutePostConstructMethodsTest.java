package lu.mms.common.quality.userguide.lifecycle;

import jakarta.annotation.PostConstruct;
import lu.mms.common.quality.assets.lifecycle.BeanLifeCycleExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@ExtendWith({MockitoExtension.class, BeanLifeCycleExtension.class})
class ExecutePostConstructMethodsTest {

    private final Doctor doctor = new Doctor();
    private final Nurse nurse = new Nurse();

    @Mock
    private Friend friendAnnotatedMock;

    private Friend friendManualMock;

    private Friend friendNull;

    @BeforeEach
    void init() {
        friendManualMock = mock(Friend.class);
    }

    @Test
    void shouldRunThePostConstructMethod() {
        // Act
        final String name = doctor.getName();

        // Assert
        assertThat(name, notNullValue());
    }

    @Test
    void shouldRunAllAnnotatedMethodwhenRelevant() {
        // Act
        final String name = doctor.getName();
        final String skills = nurse.getSkills();
        final Integer age = nurse.getAge();

        // Assert
        assertThat(name, notNullValue());
        assertThat(skills, notNullValue());
        assertThat(age, notNullValue());
    }

    @Test
    void shouldDoNothingWhenTargetIsAnnotatedMock() {
        // Act
        final String addressOne = friendAnnotatedMock.getAddress();

        // Assert
        assertThat(addressOne, nullValue());
        verify(friendAnnotatedMock, never()).initAddress();
    }

    @Test
    void shouldDoNothingWhenTargetIsManualMock() {
        // Act
        final String addressTwo = friendManualMock.getAddress();

        // Assert
        assertThat(addressTwo, nullValue());
        verify(friendManualMock, never()).initAddress();
    }

    @Test
    void shouldDoNothingWhenNotListedInPostConstruct() {
        // Act
        final Exception exception = assertThrows(NullPointerException.class, () -> friendNull.getAddress());

        // Assert
        assertThat(exception, notNullValue());
    }

    // single post construct method
    static class Doctor {
        private String name;

        @PostConstruct
        private void init() {
            this.name = "Dr John";
        }

        public String getName() {
            return name;
        }
    }

    // Multiple post construct methods
    static class Nurse {
        private String skills;
        private Integer age;

        @PostConstruct
        private void initName() {
            this.skills = "Level 5";
        }

        @PostConstruct
        private void initAge() {
            this.age = 10;
        }

        String getSkills() {
            return skills;
        }

        Integer getAge() {
            return age;
        }
    }

    // bean to be mocked or NULL. the goal is to not execute <@PostConstruct> method
    static class Friend {
        private String address;

        @PostConstruct
        void initAddress() {
            throw new UnsupportedOperationException("This method should not be executed");
        }

        String getAddress() {
            return address;
        }
    }

}
