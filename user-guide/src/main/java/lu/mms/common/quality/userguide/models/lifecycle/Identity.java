package lu.mms.common.quality.userguide.models.lifecycle;

import jakarta.annotation.PostConstruct;

/**
 * The identity.
 */
// tag::entity[]
public class Identity {

    private String id;

    @PostConstruct
    private void init() {
        this.id = "initial-id";
    }

    public String getId() {
        return id;
    }
}
// end::entity[]
