package com.erp.server.scm.service;

import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.entity.SupplierCredentialEntity;

import java.util.List;

/**
 * <p>
 * 供应商资质表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface SupplierCredentialService extends SuperService<SupplierCredentialEntity> {

    /**
     * 保存 供应商资质信息
     * @author yl
     * @date 2023-03-17 16:14
     * @param supplierId
     * @param credentialList
     * @return void
     */
    void saveBatchCredential(String supplierId, List<SupplierCredentialDTO.AddDTO> credentialList);
}
