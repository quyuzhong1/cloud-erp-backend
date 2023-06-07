package com.erp.server.plm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.TaskPagingShowDTO;
import com.erp.model.plm.dto.TaskSearchParamDTO;
import com.erp.model.plm.enums.TaskSearchCategoryEnum;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.model.workflow.dto.TaskShowDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.WorkOptionMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.WorkOptionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作台服务类
 * @Author Luo_WG
 * @Date 2023/4/21 15:33
 **/
@Service
public class WorkOptionServiceImpl implements WorkOptionService {

    @Resource
    private WorkOptionMapper workOptionMapper;

    @Resource
    private ProjectTaskService projectTaskService;

    @Resource
    private CommonService commonService;

    @Resource
    private WorkflowFeign workflowFeign;

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @Override
    public Integer getTableNum(WorkOptionDTO.TableNumDTO tableNumDTO) {
        //assignExecutable
        //assignNotStarted
        //waitAuditExecutable
        //waitAuditNotStarted
        if (tableNumDTO.getTableName().equals("project_task")) {
            PagingDTO pagingDTO = JSONObject.parseObject(tableNumDTO.getModuleParam(), PagingDTO.class);
            TaskSearchParamDTO params = JSONObject.parseObject(JSONObject.toJSONString(pagingDTO.getParams()), TaskSearchParamDTO.class);
/*            pagingDTO.setPageSize(99999);
            pagingDTO.setParams(params);
            PagingVO<List<TaskPagingShowDTO>> listPagingVO = projectTaskService.assignToMePaging(pagingDTO);*/
            if (tableNumDTO.getApproveStatus().equals("assignNotStarted") || tableNumDTO.getApproveStatus().equals("assignExecutable")) {
                return getWaitFinishCount(params);
            } else {
                return getWaitAuditCount(params);
            }
        }
        if (tableNumDTO.getTableName().equals("product_detail")) {
            Integer status = Integer.valueOf(tableNumDTO.getApproveStatus());
            return workOptionMapper.getProductDetailNum(tableNumDTO, status);
        }
        if (tableNumDTO.getTableName().equals("product_bom_info")) {
            Integer status = Integer.valueOf(tableNumDTO.getApproveStatus());
            return workOptionMapper.getProductBomInfoNum(tableNumDTO, status);
        }
        if (tableNumDTO.getTableName().equals("product_change")) {
            Integer status = Integer.valueOf(tableNumDTO.getApproveStatus());
            return workOptionMapper.getProductChangeNum(tableNumDTO, status);
        }
        return 0;
    }

    /**
     * 根据用户获取各任务阶段数量
     * @Author Luo_WG
     * @Date 2023/4/24 9:34
     * @param optionUserId optionUserId
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.StageViewDTO>
     **/
    @Override
    public List<WorkOptionDTO.StageViewDTO> stageView(String optionUserId) {
        return workOptionMapper.stageView(optionUserId);
    }


    private Integer getWaitAuditCount(TaskSearchParamDTO searchParamDTO) {
        LoginUser userInfo = commonService.getUserInfo();
        searchParamDTO.setPermissionSql(searchParamDTO.getPermissionSql());
        //"assignToMe", "myCreate", "all"
        String taskProperty = TaskConstant.ASSIGN_TO_ME;
        //任务条件 1 待完成  2 全部  3 待审核
        Integer taskCondition = searchParamDTO.getTaskCondition();
        //不在的 任务状态
        List<Integer> notStateList = getAssignToMeNoExistState(taskProperty, taskCondition);
        List<TaskShowDTO> workflowList = workflowFeign.queryMyToDo(userInfo.getUid());
        List<String> processInstanceIds = workflowList.stream().map(TaskShowDTO::getProcessInstanceId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(processInstanceIds)) {
            return 0;
        }
        searchParamDTO.setGroupFlag("");
        searchParamDTO.setSearchCategory(TaskSearchCategoryEnum.TOMEWAITAUDITPRODUCTTASKLIST.getCode());
        searchParamDTO.setProcessInstanceIds(processInstanceIds);
        List<TaskPagingShowDTO> taskPagingShowDTOS = workOptionMapper.listProductTaskBySearchCategory(notStateList, searchParamDTO);
        return taskPagingShowDTOS.size();
    }

    private Integer getWaitFinishCount(TaskSearchParamDTO params) {
        //"assignToMe", "myCreate", "all"
        String taskProperty = TaskConstant.ASSIGN_TO_ME;
        //任务条件 1 待完成  2 全部  3 待审核
        Integer taskCondition = params.getTaskCondition();
        //不在的 任务状态
        List<Integer> notStateList = getAssignToMeNoExistState(taskProperty, taskCondition);
        params.setGroupFlag("");
        params.setSearchCategory(TaskSearchCategoryEnum.TOMEPRODUCTTASKLIST.getCode());
        List<TaskPagingShowDTO> taskPagingShowDTOS = workOptionMapper.listProductTaskBySearchCategory(notStateList, params);
        return taskPagingShowDTOS.size();
    }

    /**
     * 获取分配给我的 不在状态
     *
     * @param taskCondition
     * @param taskProperty  "assignToMe", "myCreate", "all"
     * @return java.util.List<java.lang.Integer>
     * @author yl  1 待完成  2 全部
     * @date 2022-11-09 9:47
     */
    public List<Integer> getAssignToMeNoExistState(String taskProperty, Integer taskCondition) {
        List<Integer> noExistStateList = new ArrayList<>();
        //待完成  未开始，进行中，审核不通过
        if (TaskConstant.WAIT_HANDLE.equals(taskCondition)) {
            noExistStateList.add(TaskStateEnum.CLOSE.getCode());
            noExistStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            noExistStateList.add(TaskStateEnum.FINISH.getCode());
            noExistStateList.add(TaskStateEnum.APPROVAL_PASS.getCode());
            noExistStateList.add(TaskStateEnum.WAIT_CONFIRM.getCode());
            noExistStateList.add(TaskStateEnum.APPROVAL_ING.getCode());
            noExistStateList.add(TaskStateEnum.PORTION_FINISH.getCode());
        }
        // 待审核 待审核，审核中
        if (TaskConstant.WAIT_AUDIT.equals(taskCondition)) {
            noExistStateList.add(TaskStateEnum.CLOSE.getCode());
            noExistStateList.add(TaskStateEnum.ING.getCode());
            noExistStateList.add(TaskStateEnum.NOT_START.getCode());
            noExistStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            noExistStateList.add(TaskStateEnum.FINISH.getCode());
            noExistStateList.add(TaskStateEnum.APPROVAL_PASS.getCode());
            noExistStateList.add(TaskStateEnum.APPROVAL_NO_PASS.getCode());
            noExistStateList.add(TaskStateEnum.PORTION_FINISH.getCode());


        }
        //全部
        if (TaskConstant.ALL_TASK.equals(taskCondition)) {
            //分配给我   状态包含所有状态-除了待发布
            if (TaskConstant.ASSIGN_TO_ME.equals(taskProperty)) {
                noExistStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            }
        }
        return noExistStateList;
    }
}
