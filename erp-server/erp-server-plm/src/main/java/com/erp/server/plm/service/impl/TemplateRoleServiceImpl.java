package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.dto.TemplateRoleDTO;
import com.erp.model.plm.dto.TemplateRoleShowDTO;
import com.erp.model.plm.dto.TemplateSearchDTO;
import com.erp.model.plm.entity.ProjectRoleEntity;
import com.erp.model.plm.entity.TemplateRoleEntity;
import com.erp.server.plm.mapper.TemplateRoleMapper;
import com.erp.server.plm.service.ProjectRoleService;
import com.erp.server.plm.service.TemplateRoleService;
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
public class TemplateRoleServiceImpl extends ServiceImpl<TemplateRoleMapper, TemplateRoleEntity>
        implements TemplateRoleService {

    @Autowired
    private ProjectRoleService projectRoleService;


    /**
     * 产品另存为 模板保存角色
     *
     * @param templateId
     * @param productId
     * @return java.util.List<com.erp.model.plm.dto.CopySourceDTO>
     * @author yl
     * @date 2023-03-08 18:37
     */
    @Override
    public List<CopySourceDTO> saveTemplateRole(String templateId, String productId) {
        List<ProjectRoleEntity> list = projectRoleService.listByProductId(productId);
        List<CopySourceDTO> sourceList = new ArrayList<>(10);

        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplateRoleEntity> saveList = new ArrayList<>();
            for (ProjectRoleEntity role : list) {
                TemplateRoleEntity entity = new TemplateRoleEntity();
                String newCreateId = IdWorker.getIdStr();
                BeanMapper.copy(role, entity);
                entity.setTemplateId(templateId);
                entity.setId(newCreateId);
                saveList.add(entity);
                CopySourceDTO source = new CopySourceDTO();
                source.setNewCreateId(newCreateId);
                source.setDataId(role.getId());
                sourceList.add(source);
            }
            this.saveBatch(saveList);
        }
        return sourceList;
    }


    /**
     * 方法说明
     *
     * @param templateId
     * @param productId
     * @param projectId
     * @return void
     * @author yl
     * @date 2022-10-28 11:24
     */
    @Override
    public List<CopySourceDTO> copyTemplateRole(String templateId, String productId, String projectId) {
        List<TemplateRoleEntity> list = getByTemplateId(templateId);

        List<ProjectRoleEntity> projectRoleByProductId = projectRoleService.listByProductId(productId);

        List<CopySourceDTO> sourceList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            List<ProjectRoleEntity> copyList = new ArrayList<>();
            for (TemplateRoleEntity item : list) {
                ProjectRoleEntity projectRoleEntity = projectRoleByProductId.stream().filter(projectMembers -> projectMembers.getName().equals(item.getName())).findFirst().orElse(null);
                CopySourceDTO sourceDTO = new CopySourceDTO();
                String id = IdWorker.getIdStr();
                sourceDTO.setNewCreateId(id);
                sourceDTO.setDataId(item.getId());

                if (Objects.isNull(projectRoleEntity)) {
                    ProjectRoleEntity entity = new ProjectRoleEntity();
                    BeanMapper.copy(item, entity);
                    entity.setProjectId(projectId);
                    entity.setProductId(productId);
                    entity.setId(id);
                    copyList.add(entity);
                } else {
                    sourceDTO.setNewCreateId(projectRoleEntity.getId());
                }
                sourceList.add(sourceDTO);
            }

            if (CollectionUtils.isNotEmpty(copyList)) {
                projectRoleService.saveBatch(copyList);
            }
        }
        return sourceList;
    }

    @Override
    public void removeByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleEntity::getTemplateId, templateId);
        this.remove(queryWrapper);
    }

    /**
     * @param dto
     * @return PagingVO<List < TemplateRoleDTO>>
     * @description: 角色成员列表查询
     * @author Will
     * @date: 2022/11/15 12:28
     */
    @Override
    public PagingVO<List<TemplateRoleShowDTO>> paging(PagingDTO<TemplateSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        TemplateSearchDTO params = dto.getParams();
        IPage<TemplateRoleShowDTO> paging = baseMapper.paging(query, params);
        return new PagingVO(paging);
    }

    @Override
    public Boolean saveTemplateRole(TemplateRoleDTO dto) {
        //验证角色名称是否已存在
        checkRoleName(dto.getName(), dto.getTemplateId());
        TemplateRoleEntity entity = new TemplateRoleEntity();
        BeanMapperUtils.copy(dto, entity);
        LoginUser loginUser = UserContext.getLoginUser();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.USER_NOT_EXIST);
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
        return this.save(entity);
    }

    @Override
    public Boolean removeByIdAndTemplateId(String roleId, String templateId) {
        LambdaQueryWrapper<TemplateRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleEntity::getId, roleId);
        queryWrapper.eq(TemplateRoleEntity::getTemplateId, templateId);
        return this.remove(queryWrapper);
    }

    @Override
    public List<TemplateRoleEntity> getAllRoles(String templateId) {
        LambdaQueryWrapper<TemplateRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleEntity::getTemplateId, templateId);
        return this.list(queryWrapper);
    }

    @Override
    public List<TemplateRoleEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleEntity::getTemplateId, templateId);
        return this.list(queryWrapper);

    }

    @Override
    public TemplateRoleEntity getByTemplateIdAndRoleId(String templateId, String roleId) {
        LambdaQueryWrapper<TemplateRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleEntity::getTemplateId, templateId);
        queryWrapper.eq(TemplateRoleEntity::getId, roleId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    /**
     * @param roleName
     * @param tempalteId
     * @description: 验证该模板下是否存在该角色
     * @author Will
     * @date: 2022/11/15 14:18
     */
    private void checkRoleName(String roleName, String tempalteId) {
        LambdaQueryWrapper<TemplateRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleEntity::getName, roleName);
        queryWrapper.eq(TemplateRoleEntity::getTemplateId, tempalteId);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95059);
        }
    }
}




