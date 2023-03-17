package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.SupplierAccountDTO;
import com.erp.model.scm.dto.SupplierContactDTO;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.server.scm.mapper.SupplierMapper;
import com.erp.server.scm.service.SupplierAccountService;
import com.erp.server.scm.service.SupplierContactService;
import com.erp.server.scm.service.SupplierCredentialService;
import com.erp.server.scm.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 供应商表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class SupplierServiceImpl extends SuperServiceImpl<SupplierMapper, SupplierEntity> implements SupplierService {


    @Resource
    private SupplierAccountService supplierAccountService;

    @Resource
    private SupplierContactService supplierContactService;

    @Resource
    private SupplierCredentialService supplierCredentialService;

    /**
     * 保存供应商信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-17 15:12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addSupplier(SupplierDTO.AddDTO dto) {
        //供应商id
        String supplierId = IdWorker.getIdStr();
        SupplierEntity addEntity = new SupplierEntity();
        BeanMapper.copy(dto, addEntity);
        addEntity.setId(supplierId);
        //供应商账号信息
        List<SupplierAccountDTO.AddDTO> bankAccountList = dto.getBankAccountList();
        supplierAccountService.saveBatchBankAccount(supplierId, bankAccountList);

        //供应商联系信息
        List<SupplierContactDTO.AddDTO> contactList = dto.getContactList();
        supplierContactService.saveBatchContact(supplierId, contactList);

        //供应商资质信息
        List<SupplierCredentialDTO.AddDTO> credentialList = dto.getCredentialList();
        supplierCredentialService.saveBatchCredential(supplierId, credentialList);

        return true;
    }
}
