package lu.mms.common.quality.userguide.fixture;

import lu.mms.common.quality.assets.fixture.Fixture;
import lu.mms.common.quality.userguide.models.Report;
import org.mockito.internal.util.MockUtil;

import static org.mockito.Mockito.when;

/**
 * My very nice fixture file.
 */
// tag::example[]
@Fixture
public class FixtureFileExample {

    private Report report;

    void givenCustomerIdIsTwenty() {
        if (MockUtil.isMock(report)) {
            when(report.getCustomerId()).thenAnswer(arg -> 20);
        }
    }
}
// end::example[]
