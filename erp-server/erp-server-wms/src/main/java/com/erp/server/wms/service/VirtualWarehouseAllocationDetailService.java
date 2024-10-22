package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;

import java.util.List;

/**
 * <p>
 * 虚拟仓分货单明细 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
public interface VirtualWarehouseAllocationDetailService extends SuperService<VirtualWarehouseAllocationDetailEntity> {

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-06-05
     */
    BaseResultDTO.AddDTO batchAdd(VirtualWarehouseAllocationDetailDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-06-05
     */
    Boolean batchUpdate(VirtualWarehouseAllocationDetailDTO.UpdateDTO dto);


    void batchAdd(VirtualWarehouseAllocationDTO.AddDTO addDTO, String id);

    Boolean batchUpdate(VirtualWarehouseAllocationDTO.UpdateDTO updateDTO, String id);


    BatchResultDTO manualFinish(VirtualWarehouseAllocationDetailEntity vmAllocationDetailEntity, VirtualWarehouseAllocationEntity vmAllocationEntity, VirtualWarehouseAllocationDTO.ManualFinishDto dto);

    void submit(VirtualWarehouseAllocationEntity allocationEntity);

    void updateByMainId(String mainId, String syncStatus);
    /**
     * 同步
     *
     * @param vmAllocationDetailEntity
     * @return
     */
    BatchResultDTO sync(VirtualWarehouseAllocationDetailEntity vmAllocationDetailEntity, VirtualWarehouseAllocationEntity vmAllocationEntity);

    /**
     * 展示分货单同步信息
     *
     * @param id
     * @return
     */
    DmpPushTaskDTO.SyncInfoDTO viewSyncInfo(String id);

    VirtualWarehouseAllocationDTO.ThirdCodeDto view(String id);

    /**
     * 修改同步状态
     */
    void updateSyncStatus(VirtualWarehouseAllocationDTO.SyncUpdateDto dto);

    /**
     * 获取同步信息
     *
     * @param id
     * @author hyj
     */
    VirtualWarehouseAllocationDTO.ManualFinishViewDTO viewManualFinish(String id);

    /**
     * 更新备注
     */
    Boolean updateRemark(VirtualWarehouseAllocationDTO.UpdateRemarkDTO updateRemarkDTO);

    /**
     * 初始化第三方编码存在异常的数据
     */
    void initFailThirdCode(String errorMsg);
    /**
     * 查询分货信息
     * @author will
     * @date 2024/9/29 14:47
     * @param skuIdList
     * @param warehouseIdList
     * @param virtualWarehouseIdList
     * @return List<AllocationDataDTO>
     */
    List<VirtualWarehouseAllocationDetailDTO.AllocationDataDTO> listAllocationData(List<String> skuIdList, List<String> warehouseIdList, List<String> virtualWarehouseIdList);
}
