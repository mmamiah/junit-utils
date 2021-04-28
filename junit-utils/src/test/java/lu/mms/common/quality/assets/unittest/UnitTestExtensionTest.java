package lu.mms.common.quality.assets.unittest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitTestExtensionTest {

    private final UnitTestExtension sut = new UnitTestExtension();

    @Mock
    private ConsoleAppender<ILoggingEvent> consoleAppenderMock;

    @Captor
    private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

    @Mock
    private ExtensionContext extensionContextMock;

    @BeforeEach
    void init(final TestInfo testInfo) {
        final Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(consoleAppenderMock);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInfo.getTestMethod().orElse(null));
    }

    @Test
    void shouldLogTheMethodExecutionStart(final TestInfo testInfo) {
        // Act
        sut.beforeTestExecution(extensionContextMock);

        // Assert
        verify(consoleAppenderMock).doAppend(loggingEventCaptor.capture());

        final LoggingEvent msg = loggingEventCaptor.getValue();
        assertThat(msg.getFormattedMessage(), containsString(testInfo.getTestMethod().orElse(null).getName()));
        assertThat(msg.getLevel(), equalTo(Level.INFO));
    }

    @Test
    void shouldLogTheMethodExecutionEnd(final TestInfo testInfo) {
        // Act
        sut.afterTestExecution(extensionContextMock);

        // Assert
        verify(consoleAppenderMock).doAppend(loggingEventCaptor.capture());

        final LoggingEvent msg = loggingEventCaptor.getValue();
        assertThat(msg.getFormattedMessage(), containsString(testInfo.getTestMethod().orElse(null).getName()));
        assertThat(msg.getLevel(), equalTo(Level.INFO));
    }

}
