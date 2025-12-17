package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.oms.entity.CustomerAddressEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 客户地址信息 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface CustomerAddressMapper extends BaseMapper<CustomerAddressEntity> {

    /**
     * 客户名称搜索
     * @param customerName
     * @return
     */
    List<CustomerAddressEntity> listByCustomerName(@Param("customerName") String customerName);


    List<CustomerAddressEntity> listAllByMainIds(@Param("mainIds") List<String> mainIds);
}
