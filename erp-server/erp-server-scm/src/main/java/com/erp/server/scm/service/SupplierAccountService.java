package com.erp.server.scm.service;

import com.common.business.service.SuperService;
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

    
    /**
     * 根据供应商id 获取到账户信息
     * @author yl
     * @date 2023-03-20 10:12
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.SupplierAccountDTO.UpdateDTO>
     */
    List<SupplierAccountDTO.UpdateDTO> getBySupplierId(String supplierId);

    /**
     * @description: 根据供应商id 获取到账户信息
     * @author Will
     * @date: 2023/4/26 10:07
     * @param supplierId
     * @return List<SupplierAccountEntity>
     */
    List<SupplierAccountEntity> listBySupplierId(String supplierId);

    /**
     * 更改供应商账户信息
     * @author yl
     * @date 2023-03-20 11:28
     * @param bankAccountList
     * @param supplierId
     * @return void
     */
    void updateAccount(List<SupplierAccountDTO.UpdateDTO> bankAccountList, String supplierId);

    /**
     * 根据供应商id 集合删除 账户信息
     * @author yl
     * @date 2023-03-20 18:51
     * @param ids
     * @return void
     */
    void removeBySupplierIds(List<String> ids);

    /**
     * 转化 导入的数据
     * @author yl
     * @date 2023-03-31 9:11
     * @param supplierId
     * @param accountList
     * @return java.util.List<com.erp.model.scm.entity.SupplierAccountEntity>
     */
    List<SupplierAccountEntity> transform(String supplierId, List<SupplierAccountDTO.ImportAddDTO> accountList);
}
