package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProjectInfoEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.SourceType;
import com.erp.server.plm.enums.ProductInfoStateEnum;
import com.erp.server.plm.enums.ProjectStateEnum;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.mapper.ProjectInfoMapper;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProjectInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.server.plm.service.ProjectMembersService;
import com.erp.server.plm.service.ProjectTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 产品项目表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class ProjectInfoServiceImpl extends ServiceImpl<ProjectInfoMapper, ProjectInfoEntity> implements ProjectInfoService {

    @Autowired
    private ProjectMembersService projectMembersService;

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private ProductInfoService productInfoService;


    /**
     * 项目概述
     *
     * @param productId
     * @return java.util.List<com.erp.model.plm.dto.PhaseDistributeDTO>
     * @author yl
     * @date 2022-09-19 12:23
     */
    @Override
    public ProjectInfoDTO projectInfo(String productId) {
        ProductInfoEntity entity = productInfoService.getById(productId);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }
        ProjectInfoDTO result = new ProjectInfoDTO();
        List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
        //产品名
        result.setProductName(entity.getName());
        Map<String, Integer> taskMap = getTaskCount(productId,taskList,new Date());
        Integer totalTaskCount=taskMap.get("totalTaskCount");
        Integer finishTaskCount=taskMap.get("finishTaskCount");
        Integer postponeTaskCount=taskMap.get("postponeTaskCount");
        Integer unfinishedTaskCount=taskMap.get("unfinishedTaskCount");
        int finishRatio = 0;
        int postponeRatio = 0;
        if (totalTaskCount != 0) {
            finishRatio = (finishTaskCount / totalTaskCount) * 100;
            postponeRatio = (postponeTaskCount / totalTaskCount) * 100;
        }
        result.setFinishTaskCount(finishTaskCount);
        result.setUnfinishedTaskCount(unfinishedTaskCount);
        result.setTotalTaskCount(totalTaskCount);
        result.setFinishRatio(finishRatio);
        result.setPostponeTaskCount(postponeTaskCount);
        result.setPostponeRatio(postponeRatio);

        //获取任务阶段分布
        PhaseDistributeDTO taskPhase = phaseDistributeList(taskList);
        result.setPhaseDistribute(taskPhase);
        List<Map<String, Object>> finishTaskTrend = getFinishTaskTrend(30, taskList);
        result.setFinishTaskTrend(finishTaskTrend);
        return result;
    }


    //获取到任务的数量
    public Map<String, Integer> getTaskCount(String productId,List<ProjectTaskEntity> taskList, Date date) {
        if(CollectionUtils.isEmpty(taskList)&& StringUtils.isNotBlank(productId)){
            taskList = projectTaskService.getByProductId(productId);
        }
        //完成任务数
        int finishTaskCount = taskList.stream().filter(t -> TaskStateEnum.FINISH.getCode().equals(t.getStatus())).collect(Collectors.toList()).size();
        //未完成任务数
        int unfinishedTaskCount = taskList.stream().filter(t -> !TaskStateEnum.FINISH.getCode().equals(t.getStatus())).collect(Collectors.toList()).size();
        //总任务数
        int totalTaskCount = taskList.size();
        //延期的任务数
        int postponeTaskCount = taskList.stream().filter(t -> date.compareTo(t.getPlanEndTime()) == 1).collect(Collectors.toList()).size();
        Map<String, Integer> map = new HashMap<>(4);
        map.put("finishTaskCount", finishTaskCount);
        map.put("unfinishedTaskCount", unfinishedTaskCount);
        map.put("totalTaskCount", totalTaskCount);
        map.put("postponeTaskCount", postponeTaskCount);
        return map;

    }

    /**
     * 启动项目
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-20 16:05
     */
    @Override
    @Transactional
    public Boolean startProject(StartProjectDTO dto) {
        String productId = dto.getProductId();
        ProductInfoEntity product = productInfoService.getById(productId);
        if (Objects.isNull(product)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }
        ProjectInfoEntity entity = new ProjectInfoEntity();
        BeanMapper.copy(dto, entity);
        entity.setProductName(product.getName());
        boolean flag = save(entity);
        Integer sourceType = dto.getSourceType();
        if (flag) {
            String projectId = entity.getId();
            String flagId = dto.getFlagId();
            //如果是新建 就直接 复制成员
            if (SourceType.NEW.equals(sourceType)) {
                projectMembersService.add(productId, projectId, dto.getMembers());
                //从复制系统项目任务
                projectTaskService.copyTaskBySys(productId, projectId);
            }
            //如果是 从项目复制 那么从项目表 里面复制 复制成员
            if (SourceType.PROJECT.equals(sourceType)) {
                projectMembersService.saveMemberByProject(productId, projectId, flagId);
                projectTaskService.copyTaskByProject(productId, projectId, flagId);
            }

            //如果是 从模板复制 那么从项目表 里面复制 复制成员
            if (SourceType.PROJECT.equals(sourceType)) {
                projectMembersService.saveMemberByTemplate(productId, projectId, flagId);
                projectTaskService.copyTaskByTemplate(productId, projectId, flagId);
            }

            //修改产品状态
            productInfoService.updateProjectStatus(productId, ProjectStateEnum.YES_START.getState());

        }

        return flag;
    }

    @Override
    public List<Map<String, Object>> listMap() {

        return baseMapper.listMap();
    }


    /**
     * 方法说明
     *
     * @param days
     * @param taskList
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @author yl
     * @date 2022-09-20 11:45
     */
    private List<Map<String, Object>> getFinishTaskTrend(int days, List<ProjectTaskEntity> taskList) {
        DateTime dateTime = new DateTime(new Date());
        List<Map<String, Object>> finishTaskTrend = new LinkedList<>();

        String fmt = DateUtil.fmt_day;
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        for (int i = days; i >= 0; i--) {
            Map<String, Object> finishTaskMap = new HashMap<>();
            Date date = dateTime.plusDays(-i).toDate();
            long count = taskList.stream().filter(t -> t.getTaskFinishTime() != null && DateUtils.isSameDay(date, t.getTaskFinishTime())).count();
            finishTaskMap.put("date", sdf.format(date.getTime()));
            finishTaskMap.put("quantity", count);
            finishTaskTrend.add(finishTaskMap);
        }

        return finishTaskTrend;
    }


    /**
     * 获取任务阶段
     *
     * @param
     * @return java.util.List<com.erp.model.plm.dto.PhaseDistributeDTO>
     * @author yl
     * @date 2022-09-19 15:20
     */
    public PhaseDistributeDTO phaseDistributeList(List<ProjectTaskEntity> taskList) {
        PhaseDistributeDTO phaseDistribute = new PhaseDistributeDTO();
        //以阶段名分组
        Map<String, List<ProjectTaskEntity>> map = taskList.parallelStream().
                collect(Collectors.groupingBy(ProjectTaskEntity::getPhaseName));

        //阶段的 集合  以阶段名作为key 以对应结果为值
        List<Map<String, List<Map<String, Object>>>> phaseList = new LinkedList<>();
        //状态列表
        List<Map<String, Object>> statusList = new LinkedList<>();
        for (Map.Entry<String, List<ProjectTaskEntity>> item : map.entrySet()) {
            Map<String, List<Map<String, Object>>> phaseMap = new HashMap<>();
            //阶段名
            String phaseName = item.getKey();
            //分类后的任务
            List<ProjectTaskEntity> groupList = item.getValue();
            List<Map<String, Object>> phaseStateList = getPhaseStateList(groupList);
            phaseMap.put(phaseName, phaseStateList);
            phaseList.add(phaseMap);
        }
        //获取枚举的所有值
        for (ProductInfoStateEnum e : ProductInfoStateEnum.values()) {
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("name", e.getName());
            statusMap.put("colourState", e.getColourState());
            statusList.add(statusMap);
        }
        phaseDistribute.setPhaseList(phaseList);
        phaseDistribute.setStatusList(statusList);


        return phaseDistribute;
    }


    /**
     * 根据分组的后的 任务 找出对应的任务状态
     *
     * @param groupList
     * @return
     * @author yl
     * @date 2022-09-20 9:16
     */
    private List<Map<String, Object>> getPhaseStateList(List<ProjectTaskEntity> groupList) {
        List<Map<String, Object>> list = new LinkedList<>();
        //待发布
        Integer toBeReleased = TaskStateEnum.TO_BE_RELEASED.getCode();
        //未启动
        Integer notStart = TaskStateEnum.NOT_START.getCode();
        //进行中
        Integer ing = TaskStateEnum.ING.getCode();
        //已完成
        Integer finish = TaskStateEnum.FINISH.getCode();

        //完成待确认
        Integer waitConfirm = TaskStateEnum.FINISH_WAIT_CONFIRM.getCode();
        //审核中
        Integer approvalIng = TaskStateEnum.APPROVAL_PASS.getCode();
        //审核不通过
        Integer noPass = TaskStateEnum.APPROVAL_NO_PASS.getCode();
        //审核通过
        Integer pass = TaskStateEnum.APPROVAL_PASS.getCode();

        int toBeReleasedValue = 0;
        int notStartValue = 0;
        int ingValue = 0;
        int approvalIngValue = 0;
        int approvalNoPassValue = 0;
        int finishValue = 0;
        int waitConfirmValue = 0;
        int approvalPassValue = 0;
        //这是 审核任务的
        for (ProjectTaskEntity item : groupList) {
            Integer status = item.getStatus();
            //待发布
            if (toBeReleased.equals(status)) {
                toBeReleasedValue++;
            }
            //未开始
            if (notStart.equals(status)) {
                notStartValue++;
            }
            if (ing.equals(status)) {
                ingValue++;
            }
            if (finish.equals(status)) {
                finishValue++;
            }
            if (waitConfirm.equals(status)) {
                waitConfirmValue++;
            }
            if (noPass.equals(status)) {
                approvalNoPassValue++;
            }
            if (approvalIng.equals(status)) {
                approvalIngValue++;
            }
            if (pass.equals(status)) {
                approvalPassValue++;
            }

        }

        Map<String, Object> WaitReleasedMap = new HashMap<>();
        WaitReleasedMap.put("name", ProductInfoStateEnum.TO_BE_RELEASED.getName());
        WaitReleasedMap.put("value", toBeReleasedValue);
        list.add(WaitReleasedMap);

        Map<String, Object> notStartMap = new HashMap<>();
        notStartMap.put("name", ProductInfoStateEnum.NOT_START.getName());
        notStartMap.put("value", notStartValue);
        list.add(notStartMap);

        Map<String, Object> ingMap = new HashMap<>();
        ingMap.put("name", ProductInfoStateEnum.ING.getName());
        ingMap.put("value", ingValue);
        list.add(ingMap);

        Map<String, Object> approvalIngMap = new HashMap<>();
        approvalIngMap.put("name", ProductInfoStateEnum.APPROVAL_ING.getName());
        approvalIngMap.put("value", approvalIngValue);
        list.add(approvalIngMap);

        Map<String, Object> approvalNoPassMap = new HashMap<>();
        approvalNoPassMap.put("name", ProductInfoStateEnum.APPROVAL_NO_PASS.getName());
        approvalNoPassMap.put("value", approvalNoPassValue);
        list.add(approvalNoPassMap);

        Map<String, Object> finishMap = new HashMap<>();
        finishMap.put("name", ProductInfoStateEnum.FINISH.getName());
        finishMap.put("value", finishValue);
        list.add(finishMap);

        Map<String, Object> waitConfirmMap = new HashMap<>();
        waitConfirmMap.put("name", ProductInfoStateEnum.WAIT_CONFIRM.getName());
        waitConfirmMap.put("value", waitConfirmValue);
        list.add(waitConfirmMap);

        Map<String, Object> approvalPassMap = new HashMap<>();
        approvalPassMap.put("name", ProductInfoStateEnum.APPROVAL_PASS.getName());
        approvalPassMap.put("value", approvalPassValue);
        list.add(approvalPassMap);

        return list.stream().filter(m -> (Integer) m.get("value") != 0).collect(Collectors.toList());
    }
}
