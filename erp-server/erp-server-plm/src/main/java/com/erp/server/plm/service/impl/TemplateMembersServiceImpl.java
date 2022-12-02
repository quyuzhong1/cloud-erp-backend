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
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.dto.TemplateMembersAddOrUpdateDTO;
import com.erp.model.plm.dto.TemplateMembersDTO;
import com.erp.model.plm.dto.TemplateRoleMembersDeleteDTO;
import com.erp.model.plm.entity.ProjectMembersEntity;
import com.erp.model.plm.entity.TemplateMembersEntity;
import com.erp.model.plm.entity.TemplateRoleRefMembersEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.TemplateMembersMapper;
import com.erp.server.plm.service.ProjectMembersService;
import com.erp.server.plm.service.TemplateMembersService;
import com.erp.server.plm.service.TemplateRoleRefMembersService;
import com.erp.server.plm.service.TemplateRoleService;
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
    private TemplateRoleService templateRoleService;

    @Autowired
    private TemplateMembersService templateMembersService;

    @Autowired
    private TemplateMembersMapper templateMembersMapper;

    @Autowired
    private SysUserFeign sysUserFeign;


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
    public List<CopySourceDTO> copyTemplateMembers(String templateId, String productId, String projectId) {
        List<TemplateMembersEntity> templateMembers = getByTemplateId(templateId);
        List<CopySourceDTO> sourceList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(templateMembers)) {
            List<ProjectMembersEntity> copyList = new ArrayList<>();
            for (TemplateMembersEntity item : templateMembers) {
                ProjectMembersEntity entity = new ProjectMembersEntity();
                CopySourceDTO sourceDTO = new CopySourceDTO();
                BeanMapper.copy(item, entity);
                entity.setProductId(productId);
                entity.setProjectId(projectId);
                entity.setId(IdWorker.getIdStr());
                sourceDTO.setNewCreateId(entity.getId());
                sourceDTO.setDataId(item.getId());
                sourceList.add(sourceDTO);
                copyList.add(entity);
            }
            projectMembersService.saveBatch(copyList);

        }
        return sourceList;
    }

    @Override
    public Boolean removeByIdAndTemplateId(String id,String templateId) {
        LambdaQueryWrapper<TemplateMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateMembersEntity::getTemplateId,templateId);
        queryWrapper.eq(TemplateMembersEntity::getId,id);
        return this.remove(queryWrapper);
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
    public Boolean saveTemplateMembers(TemplateMembersAddOrUpdateDTO dto) {
        List<TemplateMembersDTO> membersDtoList = dto.getMembersList();
        if (CollectionUtils.isEmpty(membersDtoList)) {
            throw new ServiceException(ApiError.ERROR_95060);
        }
        //角色和成员关联表数据集合
        List<TemplateRoleRefMembersEntity> roleRefMembersList = new ArrayList<>();
        //根据角色id和模板id查询成员看是否已存在该成员
        List<TemplateMembersEntity> membersList = templateMembersMapper.getMembersByRoleIdAndTemplateId(dto.getId(), dto.getTemplateId());
        if (CollectionUtils.isNotEmpty(membersList)) {
            //录入成员
            List<String> collect1 = membersDtoList.stream().map(TemplateMembersDTO::getMemberId).collect(Collectors.toList());
            //已存在成员
            List<String> collect2 = membersList.stream().map(TemplateMembersEntity::getMemberId).collect(Collectors.toList());
            List<String> intersectionList = (List<String>) CollectionUtils.intersection(collect1, collect2);
            //表示有交集不能再次生成
            if (CollectionUtils.isNotEmpty(intersectionList)) {
                throw new ServiceException(ApiError.ERROR_95061);
            }
        }
        //获取登录人信息
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        String uid = loginUser.getUid();
        String userName = loginUser.getUserName();
        List<TemplateMembersEntity> membersEntityList = BeanMapper.copyList(membersDtoList, TemplateMembersEntity.class);
        List<String> memberIdList = membersEntityList.stream().map(TemplateMembersEntity::getMemberId).collect(Collectors.toList());
        //根据成员id集合查询
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(memberIdList);
        if (CollectionUtils.isEmpty(userList)) {
            throw  new ServiceException(ApiError.ERROR_9011);
        }
        membersEntityList.stream().forEach(obj->{
            //成员数据处理
            String memberName = userList.stream().filter(e -> e.getUserId().equals(obj.getMemberId())).map(FindUserDTO::getUserName).findAny().orElse(null);
            obj.setMemberName(memberName);
            obj.setTemplateId(dto.getTemplateId());
            obj.setCreateUserId(uid);
            obj.setCreateUserName(userName);
        });
        boolean flag = this.saveBatch(membersEntityList);
        if (!flag) {
            throw new ServiceException(ApiError.Default);
        }
        membersEntityList.stream().forEach(obj->{
            //关联表数据处理
            TemplateRoleRefMembersEntity roleRefMembersEntity = new TemplateRoleRefMembersEntity();
            roleRefMembersEntity.setCreateUserId(uid);
            roleRefMembersEntity.setCreateUserName(userName);
            roleRefMembersEntity.setRoleId(dto.getId());
            roleRefMembersEntity.setMembersId(obj.getId());
            roleRefMembersEntity.setTemplateId(dto.getTemplateId());
            roleRefMembersList.add(roleRefMembersEntity);
        });
        //新增角色和成员关联表数据
        boolean add = templateRoleRefMembersService.saveBatch(roleRefMembersList);
        return add;
    }

    @Override
    @Transactional
    public Boolean updateTemplateMembers(TemplateMembersAddOrUpdateDTO dto) {
        List<TemplateMembersDTO> membersDtoList = dto.getMembersList();
        if (CollectionUtils.isEmpty(membersDtoList)) {
            throw new ServiceException(ApiError.ERROR_95060);
        }
        //编辑时成员仅有一个
        TemplateMembersDTO templateMembersDTO = membersDtoList.stream().findFirst().orElse(null);
        //根据角色id和模板id查询成员看是否已存在该成员
        List<TemplateMembersEntity> membersList = templateMembersMapper.getMembersByRoleIdAndTemplateId(dto.getId(), dto.getTemplateId());
        if (CollectionUtils.isNotEmpty(membersList)) {
            long count = membersList.stream().filter(obj -> obj.getMemberId().equals(templateMembersDTO.getMemberId()) && !obj.getId().equals(templateMembersDTO.getId())).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_95061);
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
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(templateMembersDTO.getMemberId());
        if (ObjectUtils.isEmpty(findUserDTO)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        templateMembersEntity.setMemberName(findUserDTO.getUserName());
        templateMembersEntity.setTemplateId(dto.getTemplateId());
        templateMembersEntity.setUpdateUserId(uid);
        templateMembersEntity.setUpdateUserName(userName);
        return this.updateByTemplateId(templateMembersEntity);
    }

    @Override
    @Transactional
    public Boolean deleteTemplateMembers(TemplateRoleMembersDeleteDTO dto) {

        TemplateRoleRefMembersEntity roleRefMembers = templateRoleRefMembersService.getByIdAndTemplateId(dto.getRoleRefMembersId(), dto.getTemplateId());
        if (roleRefMembers == null) {
            throw new ServiceException(ApiError.Default);
        }
        //删除成员表信息
        Boolean templateMembersRemove = templateMembersService.removeByIdAndTemplateId(roleRefMembers.getMembersId(), roleRefMembers.getTemplateId());
        if (!templateMembersRemove) {
            throw new ServiceException(ApiError.Default);
        }
        //删除角色成员关联表信息
        Boolean roleRefMembersRemove = templateRoleRefMembersService.removeByIdAndTemplateId(dto.getRoleRefMembersId(), dto.getTemplateId());
        if (!roleRefMembersRemove) {
            throw new ServiceException(ApiError.Default);
        }
        //如果角色表下面没有成员信息则删除角色
        List<TemplateRoleRefMembersEntity> list = templateRoleRefMembersService.getByRoleIdAndTemplateId(roleRefMembers.getRoleId(), roleRefMembers.getTemplateId());
        if (CollectionUtils.isEmpty(list)) {
            Boolean templateRoleRemove = templateRoleService.removeByIdAndTemplateId(roleRefMembers.getRoleId(), roleRefMembers.getTemplateId());
            if (!templateRoleRemove) {
                throw new ServiceException(ApiError.Default);
            }
        }
        return true;
    }

    @Override
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




