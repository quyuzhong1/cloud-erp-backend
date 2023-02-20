package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.enums.BaseStatusEnum;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.HandleTaskScheduleDTO;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.vo.ScheduleTaskExportExcelVO;
import com.erp.server.plm.service.ProjectPlanService;
import com.erp.server.plm.service.ProjectTaskService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 导入排期任务监听
 *
 * @Classname
 * @Description TODO
 * @Date 2023-02-10 14:31
 * @Created by yl
 */
public class ProjectPlanTaskExcelListener extends AnalysisEventListener<ScheduleTaskExportExcelVO> {


    private ProjectTaskService projectTaskService;


    private ProjectPlanService projectPlanService;


    private List<ScheduleTaskExportExcelVO> list;


    private List<String> taskIdList;

    private String productId;

    public ProjectPlanTaskExcelListener(ProjectTaskService projectTaskService, ProjectPlanService projectPlanService, String productId) {
        this.projectTaskService = projectTaskService;
        this.projectPlanService = projectPlanService;
        this.list = new ArrayList<>();
        this.taskIdList = new ArrayList<>();
        this.productId = productId;
    }

    /**
     * 获取任务
     *
     * @param vo
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-02-10 14:33
     */
    @Override
    public void invoke(ScheduleTaskExportExcelVO vo, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        if (StringUtils.isBlank(vo.getTaskName())) {
            errorMsgList.add("任务名 不能为空");
        }
        if (StringUtils.isBlank(vo.getChargeName())) {
            errorMsgList.add("负责人不能为空");
        }
        ProjectTaskEntity task=projectTaskService.getbyName(productId,vo.getTaskName().trim());
        if(Objects.isNull(task)){
            errorMsgList.add("任务不存在");
        }
        if (StringUtils.isBlank(vo.getPlanStartTime())) {
            errorMsgList.add("计划开始时间 不能为空");
        }
        if (StringUtils.isBlank(vo.getPlanEndTime())) {
            errorMsgList.add("计划结束时间 不能为空");
        }
        if (StringUtils.isNotBlank(vo.getPlanStartTime()) && StringUtils.isNotBlank(vo.getPlanEndTime())) {
            if (vo.getPlanEndTime().compareTo(vo.getPlanStartTime())<0) {
                errorMsgList.add("结束时间必须大于开始时间");
            }
        }
        if (task != null) {
            String scheduleStatus = task.getScheduleStatus();
            List<String> statusList = new ArrayList<>();
            statusList.add(BaseStatusEnum.WAIT_SUBMIT.getStatus());
            statusList.add(BaseStatusEnum.CANCEL.getStatus());
            if (!statusList.contains(scheduleStatus)) {
                errorMsgList.add("只有待提交和取消的任务才能排期");
            }
        }
        String errStr = "";
        if (errorMsgList.size() > 0) {
            for (int i = 0; i < errorMsgList.size(); i++) {
                Integer indexTemp = i + 1;
                errStr = errStr + indexTemp + "、" + errorMsgList.get(i) + "；";
            }
            vo.setErrorMsg(errStr);
            list.add(vo);
            return;
        }
        taskIdList.add(task.getId());
        productId = task.getProductId();
        task.setPlanStartTime(DateUtil.strToDate(vo.getPlanStartTime(),DateUtil.fmt));
        task.setPlanEndTime(DateUtil.strToDate(vo.getPlanEndTime(),DateUtil.fmt));
        projectTaskService.updateById(task);
    }

    public List<ScheduleTaskExportExcelVO> getDateList() {
        return list;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //提交排期
        HandleTaskScheduleDTO dto = new HandleTaskScheduleDTO();
        dto.setProductId(productId);
        dto.setTaskIdList(taskIdList);
        projectPlanService.submitSchedule(dto);
    }
}
