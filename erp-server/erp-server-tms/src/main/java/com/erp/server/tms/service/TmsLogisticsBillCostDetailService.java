package com.erp.server.tms.service;
import com.common.business.service.SuperService;

import com.erp.model.tms.dto.TmsLogisticsBillCostDetailDTO;
import com.erp.model.tms.entity.TmsLogisticsBillCostDetailEntity;

import java.util.List;

/**
 * <p>
 * 自发货费用明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-03-20
 */
public interface TmsLogisticsBillCostDetailService extends SuperService<TmsLogisticsBillCostDetailEntity> {

    /**
     * @description: 批量新增
     * @author Will
     * @date: 2024/3/22 14:41
     * @param costDetailList
     * @param mainId
     * @return Boolean
     */
    Boolean batchAdd(List<TmsLogisticsBillCostDetailDTO.AddDTO> costDetailList, String mainId);


    /**
     * @description: 批量更新
     * @author Will
     * @date: 2024/3/22 14:41
     * @param costDetailList
     * @param mainId
     * @return Boolean
     */
    Boolean batchUpdate(List<TmsLogisticsBillCostDetailDTO.UpdateDTO> costDetailList, String mainId);


    /**
     * 查询预估与实际比对列表
     * @return
     */
    List<TmsLogisticsBillCostDetailDTO.CostCompareDTO> getCostCompareListById(String id);
}
