package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpTransferWarehouseInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpTransferWarehouseInfoDTO;

/**
 * <p>
 * 第三方中转仓库 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-07
 */
public interface DmpTransferWarehouseInfoService extends SuperService<DmpTransferWarehouseInfoEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpTransferWarehouseInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-07
    * @param dto
    * @return
    */
    Boolean update(DmpTransferWarehouseInfoDTO.UpdateDTO dto);


}
