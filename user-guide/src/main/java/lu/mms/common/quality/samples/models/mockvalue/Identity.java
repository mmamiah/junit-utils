package lu.mms.common.quality.samples.models.mockvalue;

import org.springframework.beans.factory.annotation.Value;

/**
 * The identity.
 */
// tag::entity[]
public class Identity {

    @Value("${identity-default-value}")
    private String id;

    public String getId() {
        return id;
    }
}
// end::entity[]
