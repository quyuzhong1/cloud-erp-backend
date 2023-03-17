package com.erp.server.scm.service.impl;

import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.SupplierAccountDTO;
import com.erp.model.scm.entity.SupplierAccountEntity;
import com.erp.server.scm.mapper.SupplierAccountMapper;
import com.erp.server.scm.service.SupplierAccountService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 供应商结算信息 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class SupplierAccountServiceImpl extends SuperServiceImpl<SupplierAccountMapper, SupplierAccountEntity> implements SupplierAccountService {


    /**
     * 批量保存供应商账户信息
     *
     * @param supplierId
     * @param bankAccountList
     * @return void
     * @author yl
     * @date 2023-03-17 15:59
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchBankAccount(String supplierId, List<SupplierAccountDTO.AddDTO> bankAccountList) {
        if (CollectionUtils.isEmpty(bankAccountList)) {
            return;
        }
        List<SupplierAccountEntity> addList = BeanMapper.copyList(bankAccountList, SupplierAccountEntity.class);
        addList.forEach(a -> a.setSupplierId(supplierId));
        this.saveBatch(addList);

    }
}
