package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.WarehouseReceiveDetailDTO;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  采购收货单Mapper 接口
 * </p>
 *
 * @author Luo_WG
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

    void updateInfo(@Param("id")String id, @Param("inStockStatus")String inStockStatus);
    /**
     * 获取采购订单下收货单的收货数量汇总
     * @author will
     * @date 2026-03-30 11:35
     * @param podIdList
     * @return List<WarehouseReceiveDetailDTO.ReceiveQtyDTO>
     */
    List<WarehouseReceiveDetailDTO.ReceiveQtyDTO> getTotalReceiveQty(@Param("podIdList")List<String> podIdList);
    /**
     * 获取采购订单下收货单的待质检数量汇总
     * @author will
     * @date 2026-03-30 11:35
     * @param podIdList
     * @return List<WarehouseReceiveDetailDTO.WaitQcQtyDTO>
     */
    List<WarehouseReceiveDetailDTO.WaitQcQtyDTO> getTotalWaitQcQty(@Param("podIdList")List<String> podIdList);

}
