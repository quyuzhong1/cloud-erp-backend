package com.erp.server.scm.service;

import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.SupplierAccountDTO;
import com.erp.model.scm.entity.SupplierAccountEntity;

import java.util.List;

/**
 * <p>
 * 供应商结算信息 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface SupplierAccountService extends SuperService<SupplierAccountEntity> {
    
    /**
     * 批量保存供应商账户信息
     * @author yl
     * @date 2023-03-17 15:59
     * @param supplierId
     * @param bankAccountList
     * @return void
     */
    void saveBatchBankAccount(String supplierId, List<SupplierAccountDTO.AddDTO> bankAccountList);
}
