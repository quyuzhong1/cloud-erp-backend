package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 采购订单表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrderEntity> {

}
