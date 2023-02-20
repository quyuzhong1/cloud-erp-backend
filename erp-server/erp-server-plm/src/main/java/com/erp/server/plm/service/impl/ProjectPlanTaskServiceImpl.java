package com.erp.server.plm.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.BaseStatusEnum;
import com.common.core.enums.CustomizeFieldEnum;
import com.common.core.enums.ModuleEnum;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.erp.common.business.utils.FastDFSClientUtil;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.vo.*;
import com.erp.model.sys.dto.CustomizeFieldLayoutDTO;
import com.erp.model.sys.dto.FindCustomizeFieldDTO;
import com.erp.model.sys.vo.CustomizeFieldVO;
import com.erp.model.sys.vo.UserFieldVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.ProjectPlanConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.server.plm.listener.ChangeScheduleExcelListener;
import com.erp.server.plm.listener.ProjectPlanTaskExcelListener;
import com.erp.server.plm.mapper.ProjectPlanTaskMapper;
import com.erp.server.plm.mapper.ProjectTaskMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目计划任务表(ProjectPlanTask)表服务实现类
 *
 * @author yl
 * @since 2023-02-03 15:05:42
 */
@Service
public class ProjectPlanTaskServiceImpl extends ServiceImpl<ProjectPlanTaskMapper, ProjectPlanTaskEntity> implements ProjectPlanTaskService {


    @Resource
    private ProjectTaskMapper projectTaskMapper;

    @Resource
    private ProjectTaskService projectTaskService;

    @Resource
    private PreTaskService preTaskService;

    @Resource
    private TaskDeliveryService taskDeliveryService;


    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CommonService commonService;

    @Resource
    private ProjectPlanService projectPlanService;

