package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.TemplateMembersDTO;
import com.erp.model.plm.dto.TemplateRoleDTO;
import com.erp.model.plm.entity.ProjectMembersEntity;
import com.erp.model.plm.entity.TemplateMembersEntity;
import com.erp.model.plm.entity.TemplateRoleRefMembersEntity;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.TemplateMembersMapper;
import com.erp.server.plm.service.ProjectMembersService;
import com.erp.server.plm.service.TemplateMembersService;
import com.erp.server.plm.service.TemplateRoleRefMembersService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 *
 */
@Service
public class TemplateMembersServiceImpl extends ServiceImpl<TemplateMembersMapper, TemplateMembersEntity>
        implements TemplateMembersService {

    @Autowired
    private ProjectMembersService projectMembersService;

    @Autowired
    private TemplateRoleRefMembersService templateRoleRefMembersService;


    @Autowired
    private TemplateMembersMapper templateMembersMapper;


    /**
     * 保存 模板成员
     *
     * @param templateId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-27 15:12
     */
    @Override
    public void saveMember(String templateId, String productId) {
        List<ProjectMembersEntity> list = projectMembersService.getListByProductId(productId);
        List<TemplateMembersEntity> saveList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (ProjectMembersEntity item : list) {
                TemplateMembersEntity entity = new TemplateMembersEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }


    /**
     * 从模板复制成员
     *
     * @param templateId
     * @param productId
     * @param projectId
     * @return void
     * @author yl
     * @date 2022-10-28 11:13
     */
    @Override
    public void copyTemplateMembers(String templateId, String productId, String projectId) {
        List<TemplateMembersEntity> templateMembers = getByTemplateId(templateId);
        if (CollectionUtils.isNotEmpty(templateMembers)) {
            List<ProjectMembersEntity> copyList = new ArrayList<>();
            for (TemplateMembersEntity item : templateMembers) {
                ProjectMembersEntity entity = new ProjectMembersEntity();
                BeanMapper.copy(item, entity);
                entity.setProductId(productId);
                entity.setProjectId(projectId);
                entity.setId(IdWorker.getIdStr());
                copyList.add(entity);
            }
            projectMembersService.saveBatch(copyList);

        }
    }

    @Override
    public void removeByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateMembersEntity::getTemplateId,templateId);
        this.remove(queryWrapper);
    }

    @Override
    public Boolean ifTemplateMember(String userId, String templateId) {
        LambdaQueryWrapper<TemplateMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateMembersEntity::getTemplateId, templateId);
        queryWrapper.eq(TemplateMembersEntity::getMemberId, userId);
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    @Transactional
    public Boolean saveTemplateMembers(TemplateRoleDTO dto) {
        List<TemplateMembersDTO> membersDtoList = dto.getMembersList();
        if (CollectionUtils.isEmpty(membersDtoList)) {
            throw new ServiceException(ApiError.ERROR_95056);
        }
        //角色和成员关联表数据集合
        List<TemplateRoleRefMembersEntity> roleRefMembersList = new ArrayList<>();
        //根据角色id和模板id查询成员看是否已存在该成员
        List<TemplateMembersEntity> membersList = templateMembersMapper.getMembersByRoleIdAndTemplateId(dto.getId(), dto.getTemplateId());
        if (CollectionUtils.isNotEmpty(membersList)) {
            //录入成员
            List<String> collect1 = membersDtoList.stream().map(TemplateMembersDTO::getMemberName).collect(Collectors.toList());
            //已存在成员
            List<String> collect2 = membersList.stream().map(TemplateMembersEntity::getMemberName).collect(Collectors.toList());
            List<String> intersectionList = (List<String>) CollectionUtils.intersection(collect1, collect2);
            //表示有交集不能再次生成
            if (CollectionUtils.isNotEmpty(intersectionList)) {
                throw new ServiceException(ApiError.valueOf("成员："+intersectionList.toString()+"已存在,不能重复新增"));
            }
        }
        //获取登录人信息
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        String uid = loginUser.getUid();
        String userName = loginUser.getUserName();
        membersDtoList.stream().forEach(obj->{
            TemplateRoleRefMembersEntity roleRefMembersEntity = new TemplateRoleRefMembersEntity();
            roleRefMembersEntity.setCreateUserId(uid);
            roleRefMembersEntity.setCreateUserName(userName);
            roleRefMembersEntity.setRoleId(dto.getId());
            roleRefMembersEntity.setMembersId(obj.getId());
            roleRefMembersEntity.setTemplateId(dto.getTemplateId());
            roleRefMembersList.add(roleRefMembersEntity);
        });
        //新增角色和成员关联表数据
        boolean flag = templateRoleRefMembersService.saveBatch(roleRefMembersList);
        if (!flag) {
            throw new ServiceException(ApiError.Default);
        }
        List<TemplateMembersEntity> membersEntityList = BeanMapper.copyList(membersDtoList, TemplateMembersEntity.class);
        return this.saveBatch(membersEntityList);
    }

    @Override
    public Boolean updateTemplateMembers(TemplateRoleDTO dto) {
        List<TemplateMembersDTO> membersDtoList = dto.getMembersList();
        if (CollectionUtils.isEmpty(membersDtoList)) {
            throw new ServiceException(ApiError.ERROR_95056);
        }
        //编辑时成员仅有一个
        TemplateMembersDTO templateMembersDTO = membersDtoList.stream().findFirst().orElse(null);
        //根据角色id和模板id查询成员看是否已存在该成员
        List<TemplateMembersEntity> membersList = templateMembersMapper.getMembersByRoleIdAndTemplateId(dto.getId(), dto.getTemplateId());
        if (CollectionUtils.isNotEmpty(membersList)) {
            long count = membersList.stream().filter(obj -> obj.getMemberId().equals(templateMembersDTO.getMemberId()) && !obj.getId().equals(templateMembersDTO.getId())).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_95057);
            }
        }
        //获取登录人信息
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        String uid = loginUser.getUid();
        String userName = loginUser.getUserName();
        TemplateRoleRefMembersEntity templateRoleRefMembersEntity = templateRoleRefMembersService.getByIdAndTemplateId(dto.getRoleRefMembersId(), dto.getTemplateId());
        templateRoleRefMembersEntity.setUpdateUserId(uid);
        templateRoleRefMembersEntity.setUpdateUserName(userName);
        templateRoleRefMembersEntity.setRoleId(dto.getId());
        templateRoleRefMembersEntity.setMembersId(templateMembersDTO.getId());
        //修改角色成员关联数据
        Boolean flag = templateRoleRefMembersService.updateByTemplateId(templateRoleRefMembersEntity);
        if (!flag) {
            throw new ServiceException(ApiError.Default);
        }
        //根据id和模板id修改
        TemplateMembersEntity templateMembersEntity = new TemplateMembersEntity();
        BeanMapperUtils.copy(templateMembersDTO,templateMembersEntity);
        templateMembersEntity.setUpdateUserId(uid);
        templateMembersEntity.setUpdateUserName(userName);
        return this.updateByTemplateId(templateMembersEntity);
    }

    @Override
    public Boolean deleteTemplateMembers(TemplateRoleDTO dto) {


        return null;
    }


    public List<TemplateMembersEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateMembersEntity::getTemplateId, templateId);
        return this.list(queryWrapper);
    }

    /**
     * @description: 根据id和模板id修改
     * @author Will
     * @date: 2022/11/15 15:51
     * @param templateMembersEntity
     * @return Boolean
     */
    private Boolean updateByTemplateId(TemplateMembersEntity templateMembersEntity) {
        LambdaUpdateWrapper<TemplateMembersEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TemplateMembersEntity::getTemplateId, templateMembersEntity.getTemplateId());
        updateWrapper.eq(TemplateMembersEntity::getId, templateMembersEntity.getId());
        updateWrapper.set(TemplateMembersEntity::getMemberId,templateMembersEntity.getMemberId());
        updateWrapper.set(TemplateMembersEntity::getMemberName,templateMembersEntity.getMemberName());
        updateWrapper.set(TemplateMembersEntity::getUpdateUserId,templateMembersEntity.getUpdateUserId());
        updateWrapper.set(TemplateMembersEntity::getUpdateUserName,templateMembersEntity.getUpdateUserName());
        return this.update(updateWrapper);
    }
}




