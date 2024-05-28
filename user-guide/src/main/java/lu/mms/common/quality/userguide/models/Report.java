package lu.mms.common.quality.userguide.models;

/**
 * The report class.
 */
// tag::entity[]
public class Report {

    private Customer customer;

    public Integer getCustomerId() {
        return customer.getIdentity().getId();
    }

    public Customer getCustomer() {
        return customer;
    }
}
// end::entity[]
