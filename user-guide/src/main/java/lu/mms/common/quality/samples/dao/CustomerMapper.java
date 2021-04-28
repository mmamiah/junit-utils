package lu.mms.common.quality.samples.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Customer mapper.
 */
@Mapper
public interface CustomerMapper {

    /**
     * Find the customer name by ID.
     * @param id The customer ID
     * @return the customer name
     */
    @Select("SELECT LA_NAME FROM CUSTOMER WHERE ID=#{id}")
    String findCustomerNameById(@Param("id") Integer id);

    /**
     * Insert the customer in DB.
     * @param id the customer ID
     * @param name the customer name
     * @return the customer ID.
     */
    @Insert("INSERT INTO CUSTOMER (ID, LA_NAME) VALUES (#{id}, #{name})")
    Integer insertCustomer(@Param("id") Integer id, @Param("name") String name);

}
