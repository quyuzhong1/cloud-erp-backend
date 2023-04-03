package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.entity.AttachmentEntity;
import com.erp.model.scm.entity.SupplierCredentialEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.scm.mapper.SupplierCredentialMapper;
import com.erp.server.scm.service.AttachmentService;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.SupplierCredentialService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
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

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

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
            List<String> attachmentUrlList = item.getAttachmentUrlList();
            //附件名
            List<String> attachmentNameList = item.getAttachmentNameList();
            if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
                for (int i = 0; i < attachmentUrlList.size(); i++) {
                    AttachmentEntity attachment = new AttachmentEntity();
                    attachment.setAttachUrl(attachmentUrlList.get(i));
                    attachment.setAttachName(attachmentNameList.get(i));
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
            List<String> attachmentUrlList = attachmentList.stream().
                    filter(a -> a.getBusinessId().equals(item.getId())).
                    map(AttachmentDTO.UpdateDTO::getAttachUrl).
                    collect(Collectors.toList());
            List<String> attachmentNameList = attachmentList.stream().
                    filter(a -> a.getBusinessId().equals(item.getId())).
                    map(AttachmentDTO.UpdateDTO::getAttachName).
                    collect(Collectors.toList());
            item.setAttachmentUrlList(attachmentUrlList);
            item.setAttachmentNameList(attachmentNameList);
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
        //这是修改的
        List<SupplierCredentialDTO.UpdateDTO> updateList = credentialList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是要添加的
        List<SupplierCredentialDTO.UpdateDTO> addList = credentialList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

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

        List<String> deleteIdList = getDeleteIds(updateList, dbList);
        //这是要删除的
        List<SupplierCredentialEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }

        for (SupplierCredentialDTO.UpdateDTO item : credentialList) {
            SupplierCredentialEntity entity = new SupplierCredentialEntity();
            BeanMapper.copy(item, entity);
            entity.setSupplierId(supplierId);
            saveOrUpdateList.add(entity);
            //附件集合
            List<String> attachmentUrlList = item.getAttachmentUrlList();
            List<String> attachmentNameList = item.getAttachmentNameList();
            if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {

                for (int i = 0; i < attachmentUrlList.size(); i++) {
                    AttachmentEntity addAttachment = new AttachmentEntity();
                    addAttachment.setAttachUrl(attachmentUrlList.get(i));
                    addAttachment.setAttachName(attachmentNameList.get(i));
                    addAttachment.setBusinessId(entity.getId());
                    addAttachment.setType(type);
                    batchAttachmentList.add(addAttachment);
                }

            }
        }


        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(supplierId, obj.getName())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("删除了一个资质名称【%s】", ModuleTypeEnum.SUPPLIER.getCode(), removePairList, "编辑操作");

        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(supplierId, obj.getName())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("添加了一个资质名称【%s】", ModuleTypeEnum.SUPPLIER.getCode(), addPairList, "编辑操作");

        //修改的
        for (SupplierCredentialDTO.UpdateDTO update : updateList) {
            String id = update.getId();
            SupplierCredentialEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if(old!=null){
                moduleOperateLogService.addModuleOperateLogByObj(old,update, ModuleTypeEnum.SUPPLIER.getCode(),supplierId,"","");
            }
        }
        this.saveOrUpdateBatch(saveOrUpdateList);
        attachmentService.saveBatch(batchAttachmentList);

    }

    
    /**
     * 获取要删除的
     * @author yl
     * @date 2023-03-31 15:54
     * @param updateList
     * @param dbList
     * @return java.util.List<java.lang.String>
     */
    private List<String> getDeleteIds(List<SupplierCredentialDTO.UpdateDTO> updateList, List<SupplierCredentialEntity> dbList) {
        List<String> ids = updateList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SupplierCredentialDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(SupplierCredentialEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
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
        if(CollectionUtils.isNotEmpty(idList)){
            LambdaQueryWrapper<SupplierCredentialEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SupplierCredentialEntity::getSupplierId, idList);
            this.remove(queryWrapper);
            attachmentService.deleteByBusinessIds(idList);
        }
    }


    /**
     * 检查资质日期
     *
     * @param credentialList
     * @return void
     * @author yl
     * @date 2023-03-29 15:48
     */
    @Override
    public void checkDate(List<SupplierCredentialDTO.AddDTO> credentialList) {
        if (CollectionUtils.isNotEmpty(credentialList)) {
            List<SupplierCredentialDTO.AddDTO> list = credentialList.stream().filter(c -> c.getEffectiveDate() != null && c.getExpireDate() != null).collect(Collectors.toList());
            long count = list.stream().filter(c -> c.getExpireDate().compareTo(c.getEffectiveDate()) < 0).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_98037);
            }

        }

    }


    /**
     * 转化 导入的数据
     *
     * @param supplierId
     * @param credentialList
     * @return java.util.List<com.erp.model.scm.entity.SupplierAccountEntity>
     * @author yl
     * @date 2023-03-31 9:11
     */
    @Override
    public List<SupplierCredentialEntity> transform(String supplierId, List<SupplierCredentialDTO.ImportAddDTO> credentialList) {
        if (CollectionUtils.isEmpty(credentialList)) {
            return Collections.emptyList();
        }
        List<SupplierCredentialEntity> addList = new ArrayList<>(credentialList.size());
        for (SupplierCredentialDTO.ImportAddDTO item : credentialList) {
            SupplierCredentialEntity credential = new SupplierCredentialEntity();
            BeanMapper.copy(item, credential);
            credential.setSupplierId(supplierId);
            addList.add(credential);
        }
        return addList;
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
