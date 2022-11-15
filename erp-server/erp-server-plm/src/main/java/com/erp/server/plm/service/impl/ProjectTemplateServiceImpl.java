package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.ProjectTemplateDTO;
import com.erp.model.plm.dto.StartItemSourceDTO;
import com.erp.model.plm.entity.ProjectTemplateEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.enums.ProjectTemplateTypeEnum;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProjectTemplateMapper;
import com.erp.server.plm.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private TemplateTaskService templateTaskService;

    @Autowired
    private TemplateDeliveryDocsService templateDeliveryDocsService;

    @Autowired
    private TemplateMembersService templateMembersService;

    @Autowired
    private TemplateRoleService templateRoleService;

    @Autowired
    private TemplateRoleRefMembersService templateRoleRefMembersService;


    @Override
    public PagingVO<ProjectTemplateDTO> paging(PagingDTO<ProjectTemplateDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        ProjectTemplateDTO params = dto.getParams();
        IPage<ProjectTemplateEntity> paging = baseMapper.paging(query, params);
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
    public Boolean saveOrUpdate(ProjectTemplateDTO dto) {
        //验证模板名称是否已存在
        checkTemplateName(dto.getName());
        ProjectTemplateEntity entity = new ProjectTemplateEntity();
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
            //模板管理新增模板默认启动，并且类型为项目模板
            if (entity.getStatus() == null) {
                entity.setStatus(IsConstant.YES);
            }
            if (entity.getType() == null) {
                entity.setType(ProjectTemplateTypeEnum.PROJECT_TEMPLATE.getCode());
            }
        } else {
            entity.setUpdateUserId(uid);
            entity.setUpdateUserName(userName);
        }
        return this.saveOrUpdate(entity);
    }

    /**
     * @description: 修改模板状态
     * @author Will
     * @date: 2022/11/11 15:42
     * @param dto
     * @return Boolean
     */
    @Override
    public Boolean updateTemplateStatus(ProjectTemplateDTO dto) {
        ProjectTemplateEntity entity = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95051);
        }
        entity.setStatus(dto.getStatus());
        return this.updateById(entity);
    }

    @Override
    public Boolean removeTemplate(String id) {
        ProjectTemplateEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95051);
        }
        //删除模板输出物
        templateDeliveryDocsService.removeByTemplateId(id);
        //删除成员
        templateMembersService.removeByTemplateId(id);
        //删除角色
        templateRoleService.removeByTemplateId(id);
        //删除角色和成员关系
        templateRoleRefMembersService.removeByTemplateId(id);
        //删除模板任务
        templateTaskService.removeByTemplateId(id);
        return this.removeById(id);
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
        ProjectTemplateEntity entity = new ProjectTemplateEntity();
        entity.setName(templateName);
        entity.setProductId(productId);
        entity.setType(templateType);
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
