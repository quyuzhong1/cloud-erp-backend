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
    String addSupplier(SupplierDTO.AddDTO dto);
}
