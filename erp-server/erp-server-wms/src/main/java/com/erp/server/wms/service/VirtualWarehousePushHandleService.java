package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.VirtualWarehousePushHandleDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleDetailEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleEntity;

import java.util.List;

/**
 * <p>
 * 分货单拆单主表 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
 */
public interface VirtualWarehousePushHandleService extends SuperService<VirtualWarehousePushHandleEntity> {

    /**
    * 新增
    * @author hyj
    * @date: 2024-06-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualWarehousePushHandleDTO.AddDTO dto);

    /**
    * 修改
    * @author hyj
    * @date: 2024-06-07
    * @param dto
    * @return
    */
    Boolean update(VirtualWarehousePushHandleDTO.UpdateDTO dto);


    void forceDeleteById(String id);
    /**
     * 生成平台新增分货同步单
     * @author will
     * @date 2026/1/27 15:13
     * @param allocationEntity
     * @return void
     */
    List<VirtualWarehousePushHandleDetailEntity> addAllocationPush(VirtualWarehouseAllocationEntity allocationEntity, List<String> transferIdList);
    /**
     * 生成平台取消分货同步单
     * @author will
     * @date 2026/1/27 15:58
     * @param allocationEntity
     * @return List<VirtualWarehousePushHandleDetailEntity>
     */
    List<VirtualWarehousePushHandleDetailEntity> cancelAllocationPush(VirtualWarehouseAllocationEntity allocationEntity);
    /**
     *  删除分货单推送主表
     * @author will
     * @date 2026/1/27 16:35
     * @param allocationId
     * @return void
     */
    void deleteVirtualWarehousePushHandle(String allocationId);
}
