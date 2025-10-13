package com.erp.server.wms.service;
import com.erp.model.wms.entity.InventoryTransactionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.InventoryTransactionDTO;

/**
 * <p>
 * 库存事务表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-10-13
 */
public interface InventoryTransactionService extends SuperService<InventoryTransactionEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-10-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(InventoryTransactionDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-10-13
    * @param dto
    * @return
    */
    Boolean update(InventoryTransactionDTO.UpdateDTO dto);


}
