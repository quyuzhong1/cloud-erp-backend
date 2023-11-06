package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsBillCostDTO;

/**
 * <p>
 * 自发货费用 服务类
 * </p>
 *
 * @author Will
 * @since 2023-11-06
 */
public interface LogisticsBillCostService extends SuperService<LogisticsBillCostEntity> {

    /**
    * 新增
    * @author Will
    * @date: 2023-11-06
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsBillCostDTO.AddDTO dto);

    /**
    * 修改
    * @author Will
    * @date: 2023-11-06
    * @param dto
    * @return
    */
    Boolean update(LogisticsBillCostDTO.UpdateDTO dto);


}
