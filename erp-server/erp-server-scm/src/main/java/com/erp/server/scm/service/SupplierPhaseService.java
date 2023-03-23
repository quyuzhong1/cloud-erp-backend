package com.erp.server.scm.service;

import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.SupplierPhaseDTO;
import com.erp.model.scm.entity.SupplierPhaseEntity;

/**
 * <p>
 * 供应商升降级 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface SupplierPhaseService extends SuperService<SupplierPhaseEntity> {

    
    
    /**
     * 添加供应商阶段
     * @author yl
     * @date 2023-03-23 12:20
     * @param dto
     * @return com.erp.model.scm.entity.SupplierPhaseEntity
     */
    SupplierPhaseEntity add(SupplierPhaseDTO.AddDTO dto);

    /**
     * 提交并审核
     * @author yl
     * @date 2023-03-23 16:34
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(SupplierPhaseDTO.AddDTO dto);
}
