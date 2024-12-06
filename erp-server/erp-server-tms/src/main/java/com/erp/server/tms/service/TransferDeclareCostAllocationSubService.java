package com.erp.server.tms.service;
import com.erp.model.tms.entity.TransferDeclareCostAllocationSubEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TransferDeclareCostAllocationSubDTO;

/**
 * <p>
 * 中转费用分摊子表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-12-06
 */
public interface TransferDeclareCostAllocationSubService extends SuperService<TransferDeclareCostAllocationSubEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-12-06
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TransferDeclareCostAllocationSubDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-12-06
    * @param dto
    * @return
    */
    Boolean update(TransferDeclareCostAllocationSubDTO.UpdateDTO dto);


}
