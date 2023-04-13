package com.erp.server.wms.service;


import com.common.business.service.SuperService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.WarehouseReceiveEntity;

/**
 * <p>
 *  采购收货服务类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-06
 */
public interface WarehouseReceiveService extends SuperService<WarehouseReceiveEntity> {
    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean add(WarehouseReceiveDTO.AddDTO dto);
}
