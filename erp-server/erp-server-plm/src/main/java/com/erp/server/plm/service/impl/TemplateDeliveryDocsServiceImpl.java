package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.dto.TemplateDeliveryDocsDTO;
import com.erp.model.plm.entity.TaskDeliveryDocsEntity;
import com.erp.model.plm.entity.TemplateDeliveryDocsEntity;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.TemplateDeliveryDocsMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 *
 */
@Service
public class TemplateDeliveryDocsServiceImpl extends ServiceImpl<TemplateDeliveryDocsMapper, TemplateDeliveryDocsEntity>
        implements TemplateDeliveryDocsService {


    @Autowired
    private TaskDeliveryService taskDeliveryService;

    @Autowired
    private TemplateTaskService templateTaskService;

    @Autowired
    private TemplateRoleRefMembersService templateRoleRefMembersService;

    @Autowired
    private TemplateDocsPermissionService templateDocsPermissionService;

    @Autowired
    private TemplateMembersService templateMembersService;

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
    public void saveTemplateDeliveryDocs(String templateId, String productId) {
        List<TaskDeliveryDocsEntity> list = taskDeliveryService.getByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplateDeliveryDocsEntity> saveList = new ArrayList<>();
            for (TaskDeliveryDocsEntity item : list) {
                TemplateDeliveryDocsEntity entity = new TemplateDeliveryDocsEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
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
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTaskId,taskId);
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId,templateId);
        this.remove(queryWrapper);
    }

    @Override
    public void removeByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId, templateId);
        this.remove(queryWrapper);
    }

    @Override
    public PagingVO<List<TemplateDeliveryDocsEntity>> paging(PagingDTO<TemplateDeliveryDocsDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        TemplateDeliveryDocsDTO params = dto.getParams();
        List<String> findDeliveryDocsIds = new ArrayList<>();
        IPage<TemplateDeliveryDocsEntity> pageData = baseMapper.paging(query, params, findDeliveryDocsIds);
        return new PagingVO(pageData);
    }

    @Override
    public Boolean saveOrUpdate(TemplateDeliveryDocsDTO dto) {
        TemplateDeliveryDocsEntity entity = new TemplateDeliveryDocsEntity();
        BeanMapperUtils.copy(dto,entity);
        //获取登录人信息
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
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
        //因为模板任务无主键，则无法用saveOrUpdate进行操作
        if (StringUtils.isBlank(entity.getId())) {
            return this.save(entity);
        }
        LambdaUpdateWrapper<TemplateDeliveryDocsEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TemplateDeliveryDocsEntity::getId,entity.getId());
        updateWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId,entity.getTemplateId());
        updateWrapper.set(TemplateDeliveryDocsEntity::getDocsName,entity.getDocsName());
        return this.update(updateWrapper);
    }

    @Override
    public Boolean deleteByTempalteId(String id, String templateId) {
        LambdaQueryWrapper<TemplateDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateDeliveryDocsEntity::getId,id);
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId,templateId);
        return this.remove(queryWrapper);
    }


    /**
     * @description: 根据模板id查询
     * @author Will
     * @date: 2022/11/14 18:07
     * @param templateId
     * @return List<TemplateDeliveryDocsEntity>
     */
    public List<TemplateDeliveryDocsEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId, templateId);
        return this.list(queryWrapper);

    }


}




