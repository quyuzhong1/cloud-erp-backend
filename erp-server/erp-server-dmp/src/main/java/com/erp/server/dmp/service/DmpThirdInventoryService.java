package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpThirdInventoryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpThirdInventoryDTO;

/**
 * <p>
 * 第三方仓库存 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-07
 */
public interface DmpThirdInventoryService extends SuperService<DmpThirdInventoryEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpThirdInventoryDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-07
    * @param dto
    * @return
    */
    Boolean update(DmpThirdInventoryDTO.UpdateDTO dto);


}
