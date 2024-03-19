package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;

/**
 * <p>
 * b2c报关对账单明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
public interface TmsB2cDeclareReconciliationDetailService extends SuperService<TmsB2cDeclareReconciliationDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsB2cDeclareReconciliationDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    Boolean update(TmsB2cDeclareReconciliationDetailDTO.UpdateDTO dto);


}
