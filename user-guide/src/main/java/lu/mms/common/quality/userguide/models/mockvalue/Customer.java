package lu.mms.common.quality.userguide.models.mockvalue;

import org.springframework.beans.factory.annotation.Value;

/**
 * The customer.
 */
// tag::entity[]
public class Customer {

    @Value("${customer-mother-name}")
    private String motherName;

    @Value("${customer-father-name}")
    private String fatherName;

    public String getMotherName() {
        return motherName;
    }

    public String getFatherName() {
        return fatherName;
    }
}
// end::entity[]
