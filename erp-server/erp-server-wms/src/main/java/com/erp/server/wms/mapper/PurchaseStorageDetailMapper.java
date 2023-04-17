package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.PurchaseStockInDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 采购入库明细表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@Mapper
public interface PurchaseStorageDetailMapper extends BaseMapper<PurchaseStockInDetailEntity> {
    Integer getStockInQty(@Param("purchaseOrderDetailId") String purchaseOrderDetailId);
}
