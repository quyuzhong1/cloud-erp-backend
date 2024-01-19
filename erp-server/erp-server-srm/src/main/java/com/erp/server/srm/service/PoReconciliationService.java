package com.erp.server.srm.service;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.srm.dto.PoReconciliationDTO;

/**
 * <p>
 * 采购对账单 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
public interface PoReconciliationService extends SuperService<PoReconciliationEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PoReconciliationDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    Boolean update(PoReconciliationDTO.UpdateDTO dto);


}
