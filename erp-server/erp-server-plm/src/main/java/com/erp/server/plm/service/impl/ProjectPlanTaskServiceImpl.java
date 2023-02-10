package com.erp.server.plm.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.BaseStatusEnum;
import com.common.core.enums.CustomizeFieldEnum;
import com.common.core.enums.ModuleEnum;
import com.common.core.utils.ExcelUtil;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.ChangeTaskScheduleDTO;
import com.erp.model.plm.dto.HandleTaskScheduleDTO;
import com.erp.model.plm.dto.ProjectPlanTaskConditionDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.vo.*;
import com.erp.model.sys.dto.CustomizeFieldLayoutDTO;
import com.erp.model.sys.dto.FindCustomizeFieldDTO;
import com.erp.model.sys.vo.CustomizeFieldVO;
import com.erp.model.sys.vo.UserFieldVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.listener.ProjectPlanTaskExcelListener;
import com.erp.server.plm.mapper.ProjectPlanTaskMapper;
import com.erp.server.plm.mapper.ProjectTaskMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

            //根据阶段分组
            Map<String, List<ProductTaskVO>> map = taskList.stream().
                    collect(Collectors.groupingBy(ProductTaskVO::getPhaseId));

            int parentId = 1;
            for (Map.Entry<String, List<ProductTaskVO>> item : map.entrySet()) {
                ProductTaskVO parentVO = new ProductTaskVO();
                parentVO.setId(parentId);
                parentVO.setParentId(IsConstant.NO);

                List<ProductTaskVO> phaseTaskList = item.getValue();
                //阶段名
                String phaseName = phaseTaskList.get(0).getPhaseName();
                parentVO.setPhaseId(item.getKey());
                parentVO.setPhaseName(phaseName);
                parentId++;

                for (ProductTaskVO vo : phaseTaskList) {
                    String taskId = vo.getTaskId();
                    List<String> preTaskIds = preTaskList.stream().filter(p -> p.getTaskId().equals(taskId)).
                            map(PreTaskEntity::getPreTaskId).collect(Collectors.toList());
                    vo.setPreTaskIdList(preTaskIds);
                    List<String> preTaskNameList = taskList.stream().filter(t -> preTaskIds.contains(t.getTaskId()))
                            .map(ProductTaskVO::getName).collect(Collectors.toList());
                    vo.setPreTaskNames(String.join(",", preTaskNameList));
                    vo.setId(parentId);
                    vo.setParentId(parentVO.getId());
                    List<String> docsNameList = deliveryDocsList.stream().filter(d -> d.getTaskId().equals(taskId))
                            .map(TaskDeliveryDocsEntity::getDocsName).collect(Collectors.toList());
                    vo.setDeliveryDocsNames(String.join(",", docsNameList));
                    parentId++;
                }
                resultList.add(parentVO);
                resultList.addAll(phaseTaskList);

            }
        }

        resultVO.setTaskList(resultList);
        resultVO.setTotalTaskCount(taskList.size());
        return resultVO;
    }


    /**
     * 导出
     */
    @Override
    public void exportExcel(HandleTaskScheduleDTO dto, HttpServletResponse response) {
        List<ScheduleTaskExportExcelVO> excelList = projectTaskMapper.getExportScheduleTask(dto);
        for(ScheduleTaskExportExcelVO vo:excelList){
            String status=vo.getScheduleStatus();
            vo.setScheduleStatusName(BaseStatusEnum.getName(status));
        }
        String fileName = "任务数据";
        ExcelUtil.export(fileName, "task", excelList, ScheduleTaskExportExcelVO.class, response);
    }


    /**
     * 导入
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-03 18:37
     */
    @Override
    public Boolean importTaskSchedule(MultipartFile excelFile, HttpServletResponse response) {
        ProjectPlanTaskExcelListener excelListenerUtil = new ProjectPlanTaskExcelListener(projectTaskService,projectPlanService);
        try {
            EasyExcel.read(excelFile.getInputStream(), ScheduleTaskExportExcelVO.class,excelListenerUtil).sheet(0).doRead();
            List<ScheduleTaskExportExcelVO> list = excelListenerUtil.getDateList();
            if (CollectionUtils.isEmpty(list)) {
                return true;
            }
            String fileName = "排期错误";
            ExcelUtil.export(fileName,"task",list,ScheduleTaskExportExcelVO.class,response);
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

                List<ProjectPlanTaskEntity> planTaskEntityList = this.getByProjectPlanIdList(projectPlanIds);
                List<String> taskIdList = planTaskEntityList.stream().map(ProjectPlanTaskEntity::getTaskId).collect(Collectors.toList());
                //更改任务状态
                projectTaskService.updateScheduleStatus(productId, taskIdList, waitAuditStatus, "");
            }

        }
        return result;


    }

    /**
     * 变更排期
     * 只有审核通过才能变更
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-09 15:43
     */
    @Override
    public Boolean changeSchedule(List<ChangeTaskScheduleDTO> list) {
        Boolean result = true;
        if (CollectionUtils.isNotEmpty(list)) {
            String productId = list.get(0).getProductId();
            List<String> taskIdList = list.stream().map(ChangeTaskScheduleDTO::getTaskId).collect(Collectors.toList());
            List<ScheduleTaskVO> taskList = this.getPlanTaskByTaskIds(productId, taskIdList);
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

            result = projectPlanService.changeSchedule(list);
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

}
