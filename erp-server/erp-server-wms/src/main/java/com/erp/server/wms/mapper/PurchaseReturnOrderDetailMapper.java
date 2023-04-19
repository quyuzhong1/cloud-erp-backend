package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 采购退货单明细 Mapper 接口
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-07
 */
@Mapper
public interface PurchaseReturnOrderDetailMapper extends BaseMapper<PurchaseReturnOrderDetailEntity> {
    /**
     * @description: 根据来源ids查询
     * @author Will
     * @date: 2023/4/19 11:13
     * @param sourceDetailIds
     * @return List<PurchaseReturnOrderDetailEntity>
     */
    List<PurchaseReturnOrderDetailEntity> listBySourceDetailIds(@Param("sourceDetailIds") List<String> sourceDetailIds);
}
