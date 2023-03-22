package com.erp.server.wms.service;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.core.serveice.SuperService;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.WarehouseEntity;

import java.util.List;

/**
 * <p>
 * 仓库表 服务类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
public interface WarehouseService extends SuperService<WarehouseEntity> {
    /** 根据ids查询仓库
     * @description:
     * @author Will
     * @date: 2023/3/17 16:07
     * @param ids
     * @return List<WarehouseDTO>
     */
    List<WarehouseDTO.UpdateDTO> listWarehouseByIds(List<String> ids);

    /**
     * 查询所有审核通过并启用的仓库
     * @description:
     * @author Will
     * @date: 2023/3/21 14:27
     * @return List<WarehouseDTO>
     */
    List<WarehouseDTO.UpdateDTO> listApproveWarehouse();

    
    /**
     * 添加仓库
     * @author yl
     * @date 2023-03-22 10:17
     * @param dto
     * @return com.erp.model.wms.entity.WarehouseEntity
     */
    WarehouseEntity add(WarehouseDTO.AddDTO dto);

    
    /**
     * 修改仓库
     * @author yl
     * @date 2023-03-22 11:08
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateWarehouse(WarehouseDTO.UpdateDTO dto);

    
    /**
     * 提交并审核
     * @author yl
     * @date 2023-03-22 11:16
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(WarehouseDTO.AddDTO dto);

    
    /**
     * 仓库提交审核
     * @author yl
     * @date 2023-03-22 11:31
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);

    
    /**
     * 更改仓库状态
     * @author yl
     * @date 2023-03-22 11:43
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateStatus(UpdateStateDTO dto);

    /**
     * 审核仓库
     * @author yl
     * @date 2023-03-22 11:45
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean approve(BaseApproveParamDTO dto);

    
    /**
     * 反审核
     * @author yl
     * @date 2023-03-22 11:59
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean disApprove(List<String> ids);
}
