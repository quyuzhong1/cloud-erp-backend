package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.TemplateTaskExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.DistributionTypeEnum;
import com.erp.model.plm.enums.RelatedSkuTypeEnum;
import com.erp.model.plm.enums.TaskTypeEnum;
import com.erp.model.plm.vo.PreTaskVO;
import com.erp.model.plm.vo.TemplateTaskVO;
import com.erp.model.sys.dto.UserSuperiorDTO;
import com.erp.model.sys.enums.ChargeSuperiorEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.listener.TemplateTaskExcelListener;
import com.erp.server.plm.mapper.TemplateTaskMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname TemplateTaskServiceImpl

 * @Date 2022-09-20 15:35
 * @Created by yl
 */
@Service
public class TemplateTaskServiceImpl extends ServiceImpl<TemplateTaskMapper, TemplateTaskEntity> implements TemplateTaskService {

    @Autowired
    private ProjectTaskService taskService;

    @Autowired
    private TemplateDeliveryDocsService templateDeliveryDocsService;

    @Autowired
    private TemplatePreTaskService templatePreTaskService;

    @Autowired
    @Lazy
    private NoticeMessageService noticeMessageService;

    @Autowired
    private TemplatePhaseService templatePhaseService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private BusinessProcessService businessProcessService;

    @Autowired
    private TemplateTaskRefSkuConfigService templateTaskRefSkuConfigService;


    @Autowired
    private TemplateRoleService templateRoleService;

    @Autowired
    private TaskChargeDistributionService taskChargeDistributionService;

    @Autowired
    private TemplateMembersService templateMembersService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private ProjectTemplateService projectTemplateService;

    @Autowired
    private ProjectTemplateService templateService;

    @Autowired
    private TemplateRoleRefMembersService templateRoleRefMembersService;

    @Autowired
    private TemplateTaskDocsNameService templateTaskDocsNameService;

    @Autowired
    private TemplateTaskService templateTaskService;

    @Autowired
    private TemplateDocsPermissionService templateDocsPermissionService;

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private ProjectMembersService projectMembersService;

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private ProjectInfoService projectInfoService;

    @Autowired
    private TemplateTaskFollowerService templateTaskFollowerService;

    @Autowired
    private TaskFollowerService taskFollowerService;

    /**
     * 产品保存模板 保存任务
     *
     * @param templateId
     * @param productId
     * @param phaseSourceList 阶段的源数据
     * @return
     */
    @Override
    @Transactional
    public List<CopySourceDTO> saveTemplateTask(String templateId, String productId, List<CopySourceDTO> phaseSourceList) {
        List<ProjectTaskEntity> projectTaskList = taskService.getByProductId(productId);
        List<CopySourceDTO> copySourceList = new ArrayList<>(20);
        if (CollectionUtils.isNotEmpty(projectTaskList)) {
            List<TemplateTaskEntity> saveList = new ArrayList<>();
            for (ProjectTaskEntity item : projectTaskList) {
                CopySourceDTO source = new CopySourceDTO();
                TemplateTaskEntity entity = new TemplateTaskEntity();
                String newCreateId = IdWorker.getIdStr();
                source.setNewCreateId(newCreateId);
                source.setDataId(item.getId());
                BeanMapper.copy(item, entity);
                entity.setId(newCreateId);
                entity.setTemplateId(templateId);
                entity.setSourceTaskId(item.getId());
                //新的阶段id
                String newPhaseId = phaseSourceList.stream().filter(p -> p.getDataId().
                        equals(item.getPhaseId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNewCreateId())).orElse("");
                entity.setPhaseId(newPhaseId);
                copySourceList.add(source);
                if (ObjectUtils.isEmpty(item.getDistributionType())) {
                    entity.setDistributionType(DistributionTypeEnum.DISTRIBUTION_USER.getCode());
                }
                //如果分配方式为角色时角色为空则自动转为人员分配
                if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(item.getDistributionType()) && StringUtils.isBlank(item.getRoleName())) {
                    entity.setDistributionType(DistributionTypeEnum.DISTRIBUTION_USER.getCode());
                }
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
        return copySourceList;
    }


    /**
     * 根据模板 获取项目任务
     *
     * @param flagTemplateId
     * @return java.util.List<com.erp.model.plm.entity.TemplateTaskEntity>
     * @author yl
     * @date 2022-09-21 9:57
     */
    @Override
    public List<TemplateTaskEntity> getTaskByTemplateId(String flagTemplateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, flagTemplateId);
        queryWrapper.isNull(TemplateTaskEntity::getSourceTaskId);
        return this.list(queryWrapper);
    }

