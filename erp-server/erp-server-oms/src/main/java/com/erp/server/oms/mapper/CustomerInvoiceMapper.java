package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.oms.entity.CustomerInvoiceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 客户发票信息 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface CustomerInvoiceMapper extends BaseMapper<CustomerInvoiceEntity> {

}
