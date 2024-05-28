package lu.mms.common.quality.userguide.dao;

import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * Interface not marked with '@mapper' annotation.
 */
public interface NonMapperBean {

    /**
     * find the customer name by ID.
     * @param id The customer ID.
     * @return the Optional customer name.
     */
    Optional<String> findCustomerNameById(@Param("id") Integer id);

}
