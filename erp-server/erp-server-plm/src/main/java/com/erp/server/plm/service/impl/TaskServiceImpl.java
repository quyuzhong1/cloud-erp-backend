package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.TaskDTO;
import com.erp.model.plm.dto.TaskPagingDTO;
import com.erp.model.plm.dto.TaskPagingShowDTO;
import com.erp.model.plm.entity.PreTaskEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskDeliveryDocsEntity;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.model.workflow.dto.TaskShowDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.ProjectTaskMapper;
import com.erp.server.plm.service.PreTaskService;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.TaskDeliveryService;
import com.erp.server.plm.service.TaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname TaskServiceImpl
 * @Date 2023-06-20 19:50
 * @Created by yl
 */
@Service
public class TaskServiceImpl extends ServiceImpl<ProjectTaskMapper, ProjectTaskEntity> implements TaskService {


    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private PreTaskService preTaskService;

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private TaskDeliveryService taskDeliveryService;


    /**
     * 执行的任务列表分页查询 可执行表示没有前置任务 或者 任务没有完成和取消
     *
     * @param searchParamDTO
     * @return com.common.business.vo.PagingVO<java.util.List < com.erp.model.plm.dto.TaskPagingShowDTO>>
     * @author yl
     * @date 2023-06-21 9:04
     */
    @Override
    public PagingVO<List<TaskPagingShowDTO>> allExecutablePaging(PagingDTO<TaskDTO.TaskPagingParamDTO> searchParamDTO) {
        //当前登录的用户id
        String loginUserId = UserContext.getDefaultLoginUser().getUid();
        TaskDTO.TaskPagingParamDTO params = searchParamDTO.getParams();
        params.setPermissionSql(searchParamDTO.getPermissionSql());
        Page query = new Page(searchParamDTO.getCurrPage(), searchParamDTO.getPageSize());
        //分组的标示
        String groupNameFlag = params.getGroupNameFlag();
        //是否分组
        Boolean ifGroup = false;
        //表示不分组
        if (StringUtils.isNotBlank(groupNameFlag) && !"no".equals(groupNameFlag)) {
            ifGroup = true;
        }

        return null;
    }


    /**
     * 导出任务列表
     *
     * @param params
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-25 12:10
     */
    @Override
    public Boolean exportTask(TaskPagingDTO.ExportDTO params, HttpServletResponse response) {
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        String userId = loginUser.getUid();
        Integer taskFlag = params.getTaskFlag();
        List<Integer> statusList = params.getStatusList();
        List<TaskDTO.TaskExportDTO> resultList = new ArrayList<>();
        //这个是我完成的任务
        if (TaskConstant.MY_FINISH_TASK.equals(taskFlag)) {
            statusList.add(TaskStateEnum.PORTION_FINISH.getCode());
            resultList = baseMapper.waitMyFinishExport(params, userId);
        }
        //这个待我审核的任务
        if (TaskConstant.MY_APPROVAL_TASK.equals(taskFlag)) {
            statusList.add(TaskStateEnum.APPROVAL_ING.getCode());
            List<TaskShowDTO> myToDoList = workflowFeign.queryMyToDo(userId);
            //获取流程集合
            List<String> processIds = myToDoList.stream().map(TaskShowDTO::getProcessInstanceId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(processIds)) {
                resultList = baseMapper.myApprovaExport(params, processIds);
            }
        }
        //这个是全部
        if (TaskConstant.ALL_FINISH_TASK.equals(taskFlag)) {
            resultList = baseMapper.allExport(params);
        }
        if (TaskConstant.CHANGE_TASK.equals(taskFlag)) {
            resultList = baseMapper.changeExport(params);
        }
        if (CollectionUtils.isEmpty(resultList)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //获取到任务id 集合
        List<String> taskIds = resultList.stream().map(TaskDTO.TaskExportDTO::getTaskId).collect(Collectors.toList());

        List<PreTaskEntity> preTaskList = preTaskService.getPreTaskListBytaskIds(taskIds);
        List<TaskDeliveryDocsEntity> deliveryDocsList = taskDeliveryService.getByProductId(params.getProductId());

        //前置任务
        List<ProjectTaskEntity> preTaskEntityList = projectTaskService.getByTaskIds(preTaskList.stream().map(PreTaskEntity::getPreTaskId).collect(Collectors.toList()));
        for (TaskDTO.TaskExportDTO item : resultList) {
            String taskId = item.getTaskId();
            Integer type = item.getType();
            String typeName = "一般任务";
            if (type.equals(TaskConstant.REVIEW_TASK)) {
                typeName = "评审任务";
            }
            item.setTypeName(typeName);
            Integer priority = item.getPriority();
            String priorityName = "低";
            if (priority.equals(TaskConstant.INTERMEDIATE_TASK)) {
                priorityName = "中";
            }
            if (priority.equals(TaskConstant.ADVANCED_TASK)) {
                priorityName = "高";
            }
            item.setPriorityName(priorityName);
            Integer isMilepost = item.getIsMilepost();
            String isMilepostStr = "是";
            if (0 == isMilepost) {
                isMilepostStr = "否";
            }
            item.setIsMilepostStr(isMilepostStr);
            List<String> preTaskIds = preTaskList.stream().filter(p -> p.getTaskId().equals(item.getTaskId())).map(PreTaskEntity::getPreTaskId).collect(Collectors.toList());
            String preTaskName = preTaskEntityList.stream().filter(t -> preTaskIds.contains(t.getId())).map(ProjectTaskEntity::getName).collect(Collectors.joining(","));
            item.setPreTaskName(preTaskName);
            String docsName = deliveryDocsList.stream().filter(f -> taskId.equals(f.getTaskId())).map(TaskDeliveryDocsEntity::getDocsName).distinct().collect(Collectors.joining(","));
            item.setDocsName(docsName);

        }
        String name = "产品任务列表";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/productTaskInfo.xlsx";
        try {
            new ExcelPrintUtils().patchExport(resultList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("产品任务列表导出出错 >>>>>{}", e);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }
}
