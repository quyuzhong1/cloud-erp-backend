package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  采购收货单Mapper 接口
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-06
 */
@Mapper
public interface WarehouseReceiveDetailMapper extends BaseMapper<WarehouseReceiveDetailEntity> {
    /**
     * @description: 根据采购订单明细id查询有效收货明细
     * @author Will
     * @date: 2023/4/19 10:01
     * @param purchaseDetailIds
     * @return List<WarehouseReceiveDetailEntity>
     */
    List<WarehouseReceiveDetailEntity> listWarehouseReceiveByPodIds(@Param("purchaseDetailIds") List<String> purchaseDetailIds);
}
