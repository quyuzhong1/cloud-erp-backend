package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.VirtualWarehousePushHandleRelationDTO;
import com.erp.model.wms.entity.VirtualWarehousePushHandleRelationEntity;

import java.util.List;

/**
 * <p>
 * 分货单拆单关联关系表 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
 */
public interface VirtualWarehousePushHandleRelationService extends SuperService<VirtualWarehousePushHandleRelationEntity> {

    /**
    * 新增
    * @author hyj
    * @date: 2024-06-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualWarehousePushHandleRelationDTO.AddDTO dto);

    /**
    * 修改
    * @author hyj
    * @date: 2024-06-07
    * @param dto
    * @return
    */
    Boolean update(VirtualWarehousePushHandleRelationDTO.UpdateDTO dto);

    /**
     * 根据来源id查询
     * @author will
     * @date 2026/1/16 10:26
     * @param sourceIds
     * @param sourceDetailIds
     * @return List<VirtualWarehousePushHandleRelationEntity>
     */
    List<VirtualWarehousePushHandleRelationEntity> listBySourceIds(List<String> sourceIds,List<String> sourceDetailIds);

}
