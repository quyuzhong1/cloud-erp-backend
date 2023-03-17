package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.entity.SupplierCredentialEntity;
import com.erp.server.scm.mapper.SupplierCredentialMapper;
import com.erp.server.scm.service.SupplierCredentialService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 供应商资质表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class SupplierCredentialServiceImpl extends SuperServiceImpl<SupplierCredentialMapper, SupplierCredentialEntity> implements SupplierCredentialService {


    /**
     * 保存 供应商资质信息
     *
     * @param supplierId
     * @param credentialList
     * @return void
     * @author yl
     * @date 2023-03-17 16:14
     */
    @Override
    public void saveBatchCredential(String supplierId, List<SupplierCredentialDTO.AddDTO> credentialList) {
        if (CollectionUtils.isEmpty(credentialList)) {
            return;
        }
        List<SupplierCredentialEntity> addList = new ArrayList<>(credentialList.size());
        Class<SupplierCredentialEntity> credentialClass = SupplierCredentialEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        String type = tableName.value();
        for (SupplierCredentialDTO.AddDTO item : credentialList) {
            SupplierCredentialEntity addEntity = new SupplierCredentialEntity();
        }

    }
}
