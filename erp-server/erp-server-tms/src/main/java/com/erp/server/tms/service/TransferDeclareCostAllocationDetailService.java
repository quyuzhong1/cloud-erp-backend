package com.erp.server.tms.service;
import com.erp.model.tms.entity.TransferDeclareCostAllocationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDetailDTO;

/**
 * <p>
 * 中转费用分摊明细 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-12-03
 */
public interface TransferDeclareCostAllocationDetailService extends SuperService<TransferDeclareCostAllocationDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TransferDeclareCostAllocationDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    Boolean update(TransferDeclareCostAllocationDetailDTO.UpdateDTO dto);


}
