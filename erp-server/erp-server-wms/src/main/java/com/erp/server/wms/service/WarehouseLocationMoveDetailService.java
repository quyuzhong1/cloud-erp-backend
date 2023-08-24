package com.erp.server.wms.service;
import com.erp.model.wms.entity.WarehouseLocationMoveDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;

/**
 * <p>
 * 仓位移动明细表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-24
 */
public interface WarehouseLocationMoveDetailService extends SuperService<WarehouseLocationMoveDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-08-24
    * @param dto
    * @return
    */
    String add(WarehouseLocationMoveDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-08-24
    * @param dto
    * @return
    */
    Boolean update(WarehouseLocationMoveDetailDTO.UpdateDTO dto);


}
