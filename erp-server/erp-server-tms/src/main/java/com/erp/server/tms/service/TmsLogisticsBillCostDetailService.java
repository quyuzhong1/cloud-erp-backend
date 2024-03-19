package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsLogisticsBillCostDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsLogisticsBillCostDetailDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
public interface TmsLogisticsBillCostDetailService extends SuperService<TmsLogisticsBillCostDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsLogisticsBillCostDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    Boolean update(TmsLogisticsBillCostDetailDTO.UpdateDTO dto);


}
