package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.erp.model.plm.dto.TemplateRoleDTO;
import com.erp.model.plm.dto.TemplateRoleShowDTO;
import com.erp.model.plm.dto.TemplateSearchDTO;
import com.erp.model.plm.entity.ProjectRoleEntity;
import com.erp.model.plm.entity.TemplateRoleEntity;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.TemplateRoleMapper;
import com.erp.server.plm.service.ProjectRoleService;
import com.erp.server.plm.service.TemplateRoleService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@Service
public class TemplateRoleServiceImpl extends ServiceImpl<TemplateRoleMapper, TemplateRoleEntity>
        implements TemplateRoleService {

    @Autowired
    private ProjectRoleService projectRoleService;


    //保存模板项目角色
    @Override
    public void saveTemplateRole(String templateId, String productId) {
        List<ProjectRoleEntity> list = projectRoleService.listByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplateRoleEntity> saveList = new ArrayList<>();
            for (ProjectRoleEntity role : list) {
                TemplateRoleEntity entity = new TemplateRoleEntity();
                BeanMapper.copy(role, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
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
        List<CopySourceDTO> sourceList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            List<ProjectRoleEntity> copyList = new ArrayList<>();
            for (TemplateRoleEntity item : list) {
                CopySourceDTO sourceDTO = new CopySourceDTO();
                ProjectRoleEntity entity = new ProjectRoleEntity();
                BeanMapper.copy(item, entity);
                entity.setProjectId(projectId);
                entity.setProductId(productId);
                String id = IdWorker.getIdStr();
                entity.setId(id);
                sourceDTO.setNewCreateId(id);
                sourceDTO.setDataId(item.getId());
                copyList.add(entity);
                sourceList.add(sourceDTO);
            }
            projectRoleService.saveBatch(copyList);
        }
        return sourceList;
    }

    @Override
    public void removeByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleEntity::getTemplateId,templateId);
        this.remove(queryWrapper);
    }

    /**
     * @description: 角色成员列表查询
     * @author Will
     * @date: 2022/11/15 12:28
     * @param dto
     * @return PagingVO<List<TemplateRoleDTO>>
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
        checkRoleName(dto.getName(),dto.getTemplateId());
        TemplateRoleEntity entity = new TemplateRoleEntity();
        BeanMapperUtils.copy(dto,entity);
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


    public List<TemplateRoleEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleEntity::getTemplateId, templateId);
        return this.list(queryWrapper);

    }

    /**
     * @description: 验证该模板下是否存在该角色
     * @author Will
     * @date: 2022/11/15 14:18
     * @param roleName
     * @param tempalteId

     */
    private void checkRoleName(String roleName , String tempalteId) {
        LambdaQueryWrapper<TemplateRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleEntity::getName,roleName);
        queryWrapper.eq(TemplateRoleEntity::getTemplateId,tempalteId);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95059);
        }
    }
}




