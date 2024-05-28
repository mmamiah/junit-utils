package lu.mms.common.quality.assets.mybatis;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

@Mapper
interface EntityMapper {

    /**
     * Find the customer name by ID.
     * @param id The customer ID
     * @return the customer name
     */
    @Select("SELECT LA_NAME FROM CUSTOMER WHERE ID=#{id}")
    String findCustomerNameById(@Param("id") Integer id);

    /**
     * Call the stored procedure to update the customer entity
     * @param id The customer ID
     */
    @Update("CALL UPDATE_CUSTOMER(#{id})")
    void updateTimeStampViaStoredProcedure(@Param("id") Integer id);

    /**
     * Insert a new customer with name.
     * @param id The customer ID
     * @param name The customer name
     * @return  true, if the entity has been inserted
     *          false, otherwise
     */
    @Insert("INSERT into CUSTOMER(ID, LA_NAME) VALUES(#{id}, #{name})")
    boolean insertCustomer(@Param("id") Integer id, @Param("name") String name);

    /**
     * Find the counter (updated by the trigger) name by ID.
     * @param id The customer ID
     * @return the customer name
     */
    @Select("SELECT LA_TRIGGER_COUNTER FROM CUSTOMER WHERE ID=#{id}")
    String findCustomerTriggerCountById(@Param("id") Integer id);

    /**
     * Find the update time name by ID.
     * @param id The customer ID
     * @return the customer name
     */
    @Select("SELECT TS_UPDATE FROM CUSTOMER WHERE ID=#{id}")
    Date findCustomerUpdateTimeById(@Param("id") Integer id);

}
