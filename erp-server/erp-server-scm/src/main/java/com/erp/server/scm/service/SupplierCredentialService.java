package com.erp.server.scm.service;

import com.common.business.service.SuperService;
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

    /**
     * 根据供应商ｉｄ　获取资质信息
     * @author yl
     * @date 2023-03-20 10:19
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.SupplierCredentialDTO.UpdateDTO>
     */
    List<SupplierCredentialDTO.UpdateDTO> getBySupplierId(String supplierId);

    /**
     * 修改供应商资质信息
     * @author yl
     * @date 2023-03-20 11:37
     * @param credentialList
     * @param supplierId
     * @return void
     */
    void updateCredential(List<SupplierCredentialDTO.UpdateDTO> credentialList, String supplierId);

    /**
     * 根据供应商id 集合删除资质信息
     * @author yl
     * @date 2023-03-20 18:56
     * @param ids
     * @return void
     */
    void removeBySupplierIds(List<String> ids);
}
