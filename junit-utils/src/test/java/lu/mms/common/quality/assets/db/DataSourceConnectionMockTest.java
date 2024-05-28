package lu.mms.common.quality.assets.db;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.util.MockUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DataSourceConnectionMockTest {

    private Connection sut;

    @BeforeEach
    void init() throws SQLException {
        sut = DataSourceMock.newH2Mock().build().getConnection();
    }

    @Test
    void shouldProvideResultSetWithSingleElementForEachCall() throws SQLException {
        // Arrange
        final String arg = RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(1, 10));
        final PreparedStatement preparedStatement = sut.prepareStatement(arg);
        final ResultSet resultSet = preparedStatement.executeQuery();

        // Act
        final boolean hasNext = resultSet.next();

        // Assert
        assertThat(hasNext, equalTo(true));

        /*
        Any further call to '.next()' should return false (No more record to move the cursor to) in order to avoid NPE.
        */
        assertThat(resultSet.next(), equalTo(false));
    }

    @Test
    void shouldNotHaveNextElementWhenConnectionIsClosed() throws SQLException {
        // Arrange
        final String arg = RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(1, 10));
        final PreparedStatement preparedStatement = sut.prepareStatement(arg);
        final ResultSet resultSet = preparedStatement.executeQuery();

        // Act
        resultSet.close();

        // Assert
        assertThat(resultSet.next(), equalTo(false));
    }

    @ParameterizedTest
    @MethodSource("preparedStatementArgumentProvider")
    void shouldReturnPreparedStatementWhenSingleArgPassed(final String arg1, final Object arg2) throws SQLException {
        // Arrange
        assumeTrue(MockUtil.isMock(sut));

        // Act
        final PreparedStatement preparedStatement;
        if (arg2 instanceof Integer) {
            preparedStatement = sut.prepareStatement(arg1, new int[]{(int)arg2});
        } else {
            preparedStatement = sut.prepareStatement(arg1, new String[]{(String)arg2});
        }

        // Assert
        assertThat(MockUtil.isMock(preparedStatement), equalTo(true));
        final ResultSet resultSet = preparedStatement.executeQuery();
        assertThat(MockUtil.isMock(resultSet), equalTo(true));


        final int argInt = RandomUtils.nextInt(1, 20);
        assertThat(resultSet.getString(argInt), notNullValue());

        final String argStr = RandomStringUtils.randomAlphanumeric(argInt);
        assertThat(resultSet.getString(argStr), notNullValue());
    }

    private static Stream<Arguments> preparedStatementArgumentProvider() {
        final int argInt = RandomUtils.nextInt(1, 10);
        final String argStr1 = RandomStringUtils.randomAlphanumeric(argInt);
        final String argStr2 = RandomStringUtils.randomAlphanumeric(argInt);
        return Stream.of(
                Arguments.arguments(argStr1, null),
                Arguments.arguments(argStr1, argStr2),
                Arguments.arguments(argStr1, argInt)
        );
    }

}
