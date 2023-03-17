package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.entity.AttachmentEntity;
import com.erp.model.scm.entity.SupplierCredentialEntity;
import com.erp.server.scm.mapper.SupplierCredentialMapper;
import com.erp.server.scm.service.AttachmentService;
import com.erp.server.scm.service.SupplierCredentialService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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


    @Resource
    private AttachmentService attachmentService;

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
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchCredential(String supplierId, List<SupplierCredentialDTO.AddDTO> credentialList) {
        if (CollectionUtils.isEmpty(credentialList)) {
            return;
        }
        List<SupplierCredentialEntity> addList = new ArrayList<>(credentialList.size());
        Class<SupplierCredentialEntity> credentialClass = SupplierCredentialEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        List<AttachmentEntity> batchAttachmentList = new ArrayList<>(10);
        for (SupplierCredentialDTO.AddDTO item : credentialList) {
            SupplierCredentialEntity addEntity = new SupplierCredentialEntity();
            String id = IdWorker.getIdStr();
            BeanMapper.copy(item, addEntity);
            addEntity.setSupplierId(supplierId);
            addEntity.setId(id);
            addList.add(addEntity);
            //附件集合
            List<String> attachmentList = item.getCredentialAttachmentList();
            if (CollectionUtils.isNotEmpty(attachmentList)) {
                for (String url : attachmentList) {
                    AttachmentEntity attachment = new AttachmentEntity();
                    attachment.setAttachId("");
                    attachment.setAttachUrl(url);
                    attachment.setBusinessId(id);
                    attachment.setType(type);
                    batchAttachmentList.add(attachment);
                }

            }
        }

        this.saveBatch(addList);
        attachmentService.saveBatch(batchAttachmentList);
    }
}
