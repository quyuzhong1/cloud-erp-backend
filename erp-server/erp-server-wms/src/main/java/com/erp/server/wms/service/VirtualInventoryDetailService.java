package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.dto.VirtualInventoryDetailDTO;
import com.erp.model.wms.entity.VirtualInventoryDetailEntity;

import java.util.List;

/**
 * <p>
 * 虚拟仓库明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
public interface VirtualInventoryDetailService extends SuperService<VirtualInventoryDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-12-03
    * @param addOrUpdateDTO
    * @return
    */
    VirtualInventoryDetailEntity addOrUpdate(VirtualInventoryDetailDTO.UpdateDTO addOrUpdateDTO);

    /**
     * 根据出库参数查询
     * @author will
     * @date 2024/12/10 16:24
     * @param skuId
     * @param warehouseId
     * @param virtualWarehouseId
     * @return List<VirtualInventoryDetailEntity>
     */
    List<VirtualInventoryDetailEntity> getByOutParam(String skuId, String warehouseId, String virtualWarehouseId);

    /**
     * 按SKU查询库龄差异
     * @author will
     * @date 2025/2/19 19:24
     * @return java.util.List<com.erp.model.wms.dto.VirtualInventoryAgeDTO.SendNoticeSkuDTO>
     */
    List<VirtualInventoryAgeDTO.SendNoticeSkuDTO> listDiffSkuSendNotice();
    /**
     * 按汇总查询库龄差异
     * @author will
     * @date 2025/2/19 19:26
     * @return java.util.List<com.erp.model.wms.dto.VirtualInventoryAgeDTO.SendNoticeTotalDTO>
     */
    List<VirtualInventoryAgeDTO.SendNoticeTotalDTO> listDiffTotalSendNotice();
}
