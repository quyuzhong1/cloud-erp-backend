package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
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
import com.erp.model.plm.vo.PreTaskListVO;
import com.erp.model.plm.vo.migrateTempVO;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.mapper.ProjectTemplateMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
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


    @Resource
    private SysTaskPhaseService sysTaskPhaseService;

    @Resource
    private ProjectTaskSysService projectTaskSysService;


    @Resource
    private TemplatePhaseService templatePhaseService;


    @Resource
    private TemplateTaskService templateTaskService;

    @Resource
    private TemplateTaskDocsNameService templateTaskDocsNameService;


    @Resource
    private TemplateDeliveryDocsService templateDeliveryDocsService;

    @Resource
    private SysDocsService sysDocsService;

    @Resource
    private TaskDeliveryService taskDeliveryService;

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
     * 根据产品属性id查询对应的模板信息
     *
     * @param propertyId
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO>
     * @author yl
     * @date 2023-03-06 18:18
     */
    @Override
    public List<Map<String, Object>> getByPropertyId(String propertyId) {
        if (StringUtils.isBlank(propertyId)) {
            return new ArrayList<>();
        }

        return baseMapper.getByPropertyId(propertyId);
    }


    /**
     * 模板改造  迁移阶段历史数据
     *
     * @param
     * @return boolean
     * @author yl
     * @date 2023-03-07 19:06
     */
    @Override
    public boolean migratePhaseDb() {
        //系统阶段
        List<SysTaskPhaseEntity> sysTaskPhaseList = sysTaskPhaseService.list();
        List<ProjectTaskSysEntity> sysTaskList = projectTaskSysService.list();

        List<String> templateIds = sysTaskList.stream().map(ProjectTaskSysEntity::getTemplateId).distinct().collect(Collectors.toList());
        List<TemplatePhaseEntity> dbTemplatePhaseList = templatePhaseService.getByTemplateIds(templateIds);

        List<TemplatePhaseEntity> saveTemplatePhaseList = new ArrayList<>(10);
        //模板分组
        Map<String, List<ProjectTaskSysEntity>> map = sysTaskList.stream().collect(Collectors.groupingBy(ProjectTaskSysEntity::getTemplateId));
        List<migrateTempVO> migrateTempList = new ArrayList<>(20);
        for (Map.Entry<String, List<ProjectTaskSysEntity>> entry : map.entrySet()) {
            //模板id
            String templateId = entry.getKey();
            List<ProjectTaskSysEntity> list = entry.getValue();
            //以阶段分组
            Map<String, List<ProjectTaskSysEntity>> phaseMap = list.stream().collect(Collectors.groupingBy(ProjectTaskSysEntity::getPhaseId));
            for (Map.Entry<String, List<ProjectTaskSysEntity>> phaseEntry : phaseMap.entrySet()) {
                String phaseId = phaseEntry.getKey();
                List<ProjectTaskSysEntity> phaseSysTaskList = phaseEntry.getValue();
                for (ProjectTaskSysEntity sysTask : phaseSysTaskList) {
                    SysTaskPhaseEntity sysTaskPhase = sysTaskPhaseList.stream().filter(p -> p.getId().equals(phaseId)).findFirst().orElse(null);
                    if (sysTaskPhase != null) {
                        //获取到阶段名
                        String name = sysTaskPhase.getName();
                        migrateTempVO vo = new migrateTempVO();
                        //根据阶段名查询数据库是否存在
                        TemplatePhaseEntity dbTemplatePhase = dbTemplatePhaseList.stream().filter(d -> d.getName().equals(name) &&
                                d.getTemplateId().equals(templateId)).findFirst().orElse(null);
                        if (dbTemplatePhase != null) {
                            vo.setNewCreateId(dbTemplatePhase.getId());
                        } else {
                            TemplatePhaseEntity phaseEntity = saveTemplatePhaseList.stream().filter(s -> s.getTemplateId().equals(templateId)
                                    && s.getName().equals(name)).findFirst().orElse(null);
                            if (Objects.isNull(phaseEntity)) {
                                TemplatePhaseEntity addEntity = new TemplatePhaseEntity();
                                String id = IdWorker.getIdStr();
                                addEntity.setTemplateId(templateId);
                                addEntity.setId(id);
                                addEntity.setName(sysTaskPhase.getName());
                                saveTemplatePhaseList.add(addEntity);
                                vo.setNewCreateId(id);
                            } else {
                                vo.setNewCreateId(phaseEntity.getId());
                            }

                        }
                        vo.setTaskId(sysTask.getId());
                        vo.setTemplateId(templateId);
                        migrateTempList.add(vo);
                    }
                }
            }

        }


        boolean result = true;

        if (CollectionUtils.isNotEmpty(saveTemplatePhaseList)) {
            result = templatePhaseService.saveBatch(saveTemplatePhaseList);
        }

        List<String> taskIdList = migrateTempList.stream().map(migrateTempVO::getTaskId).collect(Collectors.toList());
        List<TemplateTaskEntity> templateTaskList = templateTaskService.listByIds(taskIdList);
        for (TemplateTaskEntity item : templateTaskList) {
            migrateTempVO tempVO = migrateTempList.stream().filter(t -> t.getTaskId().equals(item.getId())).findFirst().orElse(null);
            if (tempVO != null) {
                item.setPhaseId(tempVO.getNewCreateId());
            }
        }
        templateTaskService.updateBatchById(templateTaskList);
        return result;
    }


    /**
     * 模板改造迁移 文档
     *
     * @param
     * @return boolean
     * @author yl
     * @date 2023-03-08 10:26
     */
    @Override
    public boolean migrateDocsDb() {

        List<ProjectTaskSysEntity> sysTaskList = projectTaskSysService.list();

        List<String> templateIds = sysTaskList.stream().map(ProjectTaskSysEntity::getTemplateId).distinct().collect(Collectors.toList());
        List<String> taskIds = sysTaskList.stream().map(ProjectTaskSysEntity::getId).distinct().collect(Collectors.toList());
        List<TemplateTaskDocsNameEntity> dbTemplateDocsNameList = templateTaskDocsNameService.getByTemplateIds(templateIds);


        List<TaskDeliveryDocsEntity> deliveryDocsList = taskDeliveryService.geByTaskIds(taskIds);

        List<TemplateTaskDocsNameEntity> addList = new ArrayList<>(10);

        List<migrateTempVO> migrateTempList = new ArrayList<>(20);
        boolean result = true;
        //模板分组
        Map<String, List<ProjectTaskSysEntity>> map = sysTaskList.stream().collect(Collectors.groupingBy(ProjectTaskSysEntity::getTemplateId));
        for (Map.Entry<String, List<ProjectTaskSysEntity>> entry : map.entrySet()) {
            //模板id
            String templateId = entry.getKey();
            List<ProjectTaskSysEntity> list = entry.getValue();
            for (ProjectTaskSysEntity item : list) {
                String taskId = item.getId();
                TaskDeliveryDocsEntity deliveryDocs = deliveryDocsList.stream().filter(d -> d.getTaskId().equals(taskId)).findFirst().orElse(null);
                if (deliveryDocs != null) {
                    String docsName = deliveryDocs.getDocsName();
                    migrateTempVO vo = new migrateTempVO();
                    //看数据库有没有
                    TemplateTaskDocsNameEntity templateDocsEntity = dbTemplateDocsNameList.stream().filter(db -> db.getTemplateId().equals(templateId) &&
                            db.getName().equals(docsName)).findFirst().orElse(null);
                    //当为空
                    if (Objects.isNull(templateDocsEntity)) {
                        TemplateTaskDocsNameEntity addTaskDocs = addList.stream().filter(add -> add.getName().equals(docsName) &&
                                add.getTemplateId().equals(templateId)).findFirst().orElse(null);
                        if (Objects.isNull(addTaskDocs)) {
                            TemplateTaskDocsNameEntity add = new TemplateTaskDocsNameEntity();
                            String id = IdWorker.getIdStr();
                            add.setId(id);
                            add.setName(docsName);
                            add.setTemplateId(templateId);
                            addList.add(add);
                            vo.setNewCreateId(id);
                        } else {
                            vo.setNewCreateId(addTaskDocs.getId());
                        }

                    } else {
                        //表示有
                        vo.setNewCreateId(templateDocsEntity.getId());
                    }
                    vo.setTemplateId(templateId);
                    vo.setTaskId(taskId);
                    vo.setName(docsName);
                    migrateTempList.add(vo);
                }
            }

        }
        if (CollectionUtils.isNotEmpty(addList)) {
            result = templateTaskDocsNameService.saveBatch(addList);
        }

        List<TemplateDeliveryDocsEntity> dbTemplateDeliveryList = templateDeliveryDocsService.getByTemplateIds(templateIds);
        for (TemplateDeliveryDocsEntity item : dbTemplateDeliveryList) {
            migrateTempVO tempVO = migrateTempList.stream().filter(t -> t.getTemplateId().equals(item.getTemplateId())&& item.getDocsName().equals(t.getName())).findFirst().orElse(null);
            if (tempVO != null) {
                item.setDocsNameId(tempVO.getNewCreateId());
            }
        }
        templateDeliveryDocsService.updateBatchById(dbTemplateDeliveryList);
        return result;
    }


    /**
     * 保存模板 返回模板id
     *
     * @param templateName
     * @param productPropertyId 产品属性id
     * @return java.lang.String
     * @author yl
     * @date 2022-09-20 14:30
     */
    @Override
    public String saveTemplate(String templateName, String productId, String productPropertyId) {
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
        boolean saveResult = this.save(entity);
        if (saveResult) {
            //模板id
            String templateId = entity.getId();
            templateRefPropertyService.saveRef(templateId, Arrays.asList(productPropertyId));
            return templateId;
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
