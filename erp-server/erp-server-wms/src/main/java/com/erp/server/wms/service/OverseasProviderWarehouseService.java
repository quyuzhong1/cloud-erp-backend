package com.erp.server.wms.service;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;

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
    * 新增
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OverseasProviderWarehouseDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasProviderWarehouseDTO.UpdateDTO dto);


}