    /**
     * @param templateId
     * @return List<TemplateTaskEntity>
     * @description: 获取模板下面所有的任务
     * @author Will
     * @date: 2022/11/14 14:35
     */
    @Override
    public List<TemplateTaskEntity> getAllTaskByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        return this.list(queryWrapper);
    }

    /**
     * @param id
     * @param templateId
     * @return Boolean
     * @description: 删除模板任务
     * @author Will
     * @date: 2022/11/14 16:16
     */
    @Override
    @Transactional
    public Boolean removeTask(String id, String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getId, id);
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        List<TemplateTaskEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_95058);
        }
        TemplateTaskEntity entity = list.stream().findFirst().orElse(null);
        Integer IsFixed = entity.getIsFixed();
        //判断是否是子任务
        checkTaskIfExistPid(id, templateId);
        //删除任务交付文档数据
        templateDeliveryDocsService.removeByTaskIdAndTemplateId(id, templateId);
        //删除任务审核人
        taskChargeDistributionService.removeBySourceAndTaskId(MathUtil.TWO, id);
        //批量删除前置任务
        templatePreTaskService.removeByTaskIds(Arrays.asList(id));
        //删除关注人
        templateTaskFollowerService.deleteByTemplateIdAndTaskIds(templateId, Arrays.asList(id));
        //删除模板任务
        return this.remove(queryWrapper);
    }

    @Override
    public Boolean removeTaskBatch(List<String> ids, String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TemplateTaskEntity::getId, ids);
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        List<TemplateTaskEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_95058);
        }
        //删除任务交付文档数据
        templateDeliveryDocsService.removeByTaskIdsAndTemplateId(ids, templateId);
        //删除任务审核人
        taskChargeDistributionService.removeBySourceAndTaskIds(MathUtil.TWO, ids);
        //删除关注人
        templateTaskFollowerService.deleteByTemplateIdAndTaskIds(templateId, ids);
        //批量删除前置任务
        templatePreTaskService.removeByTaskIds(ids);
        //删除模板任务
        return lambdaUpdate()
                .in(TemplateTaskEntity::getId, ids)
                .eq(TemplateTaskEntity::getTemplateId, templateId)
                .set(TemplateTaskEntity::getIsDeleted, Boolean.TRUE)
                .update();
    }

    @Override
    public void importTemplateTaskFile(MultipartFile excelFile, String templateId, HttpServletResponse response) {
        TemplateTaskExcelListener excelListenerUtil = new TemplateTaskExcelListener(templateId, sysUserFeign, templatePhaseService, templateTaskService, templateTaskDocsNameService);
        try {
            EasyExcel.read(excelFile.getInputStream(), TemplateTaskExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95124);
        }
        List<TemplateTaskExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<TemplateTaskExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<TemplateTaskExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理导入数据
        doOpHandleImport(templateId,successList, errorList);

        List<TemplateTaskExcelDTO> list = excelListenerUtil.getErrorList();
        if (list.size() > 0) {
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/templateTaskError.xlsx";
            String name = "templateTaskError";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }
        }
    }

    /**
     * 导入数据处理
     * @author will
     * @date 2024/7/4 15:39
     * @param successList
     * @param errorList
     */
    private void doOpHandleImport (String templateId,List<TemplateTaskExcelDTO> successList,List<TemplateTaskExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

        //查询模板任务
        List<TemplateTaskEntity> templateTaskEntityList = templateTaskService.listByTemplateId(templateId);

        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();

        //阶段
        List<String> phaseNameList = successList.stream().map(TemplateTaskExcelDTO::getPhaseName).distinct().collect(Collectors.toList());
        List<TemplatePhaseEntity> templatePhaseList = templatePhaseService.listProductPhaseByNameList(templateId, phaseNameList);

        //交付物文档
        List<DocsDTO> docsList = templateTaskDocsNameService.getDocsNameList(templateId);

        for (TemplateTaskExcelDTO templateTaskExcelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            TemplateTaskDTO templateTaskDTO = new TemplateTaskDTO();

            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(templateTaskExcelDTO.getType())) {
                if (!templateTaskExcelDTO.getType().equals("一般任务") && !templateTaskExcelDTO.getType().equals("评审任务")) {
                    errorMsgList.add("[任务类型]请输入'一般任务'或'评审任务'");
                }

                if (templateTaskExcelDTO.getType().equals("一般任务")){
                    templateTaskDTO.setType(0);
                } else {
                    templateTaskDTO.setType(1);
                }
            }
            List<TaskChargeDistributionDTO> TaskChargeDistributionlist = new ArrayList<>();
            //模板任务
            TemplateTaskEntity templateTaskEntity  = templateTaskEntityList.stream().filter(obj -> StrUtil.equals(obj.getName(),templateTaskExcelDTO.getName()))
                    .findFirst().orElse(null);

            if (ObjectUtil.isNotEmpty(templateTaskEntity)) {
                templateTaskDTO.setId(templateTaskEntity.getId());
            }

            List<String> chargeNameList = new ArrayList<>();
            String chargeName = templateTaskExcelDTO.getChargeName();
            String[] chargeNames = chargeName.split(",");
            for (String name : chargeNames) {
                FindUserDTO findUserDTO = userList.stream().filter(u -> name.equals(u.getUserName())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(findUserDTO)) {
                    errorMsgList.add("[任务负责人]在系统中未找到，多个负责人请用英文逗号','隔开");
                } else {
                    chargeNameList.add(findUserDTO.getUserId());
                }
            }

            TemplatePhaseEntity templatePhaseEntity = templatePhaseList.stream().filter(obj -> StrUtil.equals(obj.getName(), templateTaskExcelDTO.getPhaseName())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(templatePhaseEntity)) {
                errorMsgList.add("[阶段名称]在这个[所属产品]下不存在");
            }

            String preTask = templateTaskExcelDTO.getPreTask();
            List<String> preTaskList = new ArrayList<>();
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(preTask)) {
                String[] split = preTask.split(",");
                for (String task : split) {
                    TemplateTaskEntity taskEntity = templateTaskEntityList.stream().filter(t -> t.getName().equals(task)).findFirst().orElse(null);
                    if (ObjectUtil.isEmpty(taskEntity)) {
                        errorMsgList.add("[前置任务]在这个[所属产品]下不存在，多个前置任务请用英文逗号','隔开");
                    } else {
                        preTaskList.add(taskEntity.getId());
                    }
                }
            }

            String priority = templateTaskExcelDTO.getPriority();
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(priority)) {
                if (!priority.equals("高") && !priority.equals("中") && !priority.equals("低")) {
                    errorMsgList.add("[任务优先级]请输入'高'或'中''低'");
                }
            }
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(templateTaskExcelDTO.getIsFixed())) {
                if (!templateTaskExcelDTO.getIsFixed().equals("是") && !templateTaskExcelDTO.getIsFixed().equals("否")) {
                    errorMsgList.add("[是否是固定任务]请输入'是'或'否'");
                }
            }
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(templateTaskExcelDTO.getRefSku())) {
                if (!templateTaskExcelDTO.getRefSku().equals("关联") && !templateTaskExcelDTO.getRefSku().equals("不关联")) {
                    errorMsgList.add("[SKU关联]请输入'关联'或'不关联'");
                }
            }

            //目标交付文档
            String docsName = templateTaskExcelDTO.getDocsName();
            List<DocsDTO> docsNameList = new ArrayList<>();
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(docsName)) {
                String[] split = docsName.split(",");
                for (String docs : split) {
                    DocsDTO docsDTO = docsList.stream().filter(t -> t.getName().equals(docs)).findFirst().orElse(null);
                    if (ObjectUtil.isEmpty(docsDTO)) {
                        TmeplateDocsNameDTO docsNameDTO = new TmeplateDocsNameDTO();
                        docsNameDTO.setName(docs);
                        docsNameDTO.setTemplateId(templateId);
                        String id = templateTaskDocsNameService.saveDocs(docsNameDTO);
                        DocsDTO dto = new DocsDTO();
                        dto.setId(id);
                        dto.setName(docs);
                        dto.setState(false);
                        docsNameList.add(dto);
                    } else {
                        docsNameList.add(docsDTO);
                    }
                }
            }

            //存在错误数据则直接返回
            if (errorMsgList.size() > 0) {
                templateTaskExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(templateTaskExcelDTO);
                return;
            }
            templateTaskDTO.setTemplateId(templateId);
            templateTaskDTO.setName(templateTaskExcelDTO.getName());

            templateTaskDTO.setChargeIds(chargeNameList);
            templateTaskDTO.setPreTaskIdList(preTaskList);

            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(templateTaskExcelDTO.getPlanStartTime())) {
                templateTaskDTO.setPlanStartTime(LocalDate.parse(templateTaskExcelDTO.getPlanStartTime(), dateTimeFormatter));
            }

            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(templateTaskExcelDTO.getPlanEndTime())) {
                templateTaskDTO.setPlanEndTime(LocalDate.parse(templateTaskExcelDTO.getPlanEndTime(), dateTimeFormatter));
            }

            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(templateTaskExcelDTO.getPriority())) {
                if (templateTaskExcelDTO.getPriority().equals("高")) {
                    templateTaskDTO.setPriority(3);
                } else if (templateTaskExcelDTO.getPriority().equals("中")) {
                    templateTaskDTO.setPriority(2);
                } else {
                    templateTaskDTO.setPriority(1);
                }
            }
            templateTaskDTO.setPhaseId(templatePhaseEntity.getId());
            templateTaskDTO.setPhaseName(templatePhaseEntity.getName());
            templateTaskDTO.setDescription(templateTaskExcelDTO.getDescription());
            templateTaskDTO.setApprovalList(TaskChargeDistributionlist);
            templateTaskDTO.setDeliveryDocsList(docsNameList);
            templateTaskDTO.setDistributionType(1);
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(templateTaskExcelDTO.getRefSku())) {
                if (templateTaskExcelDTO.getRefSku().equals("关联")) {
                    templateTaskDTO.setRelatedSkuType(RelatedSkuTypeEnum.ALL_RELATED.getCode());
                } else {
                    templateTaskDTO.setRelatedSkuType(RelatedSkuTypeEnum.NOT_RELATED.getCode());
                }
            } else {
                if (ObjectUtil.isNotEmpty(templateTaskEntity)) {
                    templateTaskDTO.setRelatedSkuType(templateTaskEntity.getRelatedSkuType());
                } else {
                    templateTaskDTO.setRelatedSkuType(RelatedSkuTypeEnum.NOT_RELATED.getCode());
                }
            }
            templateTaskDTO.setWorkPeriod(templateTaskExcelDTO.getWorkPeriod());
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(templateTaskExcelDTO.getIsFixed())) {
                if (templateTaskExcelDTO.getIsFixed().equals("是")) {
                    templateTaskDTO.setIsFixed(1);
                } else {
                    templateTaskDTO.setIsFixed(0);
                }
            }
            templateTaskService.saveOrUpdate(templateTaskDTO);
        }
    }

    /**
     * @param templateId
     * @description: 根据模板id删除
     * @author Will
     * @date: 2022/11/14 16:54
     */
    @Override
    public void removeByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        this.remove(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getTaskListByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(TemplateTaskEntity::getId, TemplateTaskEntity::getName);
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        return this.listMaps(queryWrapper);
    }

    @Override
    public TemplateTaskVO taskDetails(TemplateTaskParamDTO dto) {

        TemplateTaskEntity taskEntity = this.getByIdAndTemplateId(dto.getId(), dto.getTemplateId());
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        TemplateTaskVO resultVO = new TemplateTaskVO();
        BeanMapper.copy(taskEntity, resultVO);
        //判断任务分配类型
        if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(taskEntity.getDistributionType())) {
            String roleId = taskEntity.getRoleId();
            //当角色id 为空的时候
            if (StringUtils.isBlank(roleId)) {
                List<TemplateRoleEntity> templateRoleList = templateRoleService.getAllRoles(dto.getTemplateId());
                roleId = templateRoleList.stream().filter(t -> t.getName().equals(taskEntity.getRoleName())).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            }
            if (StringUtils.isNotBlank(roleId)) {
                resultVO.setRoleIds(Arrays.asList(roleId.split(",")));
            }
        } else if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(taskEntity.getDistributionType())) {
            String chargeId = taskEntity.getChargeId();
            if (StringUtils.isNotBlank(chargeId)) {
                resultVO.setChargeIds(Arrays.asList(chargeId.split(",")));
            }
        }
        //查询模板任务下审核人
        List<TaskChargeDistributionEntity> taskChargeDistributionList = taskChargeDistributionService.listBySourceAndTaskId(MathUtil.TWO, dto.getId());
        if (CollectionUtils.isNotEmpty(taskChargeDistributionList)) {
            List<TaskChargeDistributionDTO> list = BeanMapperUtils.copyList(TaskChargeDistributionDTO.class, taskChargeDistributionList);
            list.forEach(obj -> {
                List<String> collect = Arrays.stream(obj.getCharges().split(",")).collect(Collectors.toList());
                obj.setChargeList(collect);
                //回显名称
                if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(obj.getDistributionType())) {
                    //用户分配查询名称
                    List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(collect);
                    if (CollectionUtils.isNotEmpty(userList)) {
                        List<String> usrNameList = userList.stream().map(FindUserDTO::getUserName).collect(Collectors.toList());
                        obj.setChargeNames(String.join(",", usrNameList));
                    }
                }
                if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(obj.getDistributionType())) {
                    //角色分配直接取名称
                    obj.setChargeNames(obj.getCharges());
                }
                if (DistributionTypeEnum.DISTRIBUTION_SUPERIOR.getCode().equals(obj.getDistributionType())) {
                    //上级分配取枚举
                    List<String> superiors = collect.stream().map(e -> ChargeSuperiorEnum.getDesc(e)).collect(Collectors.toList());
                    obj.setChargeNames(String.join(",", superiors));
                }

            });
            resultVO.setApprovalList(list);
        }
        resultVO.setDeliveryDocsList(templateDeliveryDocsService.getDocsByTaskIdAndTemplateId(dto.getId(), dto.getTemplateId()));
        String businessProcessId = resultVO.getBusinessProcessId();
        if (StringUtils.isNotBlank(businessProcessId)) {
            BusinessProcessEntity processEntity = businessProcessService.getById(businessProcessId);
            if (processEntity != null) {
                resultVO.setBusinessName(processEntity.getBusinessName());
            }
        }
        List<PreTaskVO> preTaskList = templatePreTaskService.getTemplatePreTaskIdList(dto.getId(), dto.getTemplateId());
        List<String> pretaskIdList = Collections.emptyList();
        if (CollectionUtil.isNotEmpty(preTaskList)) {
            pretaskIdList = preTaskList.stream().map(PreTaskVO::getPreTaskId).collect(Collectors.toList());
        }
        resultVO.setPreTaskIdList(pretaskIdList);
        TemplateTaskRefSkuConfigEntity skuConfigEntity = templateTaskRefSkuConfigService.getByTaskId(taskEntity.getId());
        if (skuConfigEntity != null) {
            resultVO.setFieldJson(skuConfigEntity.getFieldJson());
            resultVO.setFieldConfigType(skuConfigEntity.getFieldConfigType());
        }
        List<TemplateTaskFollowerEntity> concernEntityList = templateTaskFollowerService.listTemplateFollower(dto.getTemplateId(), Arrays.asList(dto.getId()));
        List<String> concernUserIdList = concernEntityList.stream().map(TemplateTaskFollowerEntity::getUserId).collect(Collectors.toList());
        resultVO.setConcernUserIdList(concernUserIdList);
        return resultVO;
    }

    @Override
    public List<TemplateTaskEntity> listByRoleId(String roleId) {
        return this.baseMapper.listByRoleId(roleId);
    }

    /**
     * 复制模板任务
     *
     * @param templateId
     * @param productId
     * @param projectId
     * @return void
     * @author yl
     * @date 2022-10-28 14:22
     */
    @Override
    public List<CopySourceDTO> copyTemplateTask(String templateId, String productId, String projectId, List<CopySourceDTO> phaseSourceList, List<String> taskIdList) {
        // 查询模板
        ProjectTemplateEntity projectTemplateEntity = projectTemplateService.getById(templateId);
        if (ObjectUtils.isEmpty(projectTemplateEntity) || !MathUtil.ONE.equals(projectTemplateEntity.getStatus())) {
            return new ArrayList<>();
        }
        List<TemplateTaskEntity> list = this.getByTemplateId(templateId, taskIdList);
        List<String> templateTaskIds = list.stream().map(TemplateTaskEntity::getId).collect(Collectors.toList());
        List<TemplateTaskFollowerEntity> templateTaskFollowerList = templateTaskFollowerService.listTemplateFollower(templateId, templateTaskIds);
        List<ProjectTaskEntity> byProductId = projectTaskService.getByProductId(productId);

        LoginUser loginUser = UserContext.getDefaultLoginUser();
        //来源信息
        List<CopySourceDTO> sourceList = new ArrayList<>();
        List<ProjectTaskEntity> copyList = new ArrayList<>(list.size());
        if (CollectionUtils.isNotEmpty(list)) {
            for (TemplateTaskEntity item : list) {
                ProjectTaskEntity projectTaskEntity = byProductId.stream().filter(projectMembers -> projectMembers.getName().equals(item.getName())).findFirst().orElse(null);
                if (!Objects.isNull(projectTaskEntity)) {
                    throw new ServiceException(ApiError.ERROR_95013);
                }

                CopySourceDTO source = new CopySourceDTO();
                String taskId = IdWorker.getIdStr();
                ProjectTaskEntity taskEntity = new ProjectTaskEntity();
                BeanMapper.copy(item, taskEntity);
                taskEntity.setProductId(productId);
                taskEntity.setProjectId(projectId);
                taskEntity.setId(taskId);
                String chargeId = item.getChargeId();
                List<String> chargeIdList = new ArrayList<>();
                if (StringUtils.isNotBlank(chargeId)) {
                    chargeIdList = Arrays.asList(chargeId.split(","));
                }

                source.setNewCreateId(taskId);
                source.setDataId(item.getId());
                CopySourceDTO phase = phaseSourceList.stream().filter(p -> p.getDataId()
                        .equals(item.getPhaseId())).findFirst().orElse(null);
                if (phase != null) {
                    taskEntity.setPhaseId(phase.getNewCreateId());
                } else {
                    taskEntity.setPhaseId("");
                }
                if (ObjectUtils.isNotEmpty(projectTemplateEntity)) {
                    //判断负责人分配方式是否是角色
                    if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(taskEntity.getDistributionType())) {
                        List<String> roleIds = Arrays.stream(item.getRoleName().split(",")).collect(Collectors.toList());
                        List<TemplateMembersEntity> templateMembersList = templateMembersService.listByRoleNames(roleIds, projectTemplateEntity.getId());
                        if (CollectionUtils.isNotEmpty(templateMembersList)) {
                            List<String> memberIds = templateMembersList.stream().map(TemplateMembersEntity::getMemberId).distinct().collect(Collectors.toList());
                            List<String> memberNames = templateMembersList.stream().map(TemplateMembersEntity::getMemberName).distinct().collect(Collectors.toList());
                            taskEntity.setChargeId(StringUtils.join(memberIds, ","));
                            taskEntity.setChargeName(StringUtils.join(memberNames, ","));
                        }
                    }
                }
                copyList.add(taskEntity);
                sourceList.add(source);
                List<String> chargeIds = new ArrayList<>();
                if (StringUtils.isNotBlank(taskEntity.getChargeId())) {
                    chargeIds = Arrays.stream(taskEntity.getChargeId().split(",")).collect(Collectors.toList());
                }
                //查询模板任务下审核人
                List<TaskChargeDistributionEntity> taskChargeDistributionList = taskChargeDistributionService.listBySourceAndTaskId(MathUtil.TWO, item.getId());

                setTaskChargeDistribution(taskChargeDistributionList, chargeIds, projectTemplateEntity.getId(), taskEntity.getId(), MathUtil.THREE);

                //新增关注人
                List<TemplateTaskFollowerEntity> followerEntityList = templateTaskFollowerList.stream().filter(req -> req.getTemplateTaskId().equals(item.getId())).collect(Collectors.toList());
                List<String> followerUserIdList = followerEntityList.stream().map(TemplateTaskFollowerEntity::getUserId).collect(Collectors.toList());
                taskFollowerService.batchAdd(taskId, productId, followerUserIdList);
            }
        }

        //更改父id
        for (ProjectTaskEntity task : copyList) {
            //这个pid 还是 模板数据的pid
            String pid = task.getPid();
            if (!pid.equals("0")) {
                CopySourceDTO source = sourceList.stream().
                        filter(s -> s.getDataId().equals(pid)).findFirst().orElse(null);
                if (source != null) {
                    task.setPid(source.getNewCreateId());
                } else {
                    task.setPid("0");
                }
            }
        }

        Boolean flag = taskService.saveBatch(copyList);
        if (flag) {
            //发送新建任务通知
            noticeMessageService.newTaskNotice(loginUser.getUserName(), copyList, productId);

        }
        return sourceList;

    }

    @Override
    public PagingVO<TemplateTaskShowDTO> paging(PagingDTO<TemplateSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        TemplateSearchDTO params = dto.getParams();
        IPage<TemplateTaskShowDTO> paging = baseMapper.paging(query, params);
        return new PagingVO(paging);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdate(TemplateTaskDTO dto) {
        //验证任务名称是否已存在
        checkTemplateTaskName(dto);
        TemplateTaskEntity entity = new TemplateTaskEntity();
        BeanMapperUtils.copy(dto, entity);
        LoginUser loginUser = UserContext.getLoginUser();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }

        String notRelated = RelatedSkuTypeEnum.NOT_RELATED.getCode();

        boolean isNotRelated = notRelated.equalsIgnoreCase(dto.getRelatedSkuType());
        //配置表单属性
        String fieldConfigType = dto.getFieldConfigType();
        //生成sku
        String createSku = TaskConstant.CREATE_SKU;
        //填写sku
        String fillProductInfo = TaskConstant.FILL_PRODUCT_INFO;
        //配置表单属性
        String fieldJson = dto.getFieldJson();
        //第一种 sku不等于空并且大于0  并且  表单属性不为空且为填写
        Boolean needCheckFirst = (StringUtils.isNotBlank(fieldConfigType) && fillProductInfo.equals(fieldConfigType) && StringUtils.isNotBlank(fieldJson));

        //第二种 sku 没有  并且 表单属性不为空 且为生成
        Boolean needCheckSecond = (StringUtils.isNotBlank(fieldConfigType) && (createSku.equals(fieldConfigType) || (StringUtils.isNotBlank(dto.getFieldJson()) && RelatedSkuTypeEnum.ALL_RELATED.getCode().equals(dto.getRelatedSkuType()))));
        Integer type = dto.getType();
        Integer generalTask = TaskTypeEnum.GENERAL_TASK.getCode();
        List<TaskChargeDistributionDTO> approvalList = dto.getApprovalList();
        //TODO 2023-03-30 暂时取消审核流程
        //如果配置表单 一般任务 一定要走流程,自定义审核人，存在多级审核及会签，暂时用两层list接收，之后公共审核模块可添加审核人表储存
/*        if (needCheckFirst || needCheckSecond) {
            if (generalTask.equals(type)) {
                if (CollectionUtils.isEmpty(approvalList)) {
                    throw new ServiceException(ApiError.ERROR_95078);
                }
            }
        }*/
        String uid = loginUser.getUid();
        String userName = loginUser.getUserName();
        if (StringUtils.isBlank(dto.getId())) {
            entity.setCreateUserId(uid);
            entity.setCreateUserName(userName);
        } else {
            entity.setUpdateUserId(uid);
            entity.setUpdateUserName(userName);
        }
        List<String> chargeIds = dto.getChargeIds();
        List<String> roleIds = dto.getRoleIds();
        //任务分配类型处理
        if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(dto.getDistributionType())) {
            //分配类型为角色
            List<TemplateRoleEntity> templateRoleList = templateRoleService.listByIds(roleIds);
            entity.setRoleId(String.join(",", roleIds));
            if (CollectionUtils.isNotEmpty(templateRoleList)) {
                List<String> roleNames = templateRoleList.stream().map(TemplateRoleEntity::getName).collect(Collectors.toList());
                entity.setRoleName(String.join(",", roleNames));
                entity.setChargeId("");
                entity.setChargeName("");
            }
        } else if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(dto.getDistributionType())) {
            //分配类型为负责人
            String chargeNames = commonService.getNameByIds(chargeIds);
            entity.setChargeId(String.join(",", chargeIds));
            entity.setChargeName(chargeNames);
            entity.setRoleName("");
            entity.setRoleId("");
        }
        //阶段名称
        if (StringUtils.isNotBlank(dto.getPhaseId())) {
            TemplatePhaseEntity phaseEntity = templatePhaseService.getByIdAndTemplateId(dto.getPhaseId(), dto.getTemplateId());
            if (ObjectUtils.isEmpty(phaseEntity)) {
                throw new ServiceException(ApiError.ERROR_95041);
            }
            entity.setPhaseName(phaseEntity.getName());

            if (TaskConstant.APPROVAL_TASK_PHASE.equals(phaseEntity.getName())) {
                entity.setProperty(TaskConstant.APPROVAL_TASK);
            } else {
                entity.setProperty(TaskConstant.PROJECT_TASK);
            }
        }
        //交付文档
        List<DocsDTO> deliveryDocsList = dto.getDeliveryDocsList();
        //因为模板任务无主键，则无法用saveOrUpdate进行操作
        if (null != dto.getWorkPeriod() && 0 < dto.getWorkPeriod()) {
            entity.setWorkPeriod(dto.getWorkPeriod());
        }
        if (StringUtils.isBlank(entity.getId())) {
            this.save(entity);
        } else {
            this.updateByIdAndTemplateId(entity);
        }
        //保存审核人信息
        if (CollectionUtils.isNotEmpty(approvalList)) {
            approvalList.forEach(obj -> obj.setCharges(String.join(",", obj.getChargeList())));
            List<TaskChargeDistributionEntity> taskChargeDistributionList = BeanMapperUtils.copyList(TaskChargeDistributionEntity.class, approvalList);
            setTaskChargeDistribution(taskChargeDistributionList, dto.getChargeIds(), entity.getTemplateId(), entity.getId(), MathUtil.TWO);
        } else {
            //删除交付文档的审核人
            taskChargeDistributionService.removeBySourceAndTaskId(MathUtil.TWO,entity.getId());
        }

        //保存交付文档
        templateDeliveryDocsService.saveTemplateDeliveryDocsList(entity.getId(), dto.getTemplateId(), deliveryDocsList);
        //保存SKU配置 字段 关系表 当不关联的时候删除
        if (!isNotRelated) {
            //保存模板配置信息
            templateTaskRefSkuConfigService.addTemplateTaskRefSkuConfig(entity.getId(), dto.getTemplateId(), dto.getFieldConfigType(), dto.getFieldJson());
        } else {
            templateTaskRefSkuConfigService.removeByTaskId(entity.getId());
        }

        //添加关注人
        templateTaskFollowerService.saveTemplateFollowerList(dto.getTemplateId(), entity.getId(), dto.getConcernUserIdList());

        //保存前置任务
        templatePreTaskService.saveTemplatePreTaskList(entity.getId(), dto.getPreTaskIdList(), dto.getTemplateId());
        return true;
    }


    /**
     * 获取项目任务
     *
     * @param templateId
     * @return java.util.List<com.erp.model.plm.entity.TemplateTaskEntity>
     * @author yl
     * @date 2022-10-29 16:29
     */
    public List<TemplateTaskEntity> getByTemplateId(String templateId, List<String> taskIdList) {
        List<TemplateTaskEntity> byTemplateId = baseMapper.getByTemplateId(templateId, taskIdList);
 /*       LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        queryWrapper.orderByAsc(TemplateTaskEntity::getCreateTime);*/
        return byTemplateId;
    }

    /**
     * @param id
     * @param templateId
     * @return TemplateTaskEntity
     * @description: 根据id和模板id查询
     * @author Will
     * @date: 2022/11/18 11:42
     */
    public TemplateTaskEntity getByIdAndTemplateId(String id, String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        queryWrapper.eq(TemplateTaskEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    /**
     * @param dto
     * @description: 验证模板任务名称是否已存在
     * @author Will
     * @date: 2022/11/14 14:05
     */
    private void checkTemplateTaskName(TemplateTaskDTO dto) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getName, dto.getName());
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, dto.getTemplateId());
        TemplateTaskEntity entity = this.getOne(queryWrapper);
        if (Objects.nonNull(entity) && !entity.getId().equals(dto.getId())) {
            throw new ServiceException(ApiError.ERROR_95057);
        }
    }

    //检查子任务
    private void checkTaskIfExistPid(String taskId, String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getPid, taskId);
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        Integer count = baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95024);
        }
    }

    /**
     * @param entity
     * @description: 任务编辑时需要更新字段
     * @author Will
     * @date: 2022/11/18 10:38
     */
    private void updateByIdAndTemplateId(TemplateTaskEntity entity) {
        LambdaUpdateWrapper<TemplateTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TemplateTaskEntity::getId, entity.getId());
        updateWrapper.eq(TemplateTaskEntity::getTemplateId, entity.getTemplateId());
        updateWrapper.set(TemplateTaskEntity::getPhaseId, entity.getPhaseId());
        updateWrapper.set(TemplateTaskEntity::getPhaseName, entity.getPhaseName());
        updateWrapper.set(TemplateTaskEntity::getUpdateUserId, entity.getUpdateUserId());
        updateWrapper.set(TemplateTaskEntity::getUpdateUserName, entity.getUpdateUserName());
        updateWrapper.set(TemplateTaskEntity::getName, entity.getName());
        updateWrapper.set(TemplateTaskEntity::getBusinessProcessId, entity.getBusinessProcessId());
        updateWrapper.set(TemplateTaskEntity::getChargeId, entity.getChargeId());
        updateWrapper.set(TemplateTaskEntity::getChargeName, entity.getChargeName());
        updateWrapper.set(TemplateTaskEntity::getDescription, entity.getDescription());
        updateWrapper.set(TemplateTaskEntity::getIsFixed, entity.getIsFixed());
        updateWrapper.set(TemplateTaskEntity::getPlanEndTime, entity.getPlanEndTime());
        updateWrapper.set(TemplateTaskEntity::getPlanStartTime, entity.getPlanStartTime());
        updateWrapper.set(TemplateTaskEntity::getPriority, entity.getPriority());
        updateWrapper.set(TemplateTaskEntity::getType, entity.getType());
        updateWrapper.set(TemplateTaskEntity::getRoleId, entity.getRoleId());
        updateWrapper.set(TemplateTaskEntity::getRoleName, entity.getRoleName());
        updateWrapper.set(TemplateTaskEntity::getDistributionType, entity.getDistributionType());
        updateWrapper.set(TemplateTaskEntity::getRelatedSkuType, entity.getRelatedSkuType());
        updateWrapper.set(TemplateTaskEntity::getWorkPeriod, entity.getWorkPeriod());
        this.update(updateWrapper);
    }

    /**
     * 添加任务审核人信息
     * ids 任务负责人
     *
     */
    @Override
    public void setTaskChargeDistribution(List<TaskChargeDistributionEntity> taskChargeDistributionList, List<String> ids, String templateId, String taskId, Integer source) {

        if (CollectionUtils.isNotEmpty(taskChargeDistributionList)) {
            //根据分配类型查询模板中的数据
            for (TaskChargeDistributionEntity taskChargeDistributionEntity : taskChargeDistributionList) {
                if (StringUtils.isBlank(taskChargeDistributionEntity.getCharges())) {
                    throw new ServiceException(ApiError.ERROR_95097);
                }
                //如果按人员分配的话
                if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(taskChargeDistributionEntity.getDistributionType())) {
                    taskChargeDistributionEntity.setChargeIds(taskChargeDistributionEntity.getCharges());
                }
                //按角色分配
                if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(taskChargeDistributionEntity.getDistributionType())) {
                    List<String> roleNames = Arrays.stream(taskChargeDistributionEntity.getCharges().split(",")).collect(Collectors.toList());
                    //查询对应模板角色下的人员
                    List<TemplateMembersEntity> templateMembersList = templateMembersService.listByRoleNames(roleNames, templateId);
                    if (CollectionUtils.isNotEmpty(templateMembersList)) {
                        String approverIds = templateMembersList.stream().map(TemplateMembersEntity::getMemberId).distinct().collect(Collectors.joining(","));
                        taskChargeDistributionEntity.setChargeIds(approverIds);
                    }
                }
                //按上级
                if (DistributionTypeEnum.DISTRIBUTION_SUPERIOR.getCode().equals(taskChargeDistributionEntity.getDistributionType()) && CollectionUtils.isNotEmpty(ids)) {
                    //查询对应负责人的上级
                    List<UserSuperiorDTO> userSuperiorDTOS = sysUserFeign.listSuperiorByUserIds(ids);
                    if (CollectionUtils.isEmpty(userSuperiorDTOS)) {
                        continue;
                    }
                    List<String> superiorTypeList = Arrays.stream(taskChargeDistributionEntity.getCharges().split(",")).collect(Collectors.toList());
                    for (String superiorType : superiorTypeList) {
                        String userIds = userSuperiorDTOS.stream().filter(obj -> obj.getSuperiorType().equals(superiorType)).map(UserSuperiorDTO::getUserId).collect(Collectors.joining(","));
                        if (StringUtils.isNotBlank(userIds)) {
                            taskChargeDistributionEntity.setChargeIds(userIds);
                        }
                    }
                }
            }
        }
        //保存交付文档的审核人
        taskChargeDistributionService.removeAndSave(taskId, taskChargeDistributionList, source);
    }


    /**
     * 更改阶段名称
     *
     * @param updateTaskList
     * @return void
     * @author yl
     * @date 2023-03-09 16:54
     */
    @Override
    public void updatePhase(List<TemplateTaskEntity> updateTaskList) {
        for (TemplateTaskEntity item : updateTaskList) {
            LambdaUpdateWrapper<TemplateTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(TemplateTaskEntity::getTemplateId, item.getTemplateId());
            updateWrapper.eq(TemplateTaskEntity::getPhaseId, item.getPhaseId());
            updateWrapper.set(TemplateTaskEntity::getPhaseName, item.getPhaseName());
            this.update(updateWrapper);
        }

    }

    /**
     * 查询模板里的任务列表
     * @Author Luo_WG
     * @Date 2023/3/20 11:06
     * @param dto dto
     * @return com.common.business.vo.PagingVO<com.erp.model.plm.dto.TemplateTaskShowDTO>
     **/
    @Override
    public PagingVO<TemplateTaskShowDTO> templateTaskList(PagingDTO<TemplateTaskSearchDTO> dto) {
        String docsNameStr = null;
                Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        TemplateTaskSearchDTO params = dto.getParams();
        if (StringUtils.isBlank(params.getTemplateId())) {
            throw new ServiceException(ApiError.ERROR_95157);
        }
        String chargeNameStr = StringUtils.join(params.getChargeName(), ",");;
        List<String> docsName = params.getDocsName();
        if (CollectionUtils.isEmpty(docsName)) {
            docsName = new ArrayList<>();
            docsName.add(params.getTemplateId());
        }
        List<TemplateTaskDocsNameEntity> docsNamesById = templateTaskDocsNameService.getDocsNamesById(docsName);
        if (CollectionUtils.isNotEmpty(docsNamesById)) {
            List<String> collect = docsNamesById.stream().map(TemplateTaskDocsNameEntity::getName).collect(Collectors.toList());
            docsNameStr = StringUtils.join(collect, ",");

        }
        IPage<TemplateTaskShowDTO> paging = baseMapper.templateTaskList(query, params, chargeNameStr, docsNameStr);
        return new PagingVO(paging);
    }

    /**
     * 模板引入任务
     * @Author Luo_WG
     * @Date 2023/3/20 14:15
     * @param dto dto
     * @return void
     **/
    @Override
    public Boolean templateCiteTask(TemplateCiteTaskDTO dto) {
        String templateId = dto.getTemplateId();
        String productId = dto.getProductId();
        ProjectTemplateEntity template = templateService.getById(templateId);
        if (Objects.isNull(template)) {
            throw new ServiceException(ApiError.ERROR_95051);
        }
        if (dto.getTaskIdList().size() <= 0 || dto.getTaskIdList() == null) {
            return true;
        }
        //复制模板团队成员
        List<CopySourceDTO> copyMembersSourceList = templateMembersService.copyTemplateMembers(template.getId(), productId, "");
        //复制模板角色
        List<CopySourceDTO> copyRoleSourceList = templateRoleService.copyTemplateRole(templateId, productId, "");
        //复制角色关系表
        templateRoleRefMembersService.copyTemplateRoleRefMembers(templateId, productId, "", copyRoleSourceList, copyMembersSourceList);
        //复制 项目任务阶段
        List<CopySourceDTO> phaseSourceList = templatePhaseService.copyTemplatePhase(templateId, productId, "");

        //复制任务文档名 可能数据库已有数据
        List<CopySourceDTO> docsNameSourceList = templateTaskDocsNameService.copyTemplateDocsName(templateId, productId, "");

        //这个是任务的
        List<CopySourceDTO> taskSourceList = taskCopyTemplate(templateId, productId, "", phaseSourceList, dto.getTaskIdList());
        //这个是复制前置任务关系
        templatePreTaskService.copyTemplatePreTask(templateId, productId, taskSourceList);

        //这个是交付文档
        List<CopySourceDTO> deliveryDocsSourceList = templateDeliveryDocsService.copyTemplateDeliveryDocs(templateId, productId, taskSourceList, docsNameSourceList);
        //这个是文档权限
        templateDocsPermissionService.copyTemplateDeliveryDocs(templateId, productId, taskSourceList, deliveryDocsSourceList);

        //复制模板sku 与任务关系
        templateTaskRefSkuConfigService.copyTemplateTaskSkuConfig(templateId, productId, taskSourceList);

        ProductInfoEntity productInfoEntity = productInfoService.getById(productId);
/*            String roleName = "产品经理";
            List<MemberPagingShowDTO> memberList = projectMembersService.listByRoleNames(null, productId, roleName);
            List<String> chargeIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(memberList)) {
                chargeIds = memberList.stream().map(MemberPagingShowDTO::getMemberName).collect(Collectors.toList());
            } else {
                chargeIds = Arrays.asList(productInfoEntity.getChargeId().split(","));
            }

            //新增或修改产品经理角色和对应成员
            projectMembersService.saveByRoleAndMembers(productId, null, "产品经理", chargeIds);*/

        List<String> chargeIds = Arrays.asList(productInfoEntity.getChargeId().split(","));
        projectMembersService.saveByRoleAndMembers(productId, null, "产品经理", chargeIds);

        /**
         * 当项目经理不为空的时候保经理
         */
        if (StringUtils.isNotBlank(productInfoEntity.getProjectChargeId())) {
/*                roleName = "项目经理";
                memberList = projectMembersService.listByRoleNames(null, productId, roleName);
                List<String> projectChargeIds = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(memberList)) {
                    projectChargeIds = memberList.stream().map(MemberPagingShowDTO::getMemberName).collect(Collectors.toList());
                } else {
                    projectChargeIds = Arrays.asList(productInfoEntity.getProjectChargeId().split(","));
                }
                //新增或修改项目经理角色和对应成员
                projectMembersService.saveByRoleAndMembers(productId, null, "项目经理", projectChargeIds);*/
            projectMembersService.saveByRoleAndMembers(productId, null, "项目经理", Arrays.asList(productInfoEntity.getProjectChargeId()));
        }

        //更新项目列表的项目经理
        projectInfoService.updateChargeByProductId(productId, productInfoEntity.getProjectChargeId());
        return true;

    }

    public List<CopySourceDTO> taskCopyTemplate(String templateId, String productId, String projectId, List<CopySourceDTO> phaseSourceList, List<String> taskIdList) {
        // 查询模板
        ProjectTemplateEntity projectTemplateEntity = projectTemplateService.getById(templateId);
        if (ObjectUtils.isEmpty(projectTemplateEntity) || !MathUtil.ONE.equals(projectTemplateEntity.getStatus())) {
            return new ArrayList<>();
        }
        List<TemplateTaskEntity> list = this.getByTemplateId(templateId, taskIdList);
        List<TemplateTaskFollowerEntity> templateTaskFollowerList = templateTaskFollowerService.listTemplateFollower(templateId, taskIdList);
        List<ProjectTaskEntity> byProductId = projectTaskService.getByProductId(productId);

        LoginUser loginUser = UserContext.getDefaultLoginUser();
        //来源信息
        List<CopySourceDTO> sourceList = new ArrayList<>();
        List<ProjectTaskEntity> copyList = new ArrayList<>(list.size());
        if (CollectionUtils.isNotEmpty(list)) {
            for (TemplateTaskEntity item : list) {
                ProjectTaskEntity projectTaskEntity = byProductId.stream().filter(projectMembers -> projectMembers.getName().equals(item.getName())).findFirst().orElse(null);
                if (!Objects.isNull(projectTaskEntity)) {
                    throw new ServiceException(ApiError.ERROR_95013);
                }

                CopySourceDTO source = new CopySourceDTO();
                String taskId = IdWorker.getIdStr();
                ProjectTaskEntity taskEntity = new ProjectTaskEntity();
                BeanMapper.copy(item, taskEntity);
                taskEntity.setProductId(productId);
                taskEntity.setProjectId(projectId);
                taskEntity.setId(taskId);
                String chargeId = item.getChargeId();
                List<String> chargeIdList = new ArrayList<>();
                if (StringUtils.isNotBlank(chargeId)) {
                    chargeIdList = Arrays.asList(chargeId.split(","));
                }

                source.setNewCreateId(taskId);
                source.setDataId(item.getId());
                CopySourceDTO phase = phaseSourceList.stream().filter(p -> p.getDataId()
                        .equals(item.getPhaseId())).findFirst().orElse(null);
                if (phase != null) {
                    taskEntity.setPhaseId(phase.getNewCreateId());
                } else {
                    taskEntity.setPhaseId("");
                }
                if (ObjectUtils.isNotEmpty(projectTemplateEntity)) {
                    //判断负责人分配方式是否是角色
                    if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(taskEntity.getDistributionType())) {
                        List<String> roleIds = Arrays.stream(item.getRoleId().split(",")).collect(Collectors.toList());

                        List<MemberPagingShowDTO> memberPagingShowDTOS = projectMembersService.listByRoleNames(roleIds, templateId, null);
//                        List<TemplateMembersEntity> templateMembersList = templateMembersService.listByRoleNames(roleIds, projectTemplateEntity.getId());
                        if (CollectionUtils.isNotEmpty(memberPagingShowDTOS)) {
                            List<String> memberIds = memberPagingShowDTOS.stream().map(MemberPagingShowDTO::getMemberId).distinct().collect(Collectors.toList());
                            List<String> memberNames = memberPagingShowDTOS.stream().map(MemberPagingShowDTO::getMemberName).distinct().collect(Collectors.toList());
                            taskEntity.setChargeId(StringUtils.join(memberIds, ","));
                            taskEntity.setChargeName(StringUtils.join(memberNames, ","));
                        }
                    }
                }
                copyList.add(taskEntity);
                sourceList.add(source);
                List<String> chargeIds = new ArrayList<>();
                if (StringUtils.isNotBlank(taskEntity.getChargeId())) {
                    chargeIds = Arrays.stream(taskEntity.getChargeId().split(",")).collect(Collectors.toList());
                }
                //查询模板任务下审核人
                List<TaskChargeDistributionEntity> taskChargeDistributionList = taskChargeDistributionService.listBySourceAndTaskId(MathUtil.TWO, item.getId());

                setTaskChargeDistribution(taskChargeDistributionList, chargeIds, projectTemplateEntity.getId(), taskEntity.getId(), MathUtil.THREE);

                //新增关注人
                List<TemplateTaskFollowerEntity> followerEntityList = templateTaskFollowerList.stream().filter(req -> req.getTemplateTaskId().equals(item.getId())).collect(Collectors.toList());
                List<String> followerUserIdList = followerEntityList.stream().map(TemplateTaskFollowerEntity::getUserId).collect(Collectors.toList());
                taskFollowerService.batchAdd(taskId, productId, followerUserIdList);
            }
        }

        //更改父id
        for (ProjectTaskEntity task : copyList) {
            //这个pid 还是 模板数据的pid
            String pid = task.getPid();
            if (!pid.equals("0")) {
                CopySourceDTO source = sourceList.stream().
                        filter(s -> s.getDataId().equals(pid)).findFirst().orElse(null);
                if (source != null) {
                    task.setPid(source.getNewCreateId());
                } else {
                    task.setPid("0");
                }
            }
        }

        Boolean flag = taskService.saveBatch(copyList);
        if (flag) {
            //发送新建任务通知
            noticeMessageService.newTaskNotice(loginUser.getUserName(), copyList, productId);

        }
        return sourceList;

    }

    @Override
    public TemplateTaskEntity getTaskByName(String templateId, String taskName) {
        return lambdaQuery().eq(TemplateTaskEntity::getTemplateId, templateId)
                .eq(TemplateTaskEntity::getName, taskName).one();
    }

    @Override
    public List<TemplateTaskEntity> listByTemplateId(String templateId) {
        return lambdaQuery().eq(TemplateTaskEntity::getTemplateId, templateId).list();
    }
}