    /**
     * 根据条件获取到项目计划任务
     *
     * @param dto
     * @return com.erp.model.plm.vo.ProductItemScheduleVO
     * @author yl
     * @date 2023-02-03 15:54
     */
    @Override
    public ProductItemScheduleVO getTaskList(ProjectPlanTaskConditionDTO dto) {
        ProductItemScheduleVO resultVO = new ProductItemScheduleVO();
        String productId = dto.getProductId();
        List<ProductTaskVO> taskList = projectTaskMapper.getScheduleTask(dto);
        List<ProductTaskVO> resultList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(taskList)) {
            //前置任务列表
            List<PreTaskEntity> preTaskList = preTaskService.getPreTaskByProductId(dto.getProductId());
            //交付文档列表
            List<TaskDeliveryDocsEntity> deliveryDocsList = taskDeliveryService.getByProductId(productId);

            //排期状态
            List<String> scheduleStatusList = new ArrayList<>();
            scheduleStatusList.add(BaseStatusEnum.AUDIT_PASS.getStatus());
            scheduleStatusList.add(BaseStatusEnum.AUDIT_PASS.getStatus());

            //根据阶段分组
            Map<String, List<ProductTaskVO>> map = taskList.stream().
                    collect(Collectors.groupingBy(ProductTaskVO::getPhaseId));
            //变更
            String change = ProjectPlanConstant.PROJECT_PLAN_CHANGE;
            int parentId = 1;
            for (Map.Entry<String, List<ProductTaskVO>> item : map.entrySet()) {
                ProductTaskVO parentVO = new ProductTaskVO();
                parentVO.setId(parentId);
                parentVO.setParentId(IsConstant.NO);


                List<ProductTaskVO> phaseTaskList = item.getValue();


                //最小计划开始时间
                String minStartTime = phaseTaskList.stream().filter(obj -> ObjectUtils.isNotNull(obj.getPlanStartTime())).sorted(Comparator.comparing(ProductTaskVO::getPlanStartTime)).map(ProductTaskVO::getPlanStartTime).findFirst().orElse(null);
                //最大计划结束时间
                String maxEndTime = phaseTaskList.stream().filter(obj -> ObjectUtils.isNotNull(obj.getPlanEndTime())).sorted(Comparator.comparing(ProductTaskVO::getPlanEndTime).reversed()).map(ProductTaskVO::getPlanEndTime).findFirst().orElse(null);

                //阶段名
                String phaseName = phaseTaskList.get(0).getPhaseName();
                parentVO.setPhaseId(item.getKey());
                parentVO.setPhaseName(phaseName);

                parentVO.setPlanStartTime(StringUtils.isEmpty(minStartTime) ? minStartTime : minStartTime.concat(" 00:00:00"));
                parentVO.setPlanEndTime(StringUtils.isEmpty(maxEndTime) ? maxEndTime : maxEndTime.concat(" 23:59:59"));
                parentId++;

                for (ProductTaskVO vo : phaseTaskList) {
                    String taskId = vo.getTaskId();
                    List<String> preTaskIds = preTaskList.stream().filter(p -> p.getTaskId().equals(taskId)).
                            map(PreTaskEntity::getPreTaskId).collect(Collectors.toList());
                    vo.setPreTaskIdList(preTaskIds);
                    List<String> preTaskNameList = taskList.stream().filter(t -> preTaskIds.contains(t.getTaskId()))
                            .map(ProductTaskVO::getTaskName).collect(Collectors.toList());
                    vo.setPreTaskNames(String.join(",", preTaskNameList));
                    vo.setId(parentId);
                    vo.setParentId(parentVO.getId());
                    vo.setPlanStartTime(StringUtils.isEmpty(vo.getPlanStartTime()) ? vo.getPlanStartTime() : vo.getPlanStartTime().concat(" 00:00:00"));
                    vo.setPlanEndTime(StringUtils.isEmpty(vo.getPlanEndTime()) ? vo.getPlanEndTime() : vo.getPlanEndTime().concat(" 23:59:59"));
                    vo.setRealityStartTime(StringUtils.isEmpty(vo.getRealityStartTime()) ? vo.getRealityStartTime() : vo.getRealityStartTime().concat(" 00:00:00"));
                    vo.setRealityEndTime(StringUtils.isEmpty(vo.getRealityEndTime()) ? vo.getRealityEndTime() : vo.getRealityEndTime().concat(" 23:59:59"));

                    List<String> docsNameList = deliveryDocsList.stream().filter(d -> d.getTaskId().equals(taskId))
                            .map(TaskDeliveryDocsEntity::getDocsName).collect(Collectors.toList());
                    vo.setDeliveryDocsNames(String.join(",", docsNameList));
                    vo.setStatusName(TaskStateEnum.getName(vo.getStatus()));
                    //排期状态
                    String scheduleStatus = vo.getScheduleStatus();
                    //排期类型
                    String scheduleType = vo.getScheduleType();
                    //如果是变更 且不在 两个状态中 就是变更
                    if (change.equals(scheduleType)) {
                        if (!scheduleStatusList.contains(scheduleStatus)) {
                            vo.setIsChange(true);
                        }
                    }
                    Integer type = vo.getType();
                    String typeName = "一般任务";
                    if (TaskConstant.REVIEW_TASK.equals(type)) {
                        typeName = "评审任务";
                    }
                    vo.setTypeName(typeName);
                    //优先级
                    Integer priority = vo.getPriority();

                    String priorityName="低级";
                    if(TaskConstant.INTERMEDIATE_TASK.equals(priority)){
                        priorityName="中级";
                    }else if(TaskConstant.ADVANCED_TASK.equals(priority)){
                        priorityName="高级";
                    }
                    vo.setPriorityName(priorityName);
                    //设置里程碑
                    Integer isMilepost=vo.getIsMilepost();
                    String isMilepostName="否";
                    if(TaskConstant.YES_MILEPOST.equals(isMilepost)){
                        isMilepostName="是";
                    }
                    vo.setIsMilepostName(isMilepostName);

                    vo.setScheduleStatusName(BaseStatusEnum.getName(vo.getScheduleStatus()));
                    parentId++;
                }
                resultList.add(parentVO);
                resultList.addAll(phaseTaskList);

            }
        }
        //取值统计为已经有排期时间的统计数值
        String waitSubmit = BaseStatusEnum.WAIT_SUBMIT.getStatus();
        Integer scheduleTaskCount = Math.toIntExact(taskList.stream().filter(t -> t.getPlanEndTime() != null &&
                t.getPlanStartTime() != null && !waitSubmit.equals(t.getScheduleStatus())).count());

