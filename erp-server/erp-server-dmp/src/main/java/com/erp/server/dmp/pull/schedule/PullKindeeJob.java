package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.business.constant.TaskConstant;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.server.dmp.pull.thread.PullErpDateThread;
import com.erp.server.dmp.service.DmpErrorLogService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@EnableScheduling
public class PullKindeeJob {

    @Resource
    private PullErpDateThread pullErpDateThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private PlatformApiTaskService platformApiTaskService;
    @Resource
    @Qualifier("kingdeeTransferDirectServiceImpl")
    private IReportSaveService kingdeeTransferDirectService;
    @Resource
    private DmpErrorLogService dmpErrorLogService;
    @Resource
    private MQProducerService mqProducerService;

    // 拉取金蝶数据任务
    //@Scheduled(cron = "*/5 * * * * ?")
    @XxlJob("kindeeExecute")
    public void execute() {
        threadPoolTaskExecutor.execute(() ->{
            pullErpDateThread.executeTask(TaskConstant.KINGDEE_PULL_DATA_TASK);
        });
    }

    @XxlJob("kindeeCleanExecute")
    public void kindeeCleanExecute() {
        String jobParam = XxlJobHelper.getJobParam();
        log.info("金蝶云清洗任务参数：{}", jobParam);
        List<String> taskList = new ArrayList<>();
        if (StrUtil.isNotBlank(jobParam)) {
            taskList = Arrays.asList(jobParam.split(","));
        }
        pullErpDateThread.executeCleanTask(TaskConstant.KINGDEE_PULL_DATA_TASK, taskList);
    }

    /**
     * 金蝶直接调拨单下载
     */
    @XxlJob("kingdeeDirectTransferDownload")
    public ReturnT<String> directTransferDownload(){
        XxlJobHelper.log("kingdeeDirectTransferDownload 任务开始执行！");
        // 查询直接调拨订单上次执行时间
        PlatformApiTaskEntity entity = platformApiTaskService.getByApiCode(PlatformApiEnum.STK_TRANSFERDIRECT.getTaskName());
        if(ObjectUtil.isEmpty(entity)){
            XxlJobHelper.log("{}任务task记录为空异常", PlatformApiEnum.STK_TRANSFERDIRECT.getTaskName());
            return ReturnT.SUCCESS;
        }
        RequestDTO requestDTO = new RequestDTO(PullGyyHistoryJob.getJobTaskDTO(entity, TaskConstant.KINGDEE_PULL_DATA_TASK), PlatformApiEnum.STK_TRANSFERDIRECT);
        try {
            // 处理直接调拨订单
            kingdeeTransferDirectService.pullDataSave(requestDTO);
            // 修改订单执行更新时间
            // 修改任务执行结果信息
            Boolean aBoolean = platformApiTaskService.updateTaskStateById(requestDTO.getJobTaskDTO(), 3);
            if (!aBoolean) {
                throw new RuntimeException("修改金蝶直接调拨单任务下次执行时间失败！");
            }
        }catch (Exception e) {
            XxlJobHelper.log(" 金蝶直接调拨订单数据错误dto={} e= {}", JSONUtil.toJsonStr(requestDTO), e);
            String message = e.getMessage();
            DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity(requestDTO.getJobTaskDTO().getId(), JSONUtil.toJsonStr(requestDTO),message, JSONUtil.toJsonStr(e.getStackTrace()));
            dmpErrorLogService.save(dmpErrorLogEntity);
            // 发送下载异常消息
            sendErrorMsgToDark(e, dmpErrorLogEntity);
        }

        XxlJobHelper.log("kingdeeDirectTransferDownload 任务执行结束！");
        return ReturnT.SUCCESS;
    }

    /**
     * 金蝶汇率下载
     */
    @XxlJob("kingdeeExchangeRateDownload")
    public ReturnT<String> kingdeeExchangeRateDownload(){
        XxlJobHelper.log("kingdeeExchangeRateDownload 任务开始执行！");
        // 查询直接调拨订单上次执行时间
        PlatformApiTaskEntity entity = platformApiTaskService.getByApiCode(PlatformApiEnum.BD_RATE.getTaskName());
        if(ObjectUtil.isEmpty(entity)){
            XxlJobHelper.log("{}任务task记录为空异常", PlatformApiEnum.BD_RATE.getTaskName());
            return ReturnT.SUCCESS;
        }
        RequestDTO requestDTO = new RequestDTO(PullGyyHistoryJob.getJobTaskDTO(entity, TaskConstant.KINGDEE_PULL_DATA_TASK), PlatformApiEnum.BD_RATE);
        try {
            // 处理汇率
            kingdeeTransferDirectService.pullDataSave(requestDTO);
            // 修改订单执行更新时间
            // 修改任务执行结果信息
            Boolean aBoolean = platformApiTaskService.updateTaskStateById(requestDTO.getJobTaskDTO(), 3);
            if (!aBoolean) {
                throw new RuntimeException("修改金蝶直接调拨单任务下次执行时间失败！");
            }
        }catch (Exception e) {
            XxlJobHelper.log(" 金蝶汇款挂你数据错误dto={} e= {}", JSONUtil.toJsonStr(requestDTO), e);
            String message = e.getMessage();
            DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity(requestDTO.getJobTaskDTO().getId(), JSONUtil.toJsonStr(requestDTO),message, JSONUtil.toJsonStr(e.getStackTrace()));
            dmpErrorLogService.save(dmpErrorLogEntity);
            // 发送下载异常消息
            sendErrorMsgToDark(e, dmpErrorLogEntity);
        }

        XxlJobHelper.log("kingdeeExchangeRateDownload 任务执行结束！");
        return ReturnT.SUCCESS;
    }


    private void sendErrorMsgToDark(Exception e, DmpErrorLogEntity dmpErrorLogEntity) {
        WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
        warnMsgInfoDTO.setTitle("金蝶直接调拨单下载异常");
        warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfoDTO.setBizName("金蝶直接调拨单下载并推送马帮其他入库单");
        warnMsgInfoDTO.setTableName(SqlHelper.table(DmpErrorLogEntity.class).getTableName());
        warnMsgInfoDTO.setTableId(dmpErrorLogEntity.getId());
        warnMsgInfoDTO.setKeyInfo(e.getMessage());
        mqProducerService.sendWarnMsg(warnMsgInfoDTO);
    }
}
