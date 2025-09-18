package com.erp.server.msg.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.*;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.DmpTaskMsgDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.WorkflowTaskRecordFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.msg.config.MsgContext;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * SKU自动匹配JOB
 */
@Component
@Slf4j
@EnableScheduling
public class FeiShuMsgJob {
    @Resource
    private MsgContext msgContext;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private LogisticsFeign logisticsFeign;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private WorkflowTaskRecordFeign workflowTaskRecordFeign;

    /**
     * 飞书预警消息汇总报告
     */
    @XxlJob("sendFeiShuWarnMsgReport")
    public void sendFeiShuWarnMsg() {
        XxlJobHelper.log("飞书预警消息汇总报告:start");
        String jobParam = XxlJobHelper.getJobParam();
        List<String> statusList = new ArrayList<>();
        if (CharSequenceUtil.isNotBlank(jobParam)){
            String[] params = jobParam.split(",");
            statusList.add(params[0]);
            statusList.add(params[1]);
        }else {
            statusList.add(SyncStatusEnum.IN_SYNC.getCode());
            statusList.add(SyncStatusEnum.FAILED_SYNC.getCode());
        }
        //获取汇总消息
        List<DmpTaskMsgDTO> warnTaskReport = null;
        try {
            warnTaskReport = dmpTaskFeign.getWarnTaskReport(statusList);
        }catch (Exception e){
            log.error("飞书预警消息汇总报告:error", e);
            XxlJobHelper.log(e);
        }
        if (CollUtil.isNotEmpty(warnTaskReport)){
            WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
            warnMsgInfo.setBizName("预警消息");
            warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
            warnMsgInfo.setTitle("异常预警消息汇总");
            warnMsgInfo.setTableName("dmp_pull_task/dmp_push_task");
            warnMsgInfo.setTableId("");
            warnMsgInfo.setHappenTime(LocalDateTime.now());
            warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
            List<String> keyInfoList = new ArrayList<>(warnTaskReport.size());
            warnTaskReport.forEach(dmpTaskMsgDTO -> {
                String format = CharSequenceUtil.format("【{}】->【{}】失败,业务【{}】,数量:{}", dmpTaskMsgDTO.getSourcePlatformName(), dmpTaskMsgDTO.getTargetPlatformName(),SourceTypeEnum.getName(dmpTaskMsgDTO.getSourceType()), dmpTaskMsgDTO.getTotal());
                keyInfoList.add(format);
            });
            warnMsgInfo.setKeyInfo(String.join("\n", keyInfoList));
            msgContext.routeSendWarnMsg(warnMsgInfo);
        }
        XxlJobHelper.log("飞书预警消息汇总报告:end");
    }

