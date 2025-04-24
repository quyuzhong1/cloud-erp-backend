package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.SalesEstimateManualDTO;
import com.erp.model.mrp.entity.SalesEstimateManualEntity;

import java.util.List;

/**
 * <p>
 * 运营销量预估 服务类
 * </p>
 *
 * @author will
 * @since 2024-09-05
 */
public interface SalesEstimateManualService extends SuperService<SalesEstimateManualEntity> {

    /**
     * 查询运营销量预估
     * @param ids 补货建议id
     */
    List<SalesEstimateManualEntity> listByReplenishmentIds(List<String> ids);

    /**
    * 修改
    * @author will
    * @date: 2024-09-05
    * @param dto
    * @return
    */
    Boolean update(SalesEstimateManualDTO.UpdateDTO dto,String replenishmentSuggestionId);


}
