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
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.ProjectTemplateEntity;
import com.erp.model.plm.entity.TemplatePreTaskEntity;
import com.erp.model.plm.entity.TemplateRoleEntity;
import com.erp.model.plm.enums.BasicDictTypeEnum;
import com.erp.model.plm.vo.PreTaskListVO;
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
    private TemplateRoleService templateRoleService;

    @Resource
    private BasicDictService basicDictService;

    @Resource
    private TemplatePreTaskService templatePreTaskService;


    @Resource
    private TemplateRefPropertyService templateRefPropertyService;

    @Override
    public PagingVO<ProjectTemplateDTO> paging(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage<ProjectTemplateDTO> paging = baseMapper.paging(query, params);
        List<ProjectTemplateDTO> list = paging.getRecords();
        List<String> templateIdList = list.stream().map(ProjectTemplateDTO::getId).collect(Collectors.toList());
        //根据模板id
        List<TemplatePropertyDTO> templatePropertyList = templateRefPropertyService.getByTemplateIds(templateIdList);
        for (ProjectTemplateDTO item : list) {
            List<TemplatePropertyDTO> propertyList = templatePropertyList.stream().
                    filter(temp -> temp.getTemplateId().equals(item.getId())).collect(Collectors.toList());
            List<String> productPropertyIdList = propertyList.stream().map(TemplatePropertyDTO::getProductPropertyId).collect(Collectors.toList());
            item.setProductPropertyIdList(productPropertyIdList);
            List<String> productPropertyValueList = propertyList.stream().map(TemplatePropertyDTO::getProductPropertyValue).collect(Collectors.toList());
            item.setProductPropertyValues(String.join(",", productPropertyValueList));
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdate(ProjectTemplateSaveOrUpdateDTO dto) {
        //验证模板名称是否已存在
        checkTemplateName(dto.getName(), dto.getId());

        //产品属性id
        List<String> productPropertyIdList = dto.getProductPropertyIdList();

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
        boolean result = this.saveOrUpdate(entity);
        if (result) {
            //保存模板与产品属性的关系表
            templateRefPropertyService.saveRef(entity.getId(), productPropertyIdList);
        }
        return result;
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
        List<Map<String, Object>> resultList = new ArrayList<>(dictList.size());
        for (BasicDictEntity dict : dictList) {
            Map<String, Object> map = new HashMap<>();
            String productPropertyId = dict.getId();
            map.put("productPropertyId", productPropertyId);
            map.put("name", dict.getValue());
            resultList.add(map);
        }

        return resultList;
    }


    @Override
    public List<PreTaskListVO> ListPreTaskByTaskId(TemplatePreTaskDTO dto) {
        // 根据id查模板类型
        ProjectTemplateEntity templateEntity = lambdaQuery()
                .eq(ProjectTemplateEntity::getId, dto.getTemplateId())
                .one();
        if (null == templateEntity) {
            throw new ServiceException(ApiError.ERROR_95051);
        }
        List<PreTaskListVO> preTaskList;

        // 模板表
        preTaskList = templatePreTaskService.getTemplatePreAndNameById(dto.getTaskId());

        if (CollectionUtil.isEmpty(preTaskList)) {
            return preTaskList;
        }
        preTaskList.stream().forEach(x -> {
            if (null != x.getRelationship()) {
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
        if (null == templateEntity) {
            throw new ServiceException(ApiError.ERROR_95051);
        }
        // 根据模板类型查询模板及前置任务
        boolean result = Boolean.FALSE;
        List<PreTaskUpdateDTO> list = dto.getList();
        // 更新前置任务
        List<TemplatePreTaskEntity> updateList = list.stream().map(TemplatePreTaskEntity::new).collect(Collectors.toList());
        result = templatePreTaskService.updateBatchById(updateList);
        if (!result) {
            throw new ServiceException(ApiError.ERROR_95151);
        }
        return Boolean.TRUE;
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
        checkTemplateName(templateName, "");
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
    private void checkTemplateName(String templateName, String id) {
        LambdaQueryWrapper<ProjectTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTemplateEntity::getName, templateName);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(ProjectTemplateEntity::getId, id);
        }
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95011);
        }
    }


}
