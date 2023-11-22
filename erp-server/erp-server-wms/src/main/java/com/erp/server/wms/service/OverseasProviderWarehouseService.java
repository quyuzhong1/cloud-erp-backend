package com.erp.server.wms.service;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;

import java.util.List;

/**
 * <p>
 * 海外物流商仓库 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface OverseasProviderWarehouseService extends SuperService<OverseasProviderWarehouseEntity> {

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasProviderWarehouseDTO.UpdateDTO dto);

    /**
     * 根据仓库id查询绑定关系
     * @Author Luo_WG
     * @Date 2023/11/17 12:18
     * @param warehouseId
     * @return com.erp.model.wms.entity.OverseasProviderWarehouseEntity
     **/
    OverseasProviderWarehouseEntity getByWarehouseId(String warehouseId);

    /**
     * 根据第三方仓库信息查询
     **/
    OverseasProviderWarehouseEntity getByPlatform(String mainId,String platformWarehouseCode);

    /**
     * 根据主表id查询详情信息
     * @Author Luo_WG
     * @Date 2023/11/22 15:52
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.OverseasProviderWarehouseEntity>
     **/
    List<OverseasProviderWarehouseEntity> listByMainIds(List<String> mainIds);
}
