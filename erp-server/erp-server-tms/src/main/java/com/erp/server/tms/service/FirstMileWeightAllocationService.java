package com.erp.server.tms.service;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;

import java.util.List;

/**
 * <p>
 * 头程重量分摊 服务类
 * </p>
 *
 * @author tmj
 * @since 2024-08-20
 */
public interface FirstMileWeightAllocationService extends SuperService<FirstMileWeightAllocationEntity> {

    /**
    * 新增
    * @author tmj
    * @date: 2024-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FirstMileWeightAllocationDTO.AddDTO dto);

    /**
    * 修改
    * @author tmj
    * @date: 2024-08-20
    * @param dto
    * @return
    */
    Boolean update(FirstMileWeightAllocationDTO.UpdateDTO dto);

    /**
     * 根据物流单id查询重量分摊记录
     * @param logisticsBillId
     * @return
     */
    List<FirstMileWeightAllocationEntity> getByLogisticsBillId(String logisticsBillId);
}
