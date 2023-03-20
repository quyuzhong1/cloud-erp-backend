package com.erp.server.scm.service;

import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierEntity;

/**
 * <p>
 * 供应商表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface SupplierService extends SuperService<SupplierEntity> {

    /**
     * 保存供应商信息
     * @author yl
     * @date 2023-03-17 15:12
     * @param dto
     * @return java.lang.Boolean
     */
    SupplierEntity addSupplier(SupplierDTO.AddDTO dto);

    
    /**
     * 保存并提交审核供应商
     * @author yl
     * @date 2023-03-20 9:13
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(SupplierDTO.AddDTO dto);

    
    /**
     * 供应商详情
     * @author yl
     * @date 2023-03-20 10:00
     * @param supplierId
     * @return com.erp.model.scm.dto.SupplierDTO.updateDTO
     */
    SupplierDTO.UpdateDTO view(String supplierId);

    
    /**
     * 修改供应商信息
     * @author yl
     * @date 2023-03-20 10:56
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateSupplier(SupplierDTO.UpdateDTO dto);
}
