package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.TaskDeliveryDocsEntity;
import com.erp.model.plm.entity.TemplateDeliveryDocsEntity;
import com.erp.model.plm.entity.TemplateTaskDocsNameEntity;
import com.erp.server.plm.mapper.TemplateDeliveryDocsMapper;
import com.erp.server.plm.service.TaskDeliveryService;
import com.erp.server.plm.service.TemplateDeliveryDocsService;
import com.erp.server.plm.service.TemplateTaskDocsNameService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


/**
 *
 */
@Service
public class TemplateDeliveryDocsServiceImpl extends ServiceImpl<TemplateDeliveryDocsMapper, TemplateDeliveryDocsEntity>
        implements TemplateDeliveryDocsService {


    @Autowired
    private TaskDeliveryService taskDeliveryService;

    @Autowired
    private TemplateTaskDocsNameService templateTaskDocsNameService;


    /**
     * 交付文档
     *
     * @param templateId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-27 16:07
     */
    @Override
    public List<CopySourceDTO> saveTemplateDeliveryDocs(String templateId, String productId, List<CopySourceDTO> taskSourceList, List<CopySourceDTO> sourceDocsNameList) {
        List<TaskDeliveryDocsEntity> list = taskDeliveryService.getByProductId(productId);
        List<CopySourceDTO> sourceList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplateDeliveryDocsEntity> saveList = new ArrayList<>();
            for (TaskDeliveryDocsEntity item : list) {
                TemplateDeliveryDocsEntity entity = new TemplateDeliveryDocsEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                String newTaskId = taskSourceList.stream().filter(t -> t.getDataId().equals(item.getTaskId())).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getNewCreateId())).orElse("");
                entity.setTaskId(newTaskId);

                String newDocsNameId = sourceDocsNameList.stream().filter(d -> d.getDataId().equals(item.getDocsNameId())).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getNewCreateId())).orElse("");
                entity.setTaskId(newTaskId);
                entity.setDocsNameId(newDocsNameId);
                String newCreateId = IdWorker.getIdStr();
                saveList.add(entity);
                CopySourceDTO source = new CopySourceDTO();
                source.setDataId(item.getId());
                source.setNewCreateId(newCreateId);
                sourceList.add(source);
            }
            this.saveBatch(saveList);
        }
        return sourceList;
    }

    /**
     * 从模板复制交付文档数据
     *
     * @param templateId
     * @param productId
     * @param taskSourceList
     * @param docsNameSourceList
     * @return void
     * @author yl
     * @date 2022-10-28 15:57
     */
    @Override
    public List<CopySourceDTO> copyTemplateDeliveryDocs(String templateId, String productId, List<CopySourceDTO> taskSourceList, List<CopySourceDTO> docsNameSourceList) {
        List<TemplateDeliveryDocsEntity> list = this.getByTemplateId(templateId);
        List<CopySourceDTO> sourceList = new ArrayList<>();
        List<TaskDeliveryDocsEntity> existList = taskDeliveryService.getByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TaskDeliveryDocsEntity> copyList = new ArrayList<>();
            for (TemplateDeliveryDocsEntity item : list) {
                CopySourceDTO source = new CopySourceDTO();
                source.setDataId(item.getId());
                TaskDeliveryDocsEntity exist = existList.stream().
                        filter(e -> e.getDocsNameId().equals(item.getDocsNameId())).findFirst().orElse(null);
                if (Objects.isNull(exist)) {
                    TaskDeliveryDocsEntity entity = new TaskDeliveryDocsEntity();
                    BeanMapper.copy(item, entity);
                    entity.setProductId(productId);
                    CopySourceDTO docsNameSource = docsNameSourceList.stream().
                            filter(d -> d.getDataId().equals(item.getDocsNameId())).findFirst().orElse(null);
                    if (docsNameSource != null) {
                        entity.setDocsNameId(docsNameSource.getNewCreateId());
                    } else {
                        entity.setDocsNameId("");
                    }
                    CopySourceDTO taskSource = taskSourceList.stream().
                            filter(d -> d.getDataId().equals(item.getTaskId())).findFirst().orElse(null);
                    if (taskSource != null) {
                        entity.setTaskId(taskSource.getNewCreateId());
                    } else {
                        entity.setTaskId("");
                    }

                    String id = IdWorker.getIdStr();
                    entity.setId(id);
                    copyList.add(entity);
                    source.setNewCreateId(id);
                } else {
                    source.setNewCreateId(exist.getId());
                }
                sourceList.add(source);
            }
            if (CollectionUtils.isNotEmpty(copyList)) {
                taskDeliveryService.saveBatch(copyList);
            }
        }
        return sourceList;
    }

    @Override
    public void removeByTaskIdAndTemplateId(String taskId, String templateId) {
        LambdaQueryWrapper<TemplateDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTaskId, taskId);
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId, templateId);
        this.remove(queryWrapper);
    }

    @Override
    public void removeByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId, templateId);
        this.remove(queryWrapper);
    }

    @Override
    public PagingVO<List<TemplateDeliveryDocsShowDTO>> paging(PagingDTO<TemplateSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        TemplateSearchDTO params = dto.getParams();
        IPage<TemplateDeliveryDocsShowDTO> pageData = baseMapper.paging(query, params);

        return new PagingVO(pageData);
    }

    @Override
    public Boolean saveOrUpdate(TemplateDeliveryDocsDTO dto) {
        TemplateDeliveryDocsEntity entity = new TemplateDeliveryDocsEntity();
        BeanMapperUtils.copy(dto, entity);
        //获取登录人信息
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        String uid = loginUser.getUid();
        String userName = loginUser.getUserName();
        if (StringUtils.isBlank(dto.getId())) {
            entity.setCreateUserId(uid);
            entity.setCreateUserName(userName);
        } else {
            entity.setUpdateUserId(uid);
            entity.setUpdateUserName(userName);
        }
        //根据交付文档名称id、模板id查询名称
        TemplateTaskDocsNameEntity templateTaskDocsNameEntity = templateTaskDocsNameService.getByIdAndTemplateId(entity.getDocsNameId(), entity.getTemplateId());
        if (templateTaskDocsNameEntity != null) {
            entity.setDocsName(templateTaskDocsNameEntity.getName());
        }
        //因为模板任务无主键，则无法用saveOrUpdate进行操作
        if (StringUtils.isBlank(entity.getId())) {
            return this.save(entity);
        }
        LambdaUpdateWrapper<TemplateDeliveryDocsEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TemplateDeliveryDocsEntity::getId, entity.getId());
        updateWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId, entity.getTemplateId());
        updateWrapper.set(TemplateDeliveryDocsEntity::getDocsName, entity.getDocsName());
        updateWrapper.set(TemplateDeliveryDocsEntity::getDocsNameId, entity.getDocsNameId());
        return this.update(updateWrapper);
    }

    @Override
    public Boolean deleteTemplateDeliveryDocs(TemplateDeliveryDocsDeleteDTO dto) {
        LambdaQueryWrapper<TemplateDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateDeliveryDocsEntity::getId, dto.getId());
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId, dto.getTemplateId());
        if (StringUtils.isNotBlank(dto.getTaskId())) {
            queryWrapper.eq(TemplateDeliveryDocsEntity::getTaskId, dto.getTaskId());
        }
        return this.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTemplateDeliveryDocsList(String taskId, String templateId, List<DocsDTO> deliveryDocsList) {
        if (CollectionUtils.isNotEmpty(deliveryDocsList)) {
            //删除交付文档
            removeTemplateDeliveryDocs(taskId, templateId);
            //获取登录人信息
            LoginUser loginUser = CommonInterceptor.threadLocal.get();
            if (ObjectUtils.isEmpty(loginUser)) {
                throw new ServiceException(ApiError.ERROR_9011);
            }
            String uid = loginUser.getUid();
            String userName = loginUser.getUserName();
            //保存交付文档
            List<TemplateDeliveryDocsEntity> saveList = new LinkedList<>();
            for (DocsDTO item : deliveryDocsList) {
                TemplateDeliveryDocsEntity entity = new TemplateDeliveryDocsEntity();
                entity.setTemplateId(templateId);
                entity.setDocsName(item.getName());
                entity.setTaskId(taskId);
                entity.setDocsNameId(item.getId());
                entity.setCreateUserId(uid);
                entity.setCreateUserName(userName);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }

    @Override
    public List<TemplateDeliveryDocsEntity> getAllDeliveryDocsForTemplate(String templateId) {
        LambdaQueryWrapper<TemplateDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId, templateId);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean updateStatus(TemplateDeliveryDocsUpdateStatusDTO dto) {
        TemplateTaskDocsNameEntity taskDocsNameEntity = templateTaskDocsNameService.getById(dto.getId());
        if (Objects.isNull(taskDocsNameEntity)) {
            throw new ServiceException(ApiError.ERROR_95155);
        }
        //当是关闭的时候 要查询是否有引用 有就不能管
        if (!dto.getStatus()) {
            List<TemplateDeliveryDocsEntity> deliveryDocsList = this.getByTemplateId(dto.getTemplateId());
            Long count = deliveryDocsList.stream().filter(d -> d.getDocsNameId().equals(dto.getId())&& StringUtils.isNotBlank(d.getTaskId())).count();
            //表示有引用
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_95156);
            }
        }
        taskDocsNameEntity.setStatus(dto.getStatus());
        return templateTaskDocsNameService.updateById(taskDocsNameEntity);
    }

    @Override
    public TemplateDeliveryDocsEntity getByIdAndTemplateId(String deliveryDocsId, String templateId) {
        LambdaQueryWrapper<TemplateDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId, templateId);
        queryWrapper.eq(TemplateDeliveryDocsEntity::getId, deliveryDocsId);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<DocsDTO> getDocsByTaskIdAndTemplateId(String taskId, String templateId) {
        return baseMapper.getDocsByTaskIdAndTemplateId(taskId, templateId);
    }


    /**
     * 根据模板id 获取对应模板数据
     *
     * @param templateIds
     * @return java.util.List<com.erp.model.plm.entity.TemplateDeliveryDocsEntity>
     * @author yl
     * @date 2023-03-08 11:09
     */
    @Override
    public List<TemplateDeliveryDocsEntity> getByTemplateIds(List<String> templateIds) {
        if (CollectionUtils.isEmpty(templateIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<TemplateDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TemplateDeliveryDocsEntity::getTemplateId, templateIds);
        return this.list(queryWrapper);
    }


    /**
     * @param templateId
     * @return List<TemplateDeliveryDocsEntity>
     * @description: 根据模板id查询
     * @author Will
     * @date: 2022/11/14 18:07
     */
    public List<TemplateDeliveryDocsEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId, templateId);
        return this.list(queryWrapper);

    }

    private List<TemplateDeliveryDocsEntity> getExistDocs(String taskId) {
        LambdaQueryWrapper<TemplateDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTaskId, taskId);
        return this.list(queryWrapper);
    }

    /**
     * @param taskId
     * @param templateId
     * @description: 根据任务id和模板id删除
     * @author Will
     * @date: 2022/11/16 10:31
     */
    private void removeTemplateDeliveryDocs(String taskId, String templateId) {
        LambdaQueryWrapper<TemplateDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTaskId, taskId);
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId, templateId);
        this.remove(queryWrapper);
    }

}




