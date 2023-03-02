package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.BasicDictTypeEnum;
import com.erp.model.plm.enums.ProjectTemplateShowTypeEnum;
import com.erp.model.plm.enums.ProjectTemplateTypeEnum;
import com.erp.model.plm.vo.PreTaskListVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.mapper.ProjectTemplateMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Resource
    private BasicDictService basicDictService;
    @Resource
    private TemplateTaskService templateTaskService;
    @Resource
    private ProjectTaskSysService projectTaskSysService;
    @Resource
    private TemplatePreTaskService templatePreTaskService;
    @Resource
    private PreTaskService preTaskService;

    @Override
    public PagingVO<ProjectTemplateDTO> paging(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage<ProjectTemplateDTO> paging = baseMapper.paging(query, params);
        List<ProjectTemplateDTO> list = paging.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(obj -> obj.setTypeName(obj.getIsDefault() == 1 ? ProjectTemplateShowTypeEnum.PROJECT_DEFAULT_TEMPLATE.getName() : ProjectTemplateTypeEnum.getNameByCode(obj.getType())));
        }
        return new PagingVO(paging);
    }

    /**
     * @param dto
     * @return Boolean
     * @description: 列表新增或修改
     * @author Will
     * @date: 2022/11/11 15:42
     */
    @Override
    public Boolean saveOrUpdate(ProjectTemplateSaveOrUpdateDTO dto) {
        //验证模板名称是否已存在
        checkTemplateName(dto.getName(),dto.getId());
        //立项模板
        Integer approvalTemplateCode = ProjectTemplateTypeEnum.APPROVAL_TEMPLATE.getCode();
        Integer templateType = dto.getTemplateType();
        //立项模板产品属性
        String productPropertyId = dto.getProductPropertyId();
        //是否是立项模板
        Boolean isApprovalTemplate = false;
        if (approvalTemplateCode.equals(templateType)) {
            isApprovalTemplate = true;
            //当为空的时候
            if (StringUtils.isBlank(productPropertyId)) {
                throw new ServiceException(ApiError.ERROR_95132);
            }
        }

        ProjectTemplateEntity entity = new ProjectTemplateEntity();
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
        //当是立项模板的时候
        if (isApprovalTemplate) {
            //查询立项模板是否已存在
            ProjectTemplateEntity approvalTemplate = getApprovalTemplate(productPropertyId);
            if (approvalTemplate != null && !approvalTemplate.getId().equals(dto.getId())) {
                throw new ServiceException(ApiError.ERROR_95063);
            }
            entity.setType(templateType);
        }
        if (ProjectTemplateShowTypeEnum.PROJECT_DEFAULT_TEMPLATE.getCode().equals(dto.getTemplateType())) {
            //查询项目默认模板是否已存在
            ProjectTemplateEntity projectDefaultTemplate = getProjectDefaultTemplate();
            if (projectDefaultTemplate != null && !projectDefaultTemplate.getId().equals(dto.getId())) {
                throw new ServiceException(ApiError.ERROR_95064);
            }
            entity.setType(ProjectTemplateTypeEnum.PROJECT_TEMPLATE.getCode());
            entity.setIsDefault(IsConstant.YES);
        } else if (ProjectTemplateShowTypeEnum.PROJECT_CUSTOM_TEMPLATE.getCode().equals(dto.getTemplateType()))  {
            entity.setType(ProjectTemplateTypeEnum.PROJECT_TEMPLATE.getCode());
        }
        return this.saveOrUpdate(entity);
    }

    /**
     * @return ProjectTemplateEntity
     * @description: 查询立项模板
     * @author Will
     * @date: 2022/11/16 16:48
     */
    private ProjectTemplateEntity getApprovalTemplate(String productPropertyId) {
        LambdaQueryWrapper<ProjectTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTemplateEntity::getType, ProjectTemplateTypeEnum.APPROVAL_TEMPLATE.getCode());
        queryWrapper.eq(ProjectTemplateEntity::getProductPropertyId, productPropertyId);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * @return ProjectTemplateEntity
     * @description: 查询项目默认模板
     * @author Will
     * @date: 2022/11/16 16:48
     */
    private ProjectTemplateEntity getProjectDefaultTemplate() {
        LambdaQueryWrapper<ProjectTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTemplateEntity::getType, ProjectTemplateTypeEnum.PROJECT_TEMPLATE.getCode());
        queryWrapper.eq(ProjectTemplateEntity::getIsDefault, IsConstant.YES);
        return this.getOne(queryWrapper);
    }

    /**
     * @param dto
     * @return Boolean
     * @description: 修改模板状态
     * @author Will
     * @date: 2022/11/11 15:42
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
    public List<SysRoleDTO> listTemplateRole(String templateId) {
        List<TemplateRoleEntity> list = templateRoleService.getByTemplateId(templateId);
        List<SysRoleDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        list.forEach(obj -> {
            resultList.add(new SysRoleDTO().setId(obj.getId()).setRoleName(obj.getName()));
        });
        return resultList;
    }

    @Override
    public ProjectTemplateEntity getByType(Integer type) {
        LambdaQueryWrapper<ProjectTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTemplateEntity::getType, type);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }


    /**
     * 获取立项模板的产品属性
     *
     * @param
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @author yl
     * @date 2023-02-21 14:45
     */
    @Override
    public List<Map<String, Object>> getProductPropertyList() {
        String type = BasicDictTypeEnum.PRODUCT_PROPERTY.getCode();
        List<BasicDictEntity> dictList = basicDictService.listByType(type);
        Integer approvalTemplateCode = ProjectTemplateTypeEnum.APPROVAL_TEMPLATE.getCode();

        List<ProjectTemplateEntity> templateList = this.getTemplateByType(approvalTemplateCode);
        List<String> productPropertyIdList = templateList.stream().
                filter(t -> StringUtils.isNotBlank(t.getProductPropertyId())).
                map(ProjectTemplateEntity::getProductPropertyId).collect(Collectors.toList());
        List<Map<String, Object>> resultList = new ArrayList<>(dictList.size());
        for (BasicDictEntity dict : dictList) {
            Map<String, Object> map = new HashMap<>();
            String productPropertyId = dict.getId();
            map.put("productPropertyId", productPropertyId);
            map.put("name", dict.getValue());
            map.put("disable", productPropertyIdList.contains(productPropertyId));
            resultList.add(map);
        }

        return resultList;
    }


    /**
     * 获取立项模板数据
     *
     * @param type
     * @param productPropertyId
     * @return com.erp.model.plm.entity.ProjectTemplateEntity
     * @author yl
     * @date 2023-02-21 16:03
     */
    @Override
    public ProjectTemplateEntity getApprovalTemplate(Integer type, String productPropertyId) {
        LambdaQueryWrapper<ProjectTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTemplateEntity::getType, type);
        queryWrapper.eq(ProjectTemplateEntity::getProductPropertyId, productPropertyId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }


    /**
     * 获取默认的模板 项目模板【默认】
     *
     * @param
     * @return com.erp.model.plm.entity.ProjectTemplateEntity
     * @author yl
     * @date 2023-02-21 17:41
     */
    @Override
    public ProjectTemplateEntity getDefaultTemplate() {
        LambdaQueryWrapper<ProjectTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTemplateEntity::getIsDefault, IsConstant.YES);
        queryWrapper.eq(ProjectTemplateEntity::getStatus, IsConstant.YES);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public List<PreTaskListVO> ListPreTaskByTaskId(TemplatePreTaskDTO dto) {
        // 根据id查模板类型
        ProjectTemplateEntity templateEntity = lambdaQuery()
                .eq(ProjectTemplateEntity::getId, dto.getTemplateId())
                .one();
        if(null == templateEntity){
            throw new ServiceException(ApiError.ERROR_95051);
        }
        List<PreTaskListVO>  preTaskList;
        // 根据模板类型查询模板及前置任务
        if(ProjectTemplateTypeEnum.PROJECT_TEMPLATE.getCode().equals(templateEntity.getType()) && 0 == templateEntity.getIsDefault()){
            // 模板表
            preTaskList = templatePreTaskService.getTemplatePreAndNameById(dto.getTaskId());
        }else {
            // 系统表
            preTaskList = preTaskService.ListSysPreTaskByTaskId(dto.getTaskId());
        }
        if(CollectionUtil.isEmpty(preTaskList)){
            return preTaskList;
        }
        preTaskList.stream().forEach(x -> {
            if(null != x.getRelationship()){
                x.setRelationshipName(x.getRelationship().getName());
                x.setRelationshipCode(x.getRelationship().getCode());
            }
        });
        //返回数据格式化
        return preTaskList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePreTask(PreTemplateTaskUpdateDTO dto) {

        // 根据任务id查询模板类型
        ProjectTemplateEntity templateEntity = lambdaQuery()
                .eq(ProjectTemplateEntity::getId, dto.getTemplateId())
                .one();
        if(null == templateEntity){
            throw new ServiceException(ApiError.ERROR_95051);
        }
        // 根据模板类型查询模板及前置任务
        boolean result = Boolean.FALSE;
        List<PreTaskUpdateDTO> list = dto.getList();
        if(ProjectTemplateTypeEnum.PROJECT_TEMPLATE.getCode().equals(templateEntity.getType()) && 0 == templateEntity.getIsDefault()){
            // 更新前置任务
            List<TemplatePreTaskEntity> updateList = list.stream().map(TemplatePreTaskEntity::new).collect(Collectors.toList());
            result = templatePreTaskService.updateBatchById(updateList);
        }else {
            // 更新前置任务
            List<PreTaskEntity> updateList = list.stream().map(PreTaskEntity::new).collect(Collectors.toList());
            result = preTaskService.updateBatchById(updateList);
        }
        if (!result){
            throw new ServiceException(ApiError.ERROR_95151);
        }
        return Boolean.TRUE;
    }


    /**
     * 根据模板类型获取模板
     *
     * @param type
     * @return
     * @author yl
     * @date 2023-02-21 15:35
     */
    public List<ProjectTemplateEntity> getTemplateByType(Integer type) {
        LambdaQueryWrapper<ProjectTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTemplateEntity::getType, type);
        return this.list(queryWrapper);
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
    public String saveTemplate(String templateName, String productId, Integer templateType) {
        checkTemplateName(templateName,"");
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
    private void checkTemplateName(String templateName,String id) {
        LambdaQueryWrapper<ProjectTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTemplateEntity::getName, templateName);
        if(StringUtils.isNotBlank(id)){
            queryWrapper.ne(ProjectTemplateEntity::getId, id);
        }
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95011);
        }
    }


}
