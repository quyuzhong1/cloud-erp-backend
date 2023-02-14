package com.erp.server.plm.listener;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.vo.ChangeScheduleExportVO;
import com.erp.server.plm.service.ProjectTaskService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Classname ChangeScheduleExcelListener
 * @Description TODO
 * @Date 2023-02-14 11:28
 * @Created by yl
 */
public class ChangeScheduleExcelListener extends AnalysisEventListener<ChangeScheduleExportVO> {

    private ProjectTaskService projectTaskService;


    private List<ChangeScheduleExportVO> errorList;


    private List<ChangeScheduleExportVO> succeedList;

    public ChangeScheduleExcelListener(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
        this.errorList = new ArrayList<>();
        this.succeedList = new ArrayList<>();

    }

    @Override
    public void invoke(ChangeScheduleExportVO vo, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        if (StringUtils.isBlank(vo.getChargeName())) {
            errorMsgList.add("负责人不能为空");
        }
        if (StringUtils.isBlank(vo.getTaskId())) {
            errorMsgList.add("任务id 不能为空");
        }
        if (vo.getChangeStartTime() == null) {
            errorMsgList.add("计划开始时间 不能为空");
        }
        if (vo.getChangeEndTime() == null) {
            errorMsgList.add("计划结束时间 不能为空");
        }
        if (vo.getChangeStartTime() != null && vo.getChangeEndTime() != null) {
            if (DateUtil.compare(vo.getChangeStartTime(),vo.getChangeEndTime()) > 0) {
                errorMsgList.add("开始时间不可大于结束时间");
            }
        }
        ProjectTaskEntity taskEntity = projectTaskService.getById(vo.getTaskId());
        if (Objects.isNull(taskEntity)) {
            errorMsgList.add("任务不存在");
        }
//        if (taskEntity != null) {
//            String scheduleStatus = taskEntity.getScheduleStatus();
//            if (!BaseStatusEnum.AUDIT_PASS.getStatus().equals(scheduleStatus)) {
//                errorMsgList.add("只有审核通过任务才能排期变更");
//            }
//        }

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
        succeedList.add(vo);
    }


    /**
     * 获取到错误的信息
     * @return
     */
    public List<ChangeScheduleExportVO> getErrorDateList() {
        return errorList;
    }

    /**
     * 获取到成功的信息
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
