package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.common.business.interceptor.CommonInterceptor;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectTemplateEntity;
import com.erp.model.plm.entity.TemplateRoleEntity;
import com.erp.model.sys.dto.UserDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.enums.ProjectTemplateShowTypeEnum;
import com.erp.server.plm.enums.ProjectTemplateTypeEnum;
import com.erp.server.plm.mapper.ProjectTemplateMapper;
import com.erp.server.plm.service.ProjectTemplateService;
import com.erp.server.plm.service.TemplateRoleService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 项目模板信息 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class ProjectTemplateServiceImpl extends ServiceImpl<ProjectTemplateMapper, ProjectTemplateEntity> implements ProjectTemplateService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private TemplateRoleService templateRoleService;

    @Override
    public PagingVO<ProjectTemplateDTO> paging(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage<ProjectTemplateDTO> paging = baseMapper.paging(query, params);
        List<ProjectTemplateDTO> list = paging.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(obj-> obj.setTypeName( obj.getIsDefault() == 1 ? ProjectTemplateTypeEnum.getNameByCode(obj.getType()).concat("【默认】") : ProjectTemplateTypeEnum.getNameByCode(obj.getType()) ));
        }
        return new PagingVO(paging);
    }

    /**
     * @description: 列表新增或修改
     * @author Will
     * @date: 2022/11/11 15:42
     * @param dto
     * @return Boolean
     */
    @Override
    public Boolean saveOrUpdate(ProjectTemplateSaveOrUpdateDTO dto) {
        //验证模板名称是否已存在
        checkTemplateName(dto.getName());
        ProjectTemplateEntity entity = new ProjectTemplateEntity();
        BeanMapperUtils.copy(dto,entity);
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
            //模板管理新增模板默认启动
            if (entity.getStatus() == null) {
                entity.setStatus(IsConstant.YES);
            }
        } else {
            entity.setUpdateUserId(uid);
            entity.setUpdateUserName(userName);
        }
        //先设置成非默认，项目模板
        entity.setIsDefault(IsConstant.NO);
        entity.setType(ProjectTemplateTypeEnum.PROJECT_TEMPLATE.getCode());
        if (ProjectTemplateShowTypeEnum.APPROVAL_TEMPLATE.getCode().equals(dto.getTemplateType()) ) {
            //查询立项模板是否已存在
            ProjectTemplateEntity approvalTemplate = getApprovalTemplate();
            if (approvalTemplate != null && !approvalTemplate.getId().equals(dto.getId())) {
                throw new ServiceException(ApiError.ERROR_95063);
            }
            entity.setType(ProjectTemplateTypeEnum.APPROVAL_TEMPLATE.getCode());
        }
        if (ProjectTemplateShowTypeEnum.PROJECT_DEFAULT_TEMPLATE.getCode().equals(dto.getTemplateType()) ) {
            //查询项目默认模板是否已存在
            ProjectTemplateEntity projectDefaultTemplate = getProjectDefaultTemplate();
            if (projectDefaultTemplate != null && !projectDefaultTemplate.getId().equals(dto.getId())) {
                throw new ServiceException(ApiError.ERROR_95064);
            }
            entity.setType(ProjectTemplateTypeEnum.PROJECT_TEMPLATE.getCode());
            entity.setIsDefault(IsConstant.YES);
        }
        return this.saveOrUpdate(entity);
    }

    /**
     * @description: 查询立项模板
     * @author Will
     * @date: 2022/11/16 16:48
     * @return ProjectTemplateEntity
     */
    private ProjectTemplateEntity getApprovalTemplate(){
        LambdaQueryWrapper<ProjectTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTemplateEntity::getType,ProjectTemplateTypeEnum.APPROVAL_TEMPLATE.getCode());
        return this.getOne(queryWrapper);
    }

    /**
     * @description: 查询项目默认模板
     * @author Will
     * @date: 2022/11/16 16:48
     * @return ProjectTemplateEntity
     */
    private ProjectTemplateEntity getProjectDefaultTemplate(){
        LambdaQueryWrapper<ProjectTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTemplateEntity::getType,ProjectTemplateTypeEnum.PROJECT_TEMPLATE.getCode());
        queryWrapper.eq(ProjectTemplateEntity::getIsDefault, IsConstant.YES);
        return this.getOne(queryWrapper);
    }

    /**
     * @description: 修改模板状态
     * @author Will
     * @date: 2022/11/11 15:42
     * @param dto
     * @return Boolean
     */
    @Override
    public Boolean updateTemplateStatus(ProjectTemplateUpdateStatusDTO dto) {
        ProjectTemplateEntity entity = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95051);
        }
        //获取登录人信息
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        String uid = loginUser.getUid();
        String userName = loginUser.getUserName();
        entity.setStatus(dto.getStatus());
        entity.setUpdateUserId(uid);
        entity.setUpdateUserName(userName);
        return this.updateById(entity);
    }

    @Override
    public List<UserDTO> listSuperior(String id, Integer type) {
        List<UserDTO> userList = new ArrayList<>();
        //根据模板角色id查询角色名称
        if (MathUtil.ZERO.equals(type)) {
            TemplateRoleEntity templateRoleEntity = templateRoleService.getById(id);
            if (ObjectUtils.isNotEmpty(templateRoleEntity)) {
                //负责人类型为角色
                userList = sysUserFeign.listSuperiorByRoleName(templateRoleEntity.getName());
            }
        } else if (MathUtil.ONE.equals(type)) {
            //负责人类型为人员
           userList = sysUserFeign.listSuperiorByUserId(id);
        }
        return userList;
    }

    @Override
    public List<SysRoleDTO> listTemplateRole(String templateId) {
        List<TemplateRoleEntity> list = templateRoleService.getByTemplateId(templateId);
        List<SysRoleDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return  new ArrayList<>();
        }
        list.forEach(obj->{
            resultList.add(new SysRoleDTO().setId(obj.getId()).setRoleName(obj.getName()));
        });
        return resultList;
    }

    @Override
    public ProjectTemplateEntity getByType(Integer type) {
        LambdaQueryWrapper<ProjectTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTemplateEntity::getType,type);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 保存模板 返回模板id
     *
     * @param templateName
     * @return java.lang.String
     * @author yl
     * @date 2022-09-20 14:30
     */
    @Override
    public String saveTemplate(String templateName,String productId,Integer templateType) {
        checkTemplateName(templateName);
        //获取登录人信息
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        String uid = loginUser.getUid();
        String userName = loginUser.getUserName();
        ProjectTemplateEntity entity = new ProjectTemplateEntity();
        entity.setName(templateName);
        entity.setProductId(productId);
        entity.setType(templateType);
        entity.setCreateUserId(uid);
        entity.setCreateUserName(userName);
        if (this.save(entity)) {
            return entity.getId();
        }
        return "";

    }

    @Override
    public List<StartItemSourceDTO> startItemSource(Integer sourceType) {
        return baseMapper.getStartItemSource(sourceType);
    }



    /**
     * 检查模板名
     *
     * @return
     * @author yl
     * @date 2022-09-20 14:34
     */
    private void checkTemplateName(String templateName) {
        LambdaQueryWrapper<ProjectTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTemplateEntity::getName, templateName);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95011);
        }
    }


}
