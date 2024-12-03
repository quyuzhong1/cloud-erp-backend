package com.erp.server.tms.service;
import com.erp.model.tms.entity.TransferDeclareCostAllocationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO;

/**
 * <p>
 * 中转费用分摊 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-12-03
 */
public interface TransferDeclareCostAllocationService extends SuperService<TransferDeclareCostAllocationEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TransferDeclareCostAllocationDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    Boolean update(TransferDeclareCostAllocationDTO.UpdateDTO dto);


}
