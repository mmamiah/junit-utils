package lu.mms.common.quality.assets.spring.context;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
interface TestcaseMapper {

    /**
     * Find the customer name by ID.
     * @param id The customer ID
     * @return the customer name
     */
    @Select("SELECT LA_NAME FROM CUSTOMER WHERE ID=#{id}")
    String findCustomerNameById(@Param("id") Integer id);
}
