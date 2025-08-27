package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.entity.VirtualInventoryDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 虚拟仓库明细 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Mapper
public interface VirtualInventoryDetailMapper extends BaseMapper<VirtualInventoryDetailEntity> {

    /**
     * 根据出库查询
     * @author will
     * @date 2024/12/10 16:27
     * @param skuId
     * @param warehouseId
     * @param virtualWarehouseId
     * @return List<VirtualInventoryDetailEntity>
     */
    List<VirtualInventoryDetailEntity> getByOutParam(@Param("skuId")String skuId,@Param("warehouseId") String warehouseId,@Param("virtualWarehouseId") String virtualWarehouseId);
    /**
     * 按SKU查询库龄差异
     * @author will
     * @date 2025/2/20 11:51
     * @return java.util.List<com.erp.model.wms.dto.VirtualInventoryAgeDTO.SendNoticeSkuDTO>
     */
    List<VirtualInventoryAgeDTO.SendNoticeSkuDTO> listDiffSkuSendNotice();
    /**
     * 按汇总查询库龄差异
     * @author will
     * @date 2025/2/20 11:51
     * @return java.util.List<com.erp.model.wms.dto.VirtualInventoryAgeDTO.SendNoticeTotalDTO>
     */
    List<VirtualInventoryAgeDTO.SendNoticeTotalDTO> listDiffTotalSendNotice();
}