    /**
     * 飞书预警消息渠道汇总报告
     */
    @XxlJob("sendFeiShuWarnMsgReportByChannel")
    public void sendFeiShuWarnMsgReportByChannel() {
        XxlJobHelper.log("飞书预警消息渠道汇总报告:start");
        String jobParam = XxlJobHelper.getJobParam();
        int day;
        if (CharSequenceUtil.isNotBlank(jobParam)){
            day = Integer.parseInt(jobParam);
        } else {
            day = 3;
        }
        LocalDateTime updateTime = LocalDateTime.now().minusDays(day);
        LocalDateTime trackTime = LocalDateTime.now().minusMonths(3);
        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(LogisticsPlatformEnum.TRACK123.getCode())
                .registerStatus(1)
                .trackEnable(true)
                .trackTime(trackTime)
                .updateTime(updateTime)
                .transportType(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode())
                .build();
        //获取汇总消息
        List<LogisticsChannelDTO.WarnReportDTO> warnReportByChannel = null;
        try {
            warnReportByChannel = logisticsFeign.getWarnReportByChannel(query);
        }catch (Exception e){
            log.error("飞书预警消息渠道汇总报告:error", e);
            XxlJobHelper.log(e);
        }
        if (CollectionUtil.isNotEmpty(warnReportByChannel)){
            WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
            warnMsgInfo.setBizName("预警消息");
            warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_TMS);
            warnMsgInfo.setTitle("物流轨迹更新预警汇总");
            warnMsgInfo.setTableName("logistics_channel/logistics_bill_detail");
            warnMsgInfo.setTableId("");
            warnMsgInfo.setHappenTime(LocalDateTime.now());
            warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
            List<String> keyInfoList = new ArrayList<>(warnReportByChannel.size());
            warnReportByChannel.forEach(warnReportDTO -> {
                String format = StrUtil.format("渠道【{}】在【{}】天内未更新轨迹信息,数量:{}", warnReportDTO.getChannelName(),day, warnReportDTO.getTotal());
                keyInfoList.add(format);
            });
            warnMsgInfo.setKeyInfo(String.join("\n", keyInfoList));
            msgContext.routeSendWarnMsg(warnMsgInfo);
        }
        XxlJobHelper.log("物流轨迹更新预警汇总报告:end");
    }
    /**
     * B2C销售订单异常订单汇总提醒
     */
    @XxlJob("sendFeiShuWarnMsgReportBySoB2C")
    public void sendFeiShuWarnMsgReportBySoB2C() {
        //OMS B2C销售订单异常订单汇总提醒
        sendB2CError();

        //OMS workflow_task_record 任务节点记录表异常汇总提醒
        sendWorkflowTaskRecordError();
    }

    //OMS B2C销售订单异常订单汇总提醒
    private void sendB2CError() {
        XxlJobHelper.log("====B2C销售订单异常订单汇总提醒:start====");
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("{}:请求参数：{}",LocalDateTime.now(), jobParam);
        List<String> typeList = CharSequenceUtil.isNotBlank(jobParam) ? Arrays.asList(jobParam.split(",")) : Collections.emptyList();
        List<SoB2cErrorDTO.TypeCountDTO> list = null;
        try {
            list = soB2cFeign.getB2CErrorReport(typeList);
        }catch (Exception e){
            log.error("B2C销售订单异常订单汇总提醒:error", e);
            XxlJobHelper.log(e);
        }
        if (CollUtil.isNotEmpty(list)){
            WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
            warnMsgInfo.setBizName("预警消息");
            warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
            warnMsgInfo.setTitle("B2C销售订单异常订单汇总提醒");
            warnMsgInfo.setTableName("so_b2c_error");
            warnMsgInfo.setTableId("");
            warnMsgInfo.setHappenTime(LocalDateTime.now());
            warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
            List<String> keyInfoList = new ArrayList<>(list.size());
            list.forEach(typeCountDTO -> {
                String format = StrUtil.format("异常类型【{}】存在数量:{}", CharSequenceUtil.isBlank(typeCountDTO.getTypeName()) ? typeCountDTO.getType() : typeCountDTO.getTypeName(),typeCountDTO.getTypeCount());
                keyInfoList.add(format);
            });
            warnMsgInfo.setKeyInfo(String.join("\n", keyInfoList));
            msgContext.routeSendWarnMsg(warnMsgInfo);
        }
        XxlJobHelper.log("====B2C销售订单异常订单汇总提醒:end====");
    }

    //OMS workflow_task_record 任务节点记录表异常汇总提醒
    private void sendWorkflowTaskRecordError() {
        XxlJobHelper.log("====workflowTaskRecord任务异常订单汇总提醒:start====");
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("{}:请求参数：{}",LocalDateTime.now(), jobParam);
        List<WorkflowTaskRecordDTO.TaskErrorReportDTO> taskErrorReport = new ArrayList<>();
        try {
            taskErrorReport = workflowTaskRecordFeign.getTaskErrorReport();
        }catch (Exception e){
            log.error("workflowTaskRecord任务异常订单汇总提醒:error", e);
            XxlJobHelper.log(e);
        }
        if(CollUtil.isNotEmpty(taskErrorReport)){
            WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
            warnMsgInfo.setBizName("预警消息");
            warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
            warnMsgInfo.setTitle("任务异常订单汇总");
            warnMsgInfo.setTableName("workflow_task_record");
            warnMsgInfo.setTableId("");
            warnMsgInfo.setHappenTime(LocalDateTime.now());
            warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
            List<String> keyInfoList = new ArrayList<>(taskErrorReport.size());
            taskErrorReport.forEach(typeCountDTO -> {
                String format = StrUtil.format("异常单据类型【{}】任务节点【{}】存在数量:{}", SourceTypeEnum.getName(typeCountDTO.getSourceType()) , typeCountDTO.getDictBasicName() , typeCountDTO.getErrorCount());
                keyInfoList.add(format);
            });
            warnMsgInfo.setKeyInfo(String.join("\n", keyInfoList));
            msgContext.routeSendWarnMsg(warnMsgInfo);
        }
        XxlJobHelper.log("====workflowTaskRecord任务异常订单汇总提醒:end====");
    }

}
