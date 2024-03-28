package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.tms.dto.TmsReconciliationCostDTO;
import com.erp.model.tms.entity.TmsReconciliationCostEntity;

import java.util.List;

/**
 * <p>
 * 对账费用单 服务类
 * </p>
 *
 * @author will
 * @since 2024-03-26
 */
public interface TmsReconciliationCostService extends SuperService<TmsReconciliationCostEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-03-26
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsReconciliationCostDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-03-26
    * @param updateList
    * @return
    */
    Boolean update(List<TmsReconciliationCostDTO.UpdateDTO> updateList);


}
