package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.BaseStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.vo.ScheduleTaskExportErrorExcelVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.service.ProjectPlanService;
import com.erp.server.plm.service.ProjectTaskService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
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
public class ProjectPlanTaskExcelListener extends AnalysisEventListener<ScheduleTaskExportErrorExcelVO> {


    private ProjectTaskService projectTaskService;


    private ProjectPlanService projectPlanService;


    private List<ScheduleTaskExportErrorExcelVO> list;


    private List<String> taskIdList;

    private List<ScheduleTaskExportErrorExcelVO> dataList;

    private SysUserFeign sysUserFeign;

    private String productId;

    private String productName;

    public ProjectPlanTaskExcelListener(ProjectTaskService projectTaskService, ProjectPlanService projectPlanService, String productId, SysUserFeign sysUserFeign, String productName) {
        this.projectTaskService = projectTaskService;
        this.projectPlanService = projectPlanService;
        this.list = new ArrayList<>();
        this.taskIdList = new ArrayList<>();
        this.sysUserFeign = sysUserFeign;
        this.productId = productId;
        this.dataList = new ArrayList<>();
        this.productName = productName;
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
    public void invoke(ScheduleTaskExportErrorExcelVO vo, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        dataList.add(vo);

        //注解验证信息
        List<String> msgList = FieldValidUtil.fieldValid(vo);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        if (StringUtils.isBlank(vo.getProductName())) {
            errorMsgList.add("产品名称不能为空");
        }
        if (!productName.equals(vo.getProductName())) {
            errorMsgList.add("产品名称有误");
        }
        String taskName = vo.getTaskName();
        if (StringUtils.isBlank(taskName)) {
            errorMsgList.add("任务名称不能为空");
        }
        ProjectTaskEntity task = projectTaskService.getbyName(productId, vo.getTaskName());
        if (Objects.isNull(task)) {
            errorMsgList.add("任务不存在");
        }
        FindUserDTO user = null;
        String chargeName = vo.getChargeName();
        if (StringUtils.isBlank(vo.getChargeName())) {
            errorMsgList.add("负责人不能为空");
        }
        if (StringUtils.isNotBlank(chargeName)) {
            user = sysUserFeign.getUserByUserName(chargeName);
            if (Objects.isNull(user) || StringUtils.isBlank(user.getUserId())) {
                errorMsgList.add("负责人不存在");
            }
        }

        if (StringUtils.isBlank(vo.getPlanStartTime())) {
            errorMsgList.add("计划开始时间 不能为空");
        }
        if (StringUtils.isBlank(vo.getPlanEndTime())) {
            errorMsgList.add("计划结束时间 不能为空");
        }
        if (StringUtils.isNotBlank(vo.getPlanStartTime()) && StringUtils.isNotBlank(vo.getPlanEndTime())) {
            Date startTime = DateUtil.strToDate(vo.getPlanStartTime(), DateUtil.fmt_year_month);
            Date endTime = DateUtil.strToDate(vo.getPlanEndTime(), DateUtil.fmt_year_month);
            if (startTime != null && endTime != null) {
                if (endTime.compareTo(startTime) < 0) {
                    errorMsgList.add("结束时间必须大于开始时间");
                }
            }

        }
        if (task != null) {
            String scheduleStatus = task.getScheduleStatus();
            List<String> statusList = new ArrayList<>();
            statusList.add(BaseStatusEnum.WAIT_SUBMIT.getStatus());
            statusList.add(BaseStatusEnum.AUDIT_NO_PASS.getStatus());
            if (!statusList.contains(scheduleStatus)) {
                errorMsgList.add("只有待提交和审核不通过的任务才能排期");
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

        String dbChargeName = task.getChargeName();
        //当两个名字不一样 就要更改
        if (!chargeName.equals(dbChargeName)) {
            if (user != null) {
                if (StringUtils.isNotBlank(user.getUserId())) {
                    task.setChargeName(chargeName);
                    task.setChargeId(user.getUserId());
                }
            }
        }

        taskIdList.add(task.getId());
        productId = task.getProductId();
        task.setPlanStartTime(DateUtil.strToDate(vo.getPlanStartTime(), DateUtil.fmt_year_month));
        task.setPlanEndTime(DateUtil.strToDate(vo.getPlanEndTime(), DateUtil.fmt_year_month));
        projectTaskService.updateById(task);
    }

    public List<ScheduleTaskExportErrorExcelVO> getErrorList() {
        return list;
    }

    public List<ScheduleTaskExportErrorExcelVO> getDataList() {
        return dataList;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
//        //提交排期
//        HandleTaskScheduleDTO dto = new HandleTaskScheduleDTO();
//        dto.setProductId(productId);
//        dto.setTaskIdList(taskIdList);
//        projectPlanService.submitSchedule(dto);
    }
}
