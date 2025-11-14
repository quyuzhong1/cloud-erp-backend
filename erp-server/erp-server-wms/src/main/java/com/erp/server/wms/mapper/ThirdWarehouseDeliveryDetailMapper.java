package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 三方仓发货单明细 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
 */
@Mapper
public interface ThirdWarehouseDeliveryDetailMapper extends BaseMapper<ThirdWarehouseDeliveryDetailEntity> {

    List<ThirdWarehouseDeliveryEntity> listWaitShipByWarehouseIds(@Param("warehouseIds") List<String> warehouseIds);
}
