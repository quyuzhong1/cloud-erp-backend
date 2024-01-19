package com.erp.server.srm.service;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;

/**
 * <p>
 * 采购对账单明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
public interface PoReconciliationDetailService extends SuperService<PoReconciliationDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PoReconciliationDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    Boolean update(PoReconciliationDetailDTO.UpdateDTO dto);


}
