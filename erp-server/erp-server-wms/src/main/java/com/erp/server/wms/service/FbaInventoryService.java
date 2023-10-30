package com.erp.server.wms.service;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaInventoryDTO;

/**
 * <p>
 * FBI库存 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
public interface FbaInventoryService extends SuperService<FbaInventoryEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    String add(FbaInventoryDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    Boolean update(FbaInventoryDTO.UpdateDTO dto);


}