        /**
         * 未排期任务数
         */
        Integer unscheduledTaskCount = taskList.size() - scheduleTaskCount;
        /**
         * 审核的任务数
         * 取值为排期审核状态为审核通过的数值统计
         */
        String auditPassStatus = BaseStatusEnum.AUDIT_PASS.getStatus();
        Integer scheduleAuditTaskCount = Math.toIntExact(taskList.stream().filter(t -> t.getPlanEndTime() != null &&
                t.getPlanStartTime() != null && auditPassStatus.equals(t.getScheduleStatus())).count());


        resultVO.setTaskList(resultList);
        resultVO.setScheduleTaskCount(scheduleTaskCount);
        resultVO.setUnscheduledTaskCount(unscheduledTaskCount);
        resultVO.setScheduleAuditTaskCount(scheduleAuditTaskCount);
        resultVO.setTotalTaskCount(taskList.size());
        return resultVO;
    }


    /**
     * 导出排期任务
     * 数据
     */
    @Override
    public void exportExcel(ProjectPlanTaskConditionDTO dto, HttpServletResponse response) {
        List<ProductTaskVO> taskList = projectTaskMapper.getScheduleTask(dto);
        for (ProductTaskVO vo : taskList) {
            String status = vo.getScheduleStatus();
            vo.setScheduleStatusName(BaseStatusEnum.getName(status));
        }
        List<ScheduleTaskExportExcelVO> excelList = BeanMapper.copyList(taskList, ScheduleTaskExportExcelVO.class);
        String fileName = "任务数据";
        ExcelUtil.export(fileName, "task", excelList, ScheduleTaskExportExcelVO.class, response);
    }


    /**
     * 导入任务排期
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-03 18:37
     */
    @Override
    public Boolean importTaskSchedule(MultipartFile excelFile, String productId, HttpServletResponse response) {
        if (StringUtils.isBlank(productId)) {
            throw new ServiceException(95010, "产品id不能为空");
        }
        ProjectPlanTaskExcelListener excelListenerUtil = new ProjectPlanTaskExcelListener(projectTaskService, projectPlanService, productId);
        try {
            EasyExcel.read(excelFile.getInputStream(), ScheduleTaskExportExcelVO.class, excelListenerUtil).sheet(0).doRead();
            List<ScheduleTaskExportExcelVO> list = excelListenerUtil.getDateList();
            if (CollectionUtils.isEmpty(list)) {
                return true;
            }
            String fileName = "排期错误";
            ExcelUtil.export(fileName, "task", list, ScheduleTaskExportExcelVO.class, response);
        } catch (IOException e) {
            throw new ServiceException(ApiError.Default);
        }

        return false;
    }

    @Override
    public Boolean fieldSet(CustomizeFieldLayoutDTO dto) {

        String userId = commonService.getUserInfo().getUid();
        dto.setModuleCode(ModuleEnum.PLM_SCHEDULE_TASK.getCode());
        dto.setUserId(userId);
        dto.setModuleName(ModuleEnum.PLM_SCHEDULE_TASK.getName());
        dto.setLayoutJson(dto.getLayoutJson());


        return sysUserFeign.batchAdd(dto);
    }

    @Override
    public List<CustomizeFieldVO> allField() {
        String code = ModuleEnum.PLM_SCHEDULE_TASK.code;
        List<CustomizeFieldEnum> customizeFieldList = CustomizeFieldEnum.getByModuleCode(code);
        List<CustomizeFieldVO> resultList = new ArrayList<>(customizeFieldList.size());
        for (CustomizeFieldEnum item : customizeFieldList) {
            CustomizeFieldVO dto = new CustomizeFieldVO();
            dto.setFieldName(item.getFieldName());
            dto.setFieldTitle(item.getFieldTitle());
            dto.setIsDefault(item.getIsDefault());
            dto.setModuleCode(item.getModuleCode());
            dto.setModuleName(item.getModuleName());
            resultList.add(dto);
        }
        return resultList;
    }


    /**
     * 获取用户设置的字段
     *
     * @return
     */
    @Override
    public UserFieldVO getUserField() {
        FindCustomizeFieldDTO dto = new FindCustomizeFieldDTO();
        dto.setUserId(commonService.getUserInfo().getUid());
        dto.setModuleCode(ModuleEnum.PLM_SCHEDULE_TASK.code);
        return sysUserFeign.getByUserId(dto);

    }


    /**
     * 获取到审核的任务
     *
     * @param productId
     * @return
     */
    public List<ScheduleTaskVO> getScheduleTaskList(String productId, String status) {
        return baseMapper.getScheduleTaskList(productId, status);
    }


    /**
     * 保存任务
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-02-08 19:16
     */
    @Override
    public void savePlanTask(String projectPlanId, String productId, List<ProjectTaskEntity> taskList) {
        if (CollectionUtils.isNotEmpty(taskList)) {
            List<ProjectPlanTaskEntity> saveList = new ArrayList<>(taskList.size());
            List<String> taskId = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            for (ProjectTaskEntity item : taskList) {
                ProjectPlanTaskEntity entity = new ProjectPlanTaskEntity();
                entity.setChangeEndTime(item.getPlanEndTime());
                entity.setChangeStartTime(item.getPlanStartTime());
                entity.setOriginEndTime(item.getPlanEndTime());
                entity.setOriginStartTime(item.getPlanStartTime());
                entity.setProductId(productId);
                entity.setProjectPlanId(projectPlanId);
                entity.setTaskId(item.getId());
                entity.setOriginChargeId(item.getChargeId());
                entity.setChangeChargeId(item.getChargeId());
                saveList.add(entity);
            }

            boolean flag = this.saveBatch(saveList);
            if (flag) {

                projectTaskService.updateScheduleStatus(productId, taskId, BaseStatusEnum.WAIT_AUDIT.getStatus(), "");

            }
        }

    }


    /**
     * 根据任务id 获取到任务的情况
     *
     * @param productId
     * @param taskIdList
     * @return java.util.List<com.erp.model.plm.vo.ScheduleTaskVO>
     * @author yl
     * @date 2023-02-09 9:57
     */
    @Override
    public List<ScheduleTaskVO> getByTaskIds(String productId, List<String> taskIdList) {
        return baseMapper.getByTaskIds(productId, taskIdList);
    }

    @Override
    public List<ScheduleTaskVO> getPlanTaskByTaskIds(String productId, List<String> taskIdList) {
        if (CollectionUtils.isNotEmpty(taskIdList)) {
            return baseMapper.getByTaskIds(productId, taskIdList);
        }
        return new ArrayList<>();
    }

    @Override
    public List<ProjectPlanTaskEntity> getByTaskIdList(String productId, List<String> taskIdList) {
        if (CollectionUtils.isNotEmpty(taskIdList)) {
            LambdaQueryWrapper<ProjectPlanTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ProjectPlanTaskEntity::getTaskId, taskIdList);
            queryWrapper.eq(ProjectPlanTaskEntity::getProductId, productId);
            return this.list(queryWrapper);
        }

        return new ArrayList<>();
    }


    /**
     * 取消任务排期
     * 只有待审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-09 14:39
     */
    @Override
    @Transactional
    public Boolean cancelSchedule(HandleTaskScheduleDTO dto) {
        String productId = dto.getProductId();
        Boolean result = true;
        //项目计划表id
        if (CollectionUtils.isNotEmpty(dto.getTaskIdList())) {
            List<ScheduleTaskVO> taskList = projectTaskService.getScheduleTaskByTaskIds(productId, dto.getTaskIdList());
            String waitAudit = BaseStatusEnum.WAIT_AUDIT.getStatus();
            //初始提交
            Long count = taskList.stream().filter(p -> !waitAudit.equals(p.getScheduleStatus()))
                    .count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_95121);
            }

            String cancelStatus = BaseStatusEnum.CANCEL.getStatus();
            List<ProjectPlanTaskEntity> planTaskList = this.getByTaskIdList(productId, dto.getTaskIdList());
            //获取到项目计划的表id
            List<String> projectPlanIds = planTaskList.stream().map(ProjectPlanTaskEntity::getProjectPlanId).collect(Collectors.toList());
            //去重
            projectPlanIds = projectPlanIds.stream().distinct().collect(Collectors.toList());
            List<ProjectPlanEntity> planList = projectPlanService.getByIds(projectPlanIds);
            if (CollectionUtils.isNotEmpty(planList)) {
                planList.stream().forEach(
                        p -> p.setStatus(cancelStatus)
                );
                //更改审核状态
                result = projectPlanService.saveOrUpdateBatch(planList);
                List<ProjectPlanTaskEntity> planTaskEntityList = this.getByProjectPlanIdList(projectPlanIds);
                List<String> taskIdList = planTaskEntityList.stream().map(ProjectPlanTaskEntity::getTaskId).collect(Collectors.toList());
                //更改任务状态
                projectTaskService.updateScheduleStatus(productId, taskIdList, cancelStatus, "");
            }

        }
        return result;
    }

    /**
     * 根据项目id  获取对应任务
     *
     * @param projectPlanIds
     * @return
     */
    @Override
    public List<ProjectPlanTaskEntity> getByProjectPlanIdList(List<String> projectPlanIds) {
        if (CollectionUtils.isNotEmpty(projectPlanIds)) {
            LambdaQueryWrapper<ProjectPlanTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ProjectPlanTaskEntity::getProjectPlanId, projectPlanIds);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }

    /**
     * 重新启动
     * 只有审核不通过才能重新启动
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-09 15:18
     */
    @Override
    public Boolean restartSchedule(HandleTaskScheduleDTO dto) {
        String productId = dto.getProductId();
        //检查审核人
        projectPlanService.checkAuditor();

        Boolean result = true;
        if (CollectionUtils.isNotEmpty(dto.getTaskIdList())) {
            List<ScheduleTaskVO> taskList = this.getPlanTaskByTaskIds(productId, dto.getTaskIdList());
            String auditNoPassStatus = BaseStatusEnum.AUDIT_NO_PASS.getStatus();
            //初始提交
            Long count = taskList.stream().filter(p -> !auditNoPassStatus.equals(p.getScheduleStatus()))
                    .count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_95119);
            }
            //待审核
            String waitAuditStatus = BaseStatusEnum.WAIT_AUDIT.getStatus();
            List<ProjectPlanTaskEntity> planTaskList = this.getByTaskIdList(productId, dto.getTaskIdList());
            //获取到项目计划的表id
            List<String> projectPlanIds = planTaskList.stream().map(ProjectPlanTaskEntity::getProjectPlanId).collect(Collectors.toList());
            //去重
            projectPlanIds = projectPlanIds.stream().distinct().collect(Collectors.toList());
            List<ProjectPlanEntity> planList = projectPlanService.getByIds(projectPlanIds);
            if (CollectionUtils.isNotEmpty(planList)) {
                planList.stream().forEach(
                        p -> p.setStatus(waitAuditStatus)
                );
                //更改审核状态
                result = projectPlanService.updateBatchById(planList);
                //启动流程吗？
                startProcess(projectPlanIds);


                List<ProjectPlanTaskEntity> planTaskEntityList = this.getByProjectPlanIdList(projectPlanIds);
                List<String> taskIdList = planTaskEntityList.stream().map(ProjectPlanTaskEntity::getTaskId).collect(Collectors.toList());
                //更改任务状态
                projectTaskService.updateScheduleStatus(productId, taskIdList, waitAuditStatus, "");
            }

        }
        return result;


    }


    /**
     * 启动流程
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-02-13 9:25
     */
    public void startProcess(List<String> projectPlanIds) {
        for (String planId : projectPlanIds) {
            projectPlanService.startScheduleTaskProcess(planId);
        }


    }

    /**
     * 变更排期
     * 只有审核通过才能变更
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-09 15:43
     */
    @Override
    public Boolean changeSchedule(ChangeScheduleDTO dto) {
        Boolean result = true;
        List<ChangeTaskScheduleDTO> list = dto.getChangeTaskList();
        if (CollectionUtils.isNotEmpty(list)) {
            String productId = dto.getProductId();
            List<String> taskIdList = list.stream().map(ChangeTaskScheduleDTO::getTaskId).collect(Collectors.toList());
            List<ScheduleTaskVO> taskList = projectTaskService.getScheduleTaskByTaskIds(productId, taskIdList);
            String auditPassStatus = BaseStatusEnum.AUDIT_PASS.getStatus();
            Long count = taskList.stream().filter(p -> !auditPassStatus.equals(p.getScheduleStatus()))
                    .count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_95120);
            }
            //检查时间
            long timeCount = taskList.stream().
                    filter(t -> Objects.isNull(t.getPlanEndTime()) || Objects.isNull(t.getPlanStartTime())).
                    count();
            if (timeCount > 0) {
                throw new ServiceException(ApiError.ERROR_95010);
            }

            result = projectPlanService.changeSchedule(productId, list);
        }

        return result;
    }


    /**
     * 保存变更的任务
     *
     * @param projectPlanId
     * @param productId
     * @param taskList
     * @param list
     * @return void
     * @author yl
     * @date 2023-02-09 16:00
     */
    @Override
    public void saveChangePlanTask(String projectPlanId, String productId, List<ProjectTaskEntity> taskList, List<ChangeTaskScheduleDTO> list) {
        List<ProjectPlanTaskEntity> addList = new ArrayList<>(list.size());
        for (ProjectTaskEntity item : taskList) {
            String taskId = item.getId();
            ProjectPlanTaskEntity entity = new ProjectPlanTaskEntity();
            entity.setProjectPlanId(projectPlanId);
            entity.setOriginChargeId(item.getChargeId());
            entity.setOriginStartTime(item.getPlanStartTime());
            entity.setOriginEndTime(item.getPlanEndTime());

            ChangeTaskScheduleDTO changeTask = list.stream().filter(t -> t.getTaskId().equals(taskId)).findFirst().orElse(null);
            //从参数里面取
            if (changeTask != null) {
                List<String> chargeIds = changeTask.getChargeIdList();
                if (CollectionUtils.isNotEmpty(chargeIds)) {
                    entity.setChangeChargeId(String.join(",", chargeIds));
                } else {
                    entity.setChangeChargeId(item.getChargeId());
                }
                entity.setChangeStartTime(changeTask.getPlanStartTime());
                entity.setChangeEndTime(changeTask.getPlanEndTime());
                if (changeTask.getIsRestart() != null) {
                    entity.setIsRestart(changeTask.getIsRestart());
                }
            } else {
                entity.setChangeStartTime(item.getPlanStartTime());
                entity.setChangeEndTime(item.getPlanEndTime());
                entity.setChangeChargeId(item.getChargeId());
            }

            addList.add(entity);
        }
        this.saveBatch(addList);
    }


    /**
     * 获取到排期 类型的 任务
     *
     * @param productId
     * @param projectPlanChange
     * @return java.util.List<com.erp.model.plm.entity.ProjectPlanEntity>
     * @author yl
     * @date 2023-02-09 17:50
     */
    @Override
    public List<ScheduleTaskDetailsVO> getTaskByPlanType(String productId, String projectPlanChange) {

        return baseMapper.getTaskByPlanType(productId, projectPlanChange);
    }


    /**
     * 查询变更 排期的任务
     *
     * @param dto
     * @return java.util.List<com.erp.model.plm.vo.ScheduleChangeTaskVO>
     * @author yl
     * @date 2023-02-14 8:27
     */
    @Override
    public List<ScheduleChangeTaskVO> getChangeTaskList(BaseIdDTO dto) {
        List<ScheduleChangeTaskVO> list = baseMapper.getChangeTaskList(dto.getId(), dto.getName(), BaseStatusEnum.AUDIT_PASS.getStatus());
        for (ScheduleChangeTaskVO vo : list) {
            vo.setStatusName(TaskStateEnum.getName(vo.getStatus()));
        }

        //前期任务
        List<PreTaskEntity> preTaskList = preTaskService.getPreTaskByProductId(dto.getId());
        List<String> taskIdList = preTaskList.stream().map(PreTaskEntity::getTaskId).collect(Collectors.toList());

        return list.stream().filter(t -> !taskIdList.contains(t.getTaskId())).collect(Collectors.toList());
    }


    /**
     * 导出排期变更
     *
     * @param dto
     * @param response
     * @return void
     * @author yl
     * @date 2023-02-14 10:39
     */
    @Override
    public void exportChangeSchedule(HandleTaskScheduleDTO dto, HttpServletResponse response) {
        List<ChangeScheduleExportVO> excelList = projectTaskMapper.getExportChangeScheduleTask(dto);
        for (ChangeScheduleExportVO vo : excelList) {
            Integer status = vo.getStatus();
            vo.setStatusName(TaskStateEnum.getName(status));
        }
        String fileName = "变更排期任务数据";
        ExcelUtil.export(fileName, "task", excelList, ChangeScheduleExportVO.class, response);
    }


    /**
     * 导入排期变更 数据
     *
     * @param excelFile
     * @param response
     * @return void
     * @author yl
     * @date 2023-02-14 11:25
     */
    @Override
    public ChangeScheduleExportResultVO importChangeSchedule(MultipartFile excelFile, HttpServletResponse response, String productId) {
        if (StringUtils.isBlank(productId)) {
            throw new ServiceException(95010, "产品id不能为空");
        }
        ChangeScheduleExportResultVO vo = new ChangeScheduleExportResultVO();
        ChangeScheduleExcelListener excelListener = new ChangeScheduleExcelListener(projectTaskService, productId);
        try {
            EasyExcel.read(excelFile.getInputStream(), ScheduleTaskExportExcelVO.class, excelListener).sheet(0).doRead();
            List<ScheduleTaskExportExcelVO> errorDateList = excelListener.getErrorDateList();

            String fileName = "排期变更错误.xlsx";
            File file = ExcelUtil.exportFile(fileName, "task", errorDateList, ScheduleTaskExportExcelVO.class);
            String url = "";
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
            vo.setErrorUrl(url);
            vo.setSucceedList(excelListener.getSucceedDateList());

        } catch (IOException e) {
            throw new ServiceException(ApiError.Default);
        }

        return vo;
    }


    /**
     * 导出任务排期
     *
     * @param request
     * @param response
     * @return void
     * @author yl
     * @date 2023-02-17 11:43
     */
    @Override
    public void exportScheduleTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/scheduleTask.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.Default);
        }
    }

}
