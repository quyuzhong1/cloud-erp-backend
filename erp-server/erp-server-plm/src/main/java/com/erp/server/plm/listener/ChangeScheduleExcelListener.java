package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.date.DateUtil;
import com.erp.common.business.enums.BaseStatusEnum;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.model.plm.vo.ChangeScheduleExportVO;
import com.erp.model.plm.vo.ScheduleTaskExportExcelVO;
import com.erp.server.plm.service.ProjectTaskService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Classname ChangeScheduleExcelListener
 * @Description TODO
 * @Date 2023-02-14 11:28
 * @Created by yl
 */
public class ChangeScheduleExcelListener extends AnalysisEventListener<ScheduleTaskExportExcelVO> {

    private ProjectTaskService projectTaskService;


    private List<ScheduleTaskExportExcelVO> errorList;


    private List<ChangeScheduleExportVO> succeedList;


    private String productId;

    public ChangeScheduleExcelListener(ProjectTaskService projectTaskService, String productId) {
        this.projectTaskService = projectTaskService;
        this.errorList = new ArrayList<>();
        this.succeedList = new ArrayList<>();
        this.productId = productId;

    }

    @Override
    public void invoke(ScheduleTaskExportExcelVO vo, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        if (StringUtils.isBlank(vo.getTaskName())) {
            errorMsgList.add("任务名 不能为空");
        }
        if (StringUtils.isBlank(vo.getChargeName())) {
            errorMsgList.add("负责人不能为空");
        }
        ProjectTaskEntity task = projectTaskService.getbyName(productId, vo.getTaskName().trim());
        if (Objects.isNull(task)) {
            errorMsgList.add("任务不存在");
        }

        if (StringUtils.isBlank(vo.getPlanStartTime())) {
            errorMsgList.add("计划开始时间 不能为空");
        }
        if (StringUtils.isBlank(vo.getPlanEndTime())) {
            errorMsgList.add("计划结束时间 不能为空");
        }
        if (StringUtils.isNotBlank(vo.getPlanStartTime()) && StringUtils.isNotBlank(vo.getPlanEndTime())) {
            if (vo.getPlanEndTime().compareTo(vo.getPlanStartTime()) < 0) {
                errorMsgList.add("结束时间必须大于开始时间");
            }
        }

        if (task != null) {
            String scheduleStatus = task.getScheduleStatus();
            if (!BaseStatusEnum.AUDIT_PASS.getStatus().equals(scheduleStatus)) {
                errorMsgList.add("只有审核通过任务才能排期变更");
            }
        }

        String errStr = "";
        if (errorMsgList.size() > 0) {
            for (int i = 0; i < errorMsgList.size(); i++) {
                Integer indexTemp = i + 1;
                errStr = errStr + indexTemp + "、" + errorMsgList.get(i) + "；";
            }
            vo.setErrorMsg(errStr);
            errorList.add(vo);
            return;
        }

        ChangeScheduleExportVO changeVO = new ChangeScheduleExportVO();
        changeVO.setStatusName(TaskStateEnum.getName(task.getStatus()));
        changeVO.setStatus(task.getStatus());
        changeVO.setChangeStartTime(DateUtil.strToDate(vo.getPlanStartTime(), DateUtil.fmt));
        changeVO.setChangeEndTime(DateUtil.strToDate(vo.getPlanEndTime(), DateUtil.fmt));
        changeVO.setChargeName(vo.getChargeName());
        changeVO.setTaskId(task.getId());
        changeVO.setTaskName(task.getName());
        changeVO.setOriginStartTime(task.getPlanStartTime());
        changeVO.setOriginEndTime(task.getPlanEndTime());
        String taskChargeId = task.getChargeId();
        if (StringUtils.isNotBlank(taskChargeId)) {
            changeVO.setChargeIdList(Arrays.asList(taskChargeId.split(",")));
        } else {
            changeVO.setChargeIdList(new ArrayList<>(1));
        }

        succeedList.add(changeVO);
    }


    /**
     * 获取到错误的信息
     *
     * @return
     */
    public List<ScheduleTaskExportExcelVO> getErrorDateList() {
        return errorList;
    }

    /**
     * 获取到成功的信息
     *
     * @return
     */
    public List<ChangeScheduleExportVO> getSucceedDateList() {
        return succeedList;
    }

    /**
     * 最后完成的
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-02-14 11:32
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
