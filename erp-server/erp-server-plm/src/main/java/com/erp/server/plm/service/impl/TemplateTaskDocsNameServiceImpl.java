package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.business.interceptor.PlmInterceptor;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.TmeplateDocsNameDTO;
import com.erp.model.plm.entity.TaskDocsNameEntity;
import com.erp.model.plm.entity.TemplateDeliveryDocsEntity;
import com.erp.model.plm.entity.TemplateTaskDocsNameEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.mapper.TemplateTaskDocsNameMapper;
import com.erp.server.plm.service.TaskDocsNameService;
import com.erp.server.plm.service.TemplateDeliveryDocsService;
import com.erp.server.plm.service.TemplateTaskDocsNameService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 *
 */
@Service
public class TemplateTaskDocsNameServiceImpl extends ServiceImpl<TemplateTaskDocsNameMapper, TemplateTaskDocsNameEntity>
        implements TemplateTaskDocsNameService {

    @Autowired
    private TaskDocsNameService taskDocsNameService;

    @Autowired
    private TemplateDeliveryDocsService templateDeliveryDocsService;


    /**
     * 保存模板 文档名
     *
     * @param templateId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-27 15:51
     */
    @Override
    public void saveTemplateDocsName(String templateId, String productId) {
        List<TaskDocsNameEntity> list = taskDocsNameService.getDocsNameByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplateTaskDocsNameEntity> saveList = new ArrayList<>();
            for (TaskDocsNameEntity item : list) {
                TemplateTaskDocsNameEntity entity = new TemplateTaskDocsNameEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }


    /**
     * 复制文档名
     *
     * @param templateId
     * @param productId
     * @param projectId
     * @return java.util.List<com.erp.model.plm.dto.TemplateCopySourceDTO>
     * @author yl
     * @date 2022-10-28 14:06
     */
    @Override
    public List<CopySourceDTO> copyTemplateDocsName(String templateId, String productId, String projectId) {
        List<TemplateTaskDocsNameEntity> list = getByTemplateId(templateId);
        List<TaskDocsNameEntity> existDocsNameList = taskDocsNameService.getDocsNameByProductId(productId);
        List<CopySourceDTO> sourceList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            List<TaskDocsNameEntity> copyList = new ArrayList<>();
            for (TemplateTaskDocsNameEntity item : list) {
                TaskDocsNameEntity exist = existDocsNameList.stream().
                        filter(e -> e.getName().equals(item.getName())).findFirst().orElse(null);
                CopySourceDTO source = new CopySourceDTO();
                //可能数据库存在 要排除数据库里面的数据
                if (Objects.isNull(exist)) {
                    TaskDocsNameEntity entity = new TaskDocsNameEntity();
                    BeanMapper.copy(item, entity);
                    entity.setProductId(productId);
                    String id = IdWorker.getIdStr();
                    entity.setId(id);
                    copyList.add(entity);
                    source.setNewCreateId(id);
                } else {
                    source.setNewCreateId(exist.getId());
                }
                source.setDataId(item.getId());
                sourceList.add(source);
            }
            if (CollectionUtils.isNotEmpty(copyList)) {
                taskDocsNameService.saveBatch(copyList);
            }

        }

        return sourceList;
    }

    @Override
    public Boolean saveDocsName(TmeplateDocsNameDTO dto) {
        String name = dto.getName();
        String templateId = dto.getTemplateId();
        List<DocsDTO> docksNames = getDocsNameList(templateId);
        List<String> names = docksNames.stream().map(DocsDTO::getName).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(names) && names.contains(name)) {
            throw new ServiceException(ApiError.ERROR_95012);
        }
        TemplateTaskDocsNameEntity entity = new TemplateTaskDocsNameEntity();
        entity.setName(name);
        entity.setTemplateId(templateId);
        boolean flag = this.save(entity);
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        String uid = loginUser.getUid();
        String userName = loginUser.getUserName();
        if (flag) {
            //保存交付文档名称的同时保存输出物数据
            TemplateDeliveryDocsEntity templateDeliveryDocsEntity = new TemplateDeliveryDocsEntity();
            templateDeliveryDocsEntity.setTemplateId(templateId);
            templateDeliveryDocsEntity.setDocsNameId(entity.getId());
            templateDeliveryDocsEntity.setDocsName(name);
            templateDeliveryDocsEntity.setStatus(IsConstant.YES);
            templateDeliveryDocsEntity.setCreateUserId(uid);
            templateDeliveryDocsEntity.setCreateUserName(userName);
            templateDeliveryDocsService.save(templateDeliveryDocsEntity);
        }
        return true;
    }

    /**
     * 获取到项目
     * @param templateId
     * @return
     */
    public List<TemplateTaskDocsNameEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateTaskDocsNameEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskDocsNameEntity::getTemplateId, templateId);
        return list(queryWrapper);
    }

    /**
     * @description: 查询已存在的文档名称
     * @author Will
     * @date: 2022/11/16 12:58
     * @param templateId
     * @return List<DocsDTO>
     */
    @Override
    public List<DocsDTO> getDocsNameList(String templateId) {
        List<DocsDTO> resultList = new LinkedList<>();
        LambdaQueryWrapper<TemplateTaskDocsNameEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskDocsNameEntity::getTemplateId, templateId);
        List<TemplateTaskDocsNameEntity> list = list(queryWrapper);
        List<DocsDTO> docsNames = BeanMapper.copyList(list, DocsDTO.class);
        int noSys = IsConstant.NO;
        for (DocsDTO item : docsNames) {
            item.setIsSys(noSys);
        }
        resultList.addAll(docsNames);
        return resultList;
    }

    @Override
    public TemplateTaskDocsNameEntity getByIdAndTemplateId(String docsNameId, String templateId) {
        LambdaQueryWrapper<TemplateTaskDocsNameEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskDocsNameEntity::getTemplateId,templateId);
        queryWrapper.eq(TemplateTaskDocsNameEntity::getId,docsNameId);
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional
    public Boolean updateDocsName(TmeplateDocsNameDTO dto) {
        String name = dto.getName();
        String templateId = dto.getTemplateId();
        List<DocsDTO> docksNames = getDocsNameList(templateId);
        List<String> names = docksNames.stream().map(DocsDTO::getName).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(names) && names.contains(name)) {
            throw new ServiceException(ApiError.ERROR_95012);
        }
        TemplateTaskDocsNameEntity entity = new TemplateTaskDocsNameEntity();
        entity.setName(name);
        entity.setTemplateId(templateId);
        boolean flag = this.save(entity);
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        String uid = loginUser.getUid();
        String userName = loginUser.getUserName();
        if (flag) {
            //更新输出物关联的文件名和文件名id
            LambdaUpdateWrapper<TemplateDeliveryDocsEntity> updateWrapper = new LambdaUpdateWrapper();
            updateWrapper.eq(TemplateDeliveryDocsEntity::getId,dto.getDeliveryDocsId());
            updateWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId,dto.getTemplateId());
            updateWrapper.set(TemplateDeliveryDocsEntity::getDocsNameId,entity.getId());
            updateWrapper.set(TemplateDeliveryDocsEntity::getDocsName,entity.getName());
            updateWrapper.set(TemplateDeliveryDocsEntity::getUpdateUserId,uid);
            updateWrapper.set(TemplateDeliveryDocsEntity::getUpdateUserName,userName);
            templateDeliveryDocsService.update(updateWrapper);
        }
        return true;
    }


}




