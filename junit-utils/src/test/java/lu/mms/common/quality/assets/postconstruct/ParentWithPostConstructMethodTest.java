package lu.mms.common.quality.assets.postconstruct;

import jakarta.annotation.PostConstruct;
import lu.mms.common.quality.assets.lifecycle.BeanLifeCycleExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

@ExtendWith({MockitoExtension.class, BeanLifeCycleExtension.class})
class ParentWithPostConstructMethodTest {

    private static final String PARENT_NAME = "I am the parent";
    private static final String CHILD_NAME = "I am the child";

    @InjectMocks
    private SubjectUnderTest sut;

    @Test
    void shouldInitParentAndChildClassesWhenPostConstructMethods() {
        // Arrange

        // Act
        final String result = sut.getName();

        // Assert
        assertThat(result, notNullValue());
        assertThat(result, equalTo(PARENT_NAME));
    }

    private static abstract class AbstractItemWithPostConstruct {
        private String name;

        @PostConstruct
        void parentPostConstruct() {
            name = PARENT_NAME;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

    private static class SubjectUnderTest extends AbstractItemWithPostConstruct{
        // concrete class.
    }

}
