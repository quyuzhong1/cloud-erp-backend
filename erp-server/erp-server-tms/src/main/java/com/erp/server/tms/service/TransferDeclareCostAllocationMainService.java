package com.erp.server.tms.service;
import com.erp.model.tms.entity.TransferDeclareCostAllocationMainEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TransferDeclareCostAllocationMainDTO;

/**
 * <p>
 * 中转费用分摊主表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-12-06
 */
public interface TransferDeclareCostAllocationMainService extends SuperService<TransferDeclareCostAllocationMainEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-12-06
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TransferDeclareCostAllocationMainDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-12-06
    * @param dto
    * @return
    */
    Boolean update(TransferDeclareCostAllocationMainDTO.UpdateDTO dto);

    /**
     * 修改物流大表生成状态
     * @param id
     * @param bigTableStatus
     * @return
     */
    Boolean updateBigTableStatus(String id, String bigTableStatus);
}
