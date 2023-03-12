package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.BaseStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.model.plm.vo.ChangeScheduleExportVO;
import com.erp.model.plm.vo.ScheduleTaskExportErrorExcelVO;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Classname ChangeScheduleExcelListener
 * @Description TODO
 * @Date 2023-02-14 11:28
 * @Created by yl
 */
public class ChangeScheduleExcelListener extends AnalysisEventListener<ScheduleTaskExportErrorExcelVO> {

    private List<ProjectTaskEntity> projectTaskList;


    private List<ScheduleTaskExportErrorExcelVO> errorList;

    private List<ScheduleTaskExportErrorExcelVO> dataList;
    private List<ChangeScheduleExportVO> succeedList;
    private List<FindUserDTO> sysUserList;


    private String productName;


    private String productId;

    public ChangeScheduleExcelListener(List<ProjectTaskEntity> projectTaskList, String productId, List<FindUserDTO> sysUserList, String productName) {
        this.projectTaskList = projectTaskList;
        this.errorList = new ArrayList<>();
        this.succeedList = new ArrayList<>();
        this.productId = productId;
        this.dataList = new ArrayList<>();
        this.sysUserList = sysUserList;
        this.productName = productName;

    }

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
            errorMsgList.add("任务名不能为空");
        }

        ProjectTaskEntity task = null;
        if (StringUtils.isNotBlank(taskName)) {
            task=projectTaskList.stream().filter(t -> t.getName().equals(taskName) &&
                    t.getProductId().equals(productId)).findFirst().orElse(null);
        }
        if (Objects.isNull(task)) {
            errorMsgList.add("任务不存在");
        }

        if (!Objects.isNull(task) && !task.getPid().equals("0")) {
            errorMsgList.add("子任务不能排期变更");
        }
        String chargeName = vo.getChargeName();
        if (StringUtils.isBlank(chargeName)) {
            errorMsgList.add("负责人不能为空");
        }
        FindUserDTO user = null;
        if (StringUtils.isNotBlank(chargeName)) {
            user = sysUserList.stream().filter(u->chargeName.equals(u.getUserName())).
                    findFirst().orElse(null);
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
            if (!BaseStatusEnum.AUDIT_PASS.getStatus().equals(scheduleStatus)) {
                errorMsgList.add("只有审核通过任务才能排期变更");
            }
        }

        if (errorMsgList.size() > 0) {
            vo.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(vo);
            return;
        }

        ChangeScheduleExportVO changeVO = new ChangeScheduleExportVO();
        changeVO.setStatusName(TaskStateEnum.getName(task.getStatus()));
        changeVO.setStatus(task.getStatus());
        changeVO.setChangeStartTime(LocalDateTime.parse(vo.getPlanStartTime(), DateTimeFormatter.ofPattern(DateUtil.fmt_year_month)));
        changeVO.setChangeEndTime(LocalDateTime.parse(vo.getPlanEndTime(), DateTimeFormatter.ofPattern(DateUtil.fmt_year_month)));
        changeVO.setChargeName(vo.getChargeName());
        changeVO.setTaskId(task.getId());
        changeVO.setTaskName(task.getName());
        changeVO.setOriginStartTime(task.getPlanStartTime());
        changeVO.setOriginEndTime(task.getPlanEndTime());
        String taskChargeId = task.getChargeId();
        String taskChargeName = task.getChargeName();
        List<String> chargeIdList = new ArrayList<>();
        //当传过来的任务负责人不同的时候
        if (!chargeName.equals(taskChargeName)) {
            if (!Objects.isNull(user)) {
                String userId = user.getUserId();
                if (StringUtils.isNotBlank(userId)) {
                    chargeIdList = Arrays.asList(userId);
                }
            }
        } else {
            if (StringUtils.isNotBlank(taskChargeId)) {
                chargeIdList = Arrays.asList(taskChargeId.split(","));
            }
        }


        changeVO.setChargeIdList(chargeIdList);
        succeedList.add(changeVO);
    }


    /**
     * 获取到错误的信息
     *
     * @return
     */
    public List<ScheduleTaskExportErrorExcelVO> getErrorDataList() {
        return errorList;
    }


    /**
     * 获取到数据的信息
     *
     * @return
     */
    public List<ScheduleTaskExportErrorExcelVO> getDataList() {
        return dataList;
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
