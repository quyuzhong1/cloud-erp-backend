package com.erp.server.msg.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncKingdeeOmsStatusEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.DmpTaskMsgDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.msg.config.MsgContext;
import com.erp.server.msg.constant.MongoTableConstant;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SKU自动匹配JOB
 */
@Component
@Slf4j
@EnableScheduling
public class FeiShuMsgJob {
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private MsgContext msgContext;
    @Resource
    private DmpTaskFeign dmpTaskFeign;

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
        List<DmpTaskMsgDTO> warnTaskReport = dmpTaskFeign.getWarnTaskReport(statusList);
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

    @XxlJob("sendFeiShuWarnPushMsg")
    public void sendFeiShuWarnPushMsg(){
        String jobParam = XxlJobHelper.getJobParam();
        String[] params = jobParam.split(",");
        List<String> statusList = new ArrayList<>();
        statusList.add(params[0]);
        statusList.add(params[1]);
        int size = Integer.parseInt(params[2]);
        int waitMillis = Integer.parseInt(params[3]);
        List<WarnMsgInfoDTO> list = getPushTask(statusList);
        XxlJobHelper.log("待发送Push飞书预警消息数量:{}", list.size());
        //对全量数据进行分区
        sendMsg(size, waitMillis, list);
    }

    @XxlJob("sendFeiShuWarnPullMsg")
    public void sendFeiShuWarnPullMsg(){
        String jobParam = XxlJobHelper.getJobParam();
        String[] params = jobParam.split(",");
        List<String> statusList = new ArrayList<>();
        statusList.add(params[0]);
        statusList.add(params[1]);
        int size = Integer.parseInt(params[2]);
        int waitMillis = Integer.parseInt(params[3]);
        List<WarnMsgInfoDTO> list = getPullTask(statusList);
        XxlJobHelper.log("待发送Pull飞书预警消息数量:{}", list.size());
        //对全量数据进行分区
        sendMsg(size, waitMillis, list);
    }

    private void sendMsg(int size, int waitMillis, List<WarnMsgInfoDTO> list) {
        if (list.size() > size){
            List<List<WarnMsgInfoDTO>> partition = ListUtil.partition(list, MathUtil.NUMBER_100);
            partition.forEach(e -> sendWarnMsgByTask(e, waitMillis));
        }else {
            sendWarnMsgByTask(list,waitMillis);
        }
        XxlJobHelper.log("发送飞书预警消息:end");
    }

    private void sendWarnMsgByTask(List<WarnMsgInfoDTO> list, int waitMillis){
        XxlJobHelper.log("批量发送飞书预警消息数量:{}", list.size());
        if (CollUtil.isEmpty(list)){
            return;
        }
        //批量发送异常提醒，并更新mongo数据记录状态
        list.forEach(msgContext::routeSendWarnMsg);
        try {
            XxlJobHelper.log("发送飞书预警消息休眠 start:{}", System.currentTimeMillis());
            //增加休眠，避免飞书请求限制
            Thread.sleep(waitMillis);
            XxlJobHelper.log("发送飞书预警消息休眠 end:{}", System.currentTimeMillis());
        } catch (InterruptedException e) {
            log.error("FeiShuMsgJob.sendWarnMsg：休眠异常");
            Thread.currentThread().interrupt();
        }
        XxlJobHelper.log("批量发送飞书预警消息完成:{}", list.size());
    }
    public  List<WarnMsgInfoDTO> getPushTask(List<String> statusList){
        List<DmpPushTaskEntity> warnPushTaskList = dmpTaskFeign.getWarnPushTaskList(statusList);
        List<WarnMsgInfoDTO> warnMsgInfoDTOS = new ArrayList<>(warnPushTaskList.size());
        for (DmpPushTaskEntity entity: warnPushTaskList ){
            WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
            warnMsgInfo.setBizName(SourceTypeEnum.getName(entity.getSourceType()));
            warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
            warnMsgInfo.setTitle(CharSequenceUtil.format("单据【{}】从{}推送至{}失败",entity.getSourceCode(),entity.getSourcePlatformName(),entity.getTargetPlatformName()));
            warnMsgInfo.setTableName(SourceTypeEnum.getTableName(entity.getSourceType()));
            warnMsgInfo.setTableId(entity.getSourceId());
            warnMsgInfo.setKeyInfo(entity.getReturnMsg());
            warnMsgInfo.setHappenTime(entity.getUpdateTime());
            warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
            warnMsgInfoDTOS.add(warnMsgInfo);
        }
        return warnMsgInfoDTOS;
    }

    public List<WarnMsgInfoDTO> getPullTask(List<String> statusList){
        List<DmpPullTaskEntity> warnPullTaskList = dmpTaskFeign.getWarnPullTaskList(statusList);
        List<WarnMsgInfoDTO> warnMsgInfoDTOS = new ArrayList<>(warnPullTaskList.size());
        for (DmpPullTaskEntity entity : warnPullTaskList){
            WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
            warnMsgInfo.setBizName(SourceTypeEnum.getName(entity.getSourceType()));
            warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
            warnMsgInfo.setTitle(CharSequenceUtil.format("单据【{}】从{}拉取至{}失败",entity.getSourceCode(),entity.getSourcePlatformName(),entity.getTargetPlatformName()));
            warnMsgInfo.setTableName(SourceTypeEnum.getTableName(entity.getSourceType()));
            warnMsgInfo.setTableId(entity.getSourceId());
            warnMsgInfo.setKeyInfo(entity.getReturnMsg());
            warnMsgInfo.setHappenTime(entity.getUpdateTime());
            warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
            warnMsgInfoDTOS.add(warnMsgInfo);
        }
        return warnMsgInfoDTOS;
    }
}
