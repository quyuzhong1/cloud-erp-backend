package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationCostEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationCostDTO;

/**
 * <p>
 * B2c报关单 服务类
 * </p>
 *
 * @author will
 * @since 2024-03-26
 */
public interface TmsB2cDeclareReconciliationCostService extends SuperService<TmsB2cDeclareReconciliationCostEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-03-26
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsB2cDeclareReconciliationCostDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-03-26
    * @param dto
    * @return
    */
    Boolean update(TmsB2cDeclareReconciliationCostDTO.UpdateDTO dto);


}
