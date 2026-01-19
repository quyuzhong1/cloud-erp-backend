package com.erp.server.wms.service;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehousePushHandleDetailDTO;
import com.erp.model.wms.entity.VirtualWarehousePushHandleEntity;

import java.util.List;

/**
 * <p>
 * 分货单拆单明细表 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
 */
public interface VirtualWarehousePushHandleDetailService extends SuperService<VirtualWarehousePushHandleDetailEntity> {

    /**
    * 新增
    * @author hyj
    * @date: 2024-06-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualWarehousePushHandleDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author hyj
    * @date: 2024-06-07
    * @param dto
    * @return
    */
    Boolean update(VirtualWarehousePushHandleDetailDTO.UpdateDTO dto);


    void handleDetail(VirtualWarehouseAllocationEntity allocationEntity, VirtualWarehousePushHandleEntity pushHandleEntity);
    /**
     * 设置
     * @author will
     * @date 2026/1/8 10:42
     * @param syncStatus
     * @param ids
     * @return void
     */
    void updateSyncStatus(String syncStatus, List<String> ids);
    /**
     * 更新三方信息
     * @author will
     * @date 2026/1/8 10:50
     * @param dto
     * @param handelDetailId
     * @return void
     */
    void updateThirdData(VirtualWarehouseAllocationDTO.SyncUpdateDto dto, String handelDetailId);
    /**
     * 查询三方数据
     * @author will
     * @date 2026/1/8 11:46
     * @param detailIdList
     * @return List<ThirdDataDTO>
     */
    List<VirtualWarehousePushHandleDetailDTO.ThirdDataDTO> listThirdDataByDetailIdList(List<String> detailIdList);
    /**
     * 手动完结
     * @author will
     * @date 2026/1/16 11:28
     * @param dto
     * @param code
     * @param hanleDetailIdList
     * @return void
     */
    void batchManualFinish(VirtualWarehouseAllocationDTO.ManualFinishDto dto, String code, List<String> hanleDetailIdList);
}
