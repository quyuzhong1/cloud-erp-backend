package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.AttachmentDTO;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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


    /**
     * 根据供应商ｉｄ　获取资质信息
     *
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.SupplierCredentialDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-20 10:19
     */
    @Override
    public List<SupplierCredentialDTO.UpdateDTO> getBySupplierId(String supplierId) {
        List<SupplierCredentialEntity> list = this.getList(supplierId);
        List<SupplierCredentialDTO.UpdateDTO> resultList = BeanMapper.copyList(list, SupplierCredentialDTO.UpdateDTO.class);
        //获取到业务表id
        List<String> businessIds = resultList.stream().map(SupplierCredentialDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(businessIds);
        for (SupplierCredentialDTO.UpdateDTO item : resultList) {
            List<AttachmentDTO.UpdateDTO> attachments = attachmentList.stream().
                    filter(a -> a.getBusinessId().equals(item.getId())).collect(Collectors.toList());
            item.setCredentialAttachmentList(attachments);
        }
        return resultList;
    }


    /**
     * 修改供应商资质信息
     *
     * @param credentialList
     * @param supplierId
     * @return void
     * @author yl
     * @date 2023-03-20 11:37
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCredential(List<SupplierCredentialDTO.UpdateDTO> credentialList, String supplierId) {
        if (CollectionUtils.isEmpty(credentialList)) {
            return;
        }
        List<SupplierCredentialEntity> saveOrUpdateList = new ArrayList<>(credentialList.size());
        Class<SupplierCredentialEntity> credentialClass = SupplierCredentialEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        List<AttachmentEntity> batchAttachmentList = new ArrayList<>(10);
        List<SupplierCredentialEntity> dbList = this.getList(supplierId);
        //获取到业务表id 集合
        List<String> businessIdList = dbList.stream().map(SupplierCredentialEntity::getId).collect(Collectors.toList());
        attachmentService.deleteByBusinessIds(businessIdList);

        for (SupplierCredentialDTO.UpdateDTO item : credentialList) {
            SupplierCredentialEntity entity = new SupplierCredentialEntity();
            BeanMapper.copy(item, entity);
            entity.setSupplierId(supplierId);
            saveOrUpdateList.add(entity);
            //附件集合
            List<AttachmentDTO.UpdateDTO> attachmentList = item.getCredentialAttachmentList();
            if (CollectionUtils.isNotEmpty(attachmentList)) {
                for (AttachmentDTO.UpdateDTO attachment : attachmentList) {
                    AttachmentEntity addAttachment = new AttachmentEntity();
                    addAttachment.setAttachUrl(attachment.getAttachUrl());
                    addAttachment.setBusinessId(entity.getId());
                    addAttachment.setType(type);
                    batchAttachmentList.add(addAttachment);
                }

            }
        }

        this.saveOrUpdateBatch(saveOrUpdateList);
        attachmentService.saveBatch(batchAttachmentList);

    }


    /**
     * 根据供应商id 集合删除资质信息
     *
     * @param supplierIds
     * @return void
     * @author yl
     * @date 2023-03-20 18:56
     */
    @Override
    public void removeBySupplierIds(List<String> supplierIds) {
        if (CollectionUtils.isEmpty(supplierIds)) {
            return;
        }
        List<SupplierCredentialEntity> allList = this.getList(supplierIds);
        List<String> idList = allList.stream().map(SupplierCredentialEntity::getId).collect(Collectors.toList());
        LambdaQueryWrapper<SupplierCredentialEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SupplierCredentialEntity::getSupplierId, idList);
        this.remove(queryWrapper);
        attachmentService.deleteByBusinessIds(idList);
    }


    private List<SupplierCredentialEntity> getList(String supplierId) {
        LambdaQueryWrapper<SupplierCredentialEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SupplierCredentialEntity::getSupplierId, supplierId);
        return list(queryWrapper);
    }


    private List<SupplierCredentialEntity> getList(List<String> supplierIdS) {
        if (CollectionUtils.isEmpty(supplierIdS)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SupplierCredentialEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SupplierCredentialEntity::getSupplierId, supplierIdS);
        return list(queryWrapper);
    }


}
