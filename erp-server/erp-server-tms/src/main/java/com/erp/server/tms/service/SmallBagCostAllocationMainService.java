package com.erp.server.tms.service;
import com.erp.model.tms.entity.SmallBagCostAllocationMainEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.SmallBagCostAllocationMainDTO;

/**
 * <p>
 * 小包费用分摊主表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-12-05
 */
public interface SmallBagCostAllocationMainService extends SuperService<SmallBagCostAllocationMainEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-12-05
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SmallBagCostAllocationMainDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-12-05
    * @param dto
    * @return
    */
    Boolean update(SmallBagCostAllocationMainDTO.UpdateDTO dto);


}
