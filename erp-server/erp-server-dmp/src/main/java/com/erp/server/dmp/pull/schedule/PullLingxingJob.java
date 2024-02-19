package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.business.constant.TaskConstant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.enums.PlatformDictEnum;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 领星相关任务
 *
 * @Author Jim
 * @Date 2024/02/18
 **/

@Component
@Slf4j
public class PullLingxingJob {

    @Resource
    private PullErpDateThread pullErpDateThread;
    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private PlatformApiTaskService platformApiTaskService;
    @Resource
    private DmpErrorLogService dmpErrorLogService;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    @Qualifier("lxShopInfoServiceImpl")
    private IReportSaveService lxShopInfoService;


    @XxlJob("lingxingExecute")
    public ReturnT<String> lingxingExecute(){
        threadPoolTaskExecutor.execute(() -> {
            pullErpDateThread.executeTask(TaskConstant.LX_PULL_DATA_TASK);
        });
        return ReturnT.SUCCESS;
    }

    /**
     * 同步领星店铺ID
     */
    @XxlJob("syncLingxingSid")
    public ReturnT<String> syncLingxingSid(){
        XxlJobHelper.log("syncLingxingSid 同步领星店铺ID 任务开始执行！");
        // 查询上次执行时间
        PlatformApiTaskEntity entity = platformApiTaskService.getByApiCode(PlatformApiEnum.LX_ERP_SHOP_LIST_GET.getTaskName());
        if(ObjectUtil.isEmpty(entity)){
            XxlJobHelper.log("{}任务task记录为空异常", PlatformApiEnum.LX_ERP_SHOP_LIST_GET.getTaskName());
            return ReturnT.SUCCESS;
        }
        RequestDTO requestDTO = new RequestDTO(PullGyyHistoryJob.getJobTaskDTO(entity, TaskConstant.LX_PULL_DATA_TASK), PlatformApiEnum.LX_ERP_SHOP_LIST_GET);
        try {
            // 保存或更新领星店铺ID
            lxShopInfoService.pullDataSave(requestDTO);
            // 修改执行更新时间
            // 修改执行结果信息
            Boolean aBoolean = platformApiTaskService.updateTaskStateById(requestDTO.getJobTaskDTO(), 3);
            if (!aBoolean) {
                throw new RuntimeException("修改同步领星店铺ID任务下次执行时间失败！");
            }
        }catch (Exception e) {
            XxlJobHelper.log(" 同步领星店铺ID错误dto={} e= {}", JSONUtil.toJsonStr(requestDTO), e);
            String message = e.getMessage();
            DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity(requestDTO.getJobTaskDTO().getId(), JSONUtil.toJsonStr(requestDTO),message, JSONUtil.toJsonStr(e.getStackTrace()));
            dmpErrorLogService.save(dmpErrorLogEntity);
            // 发送下载异常消息
            sendErrorMsgToDark(e, dmpErrorLogEntity);
        }

        XxlJobHelper.log("yncLingxingSid 同步领星店铺ID 任务执行结束！");
        return ReturnT.SUCCESS;
    }

    private void sendErrorMsgToDark(Exception e, DmpErrorLogEntity dmpErrorLogEntity) {
        WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
        warnMsgInfoDTO.setTitle("同步领星店铺ID异常");
        warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfoDTO.setBizName("同步领星店铺ID");
        warnMsgInfoDTO.setTableName(SqlHelper.table(DmpErrorLogEntity.class).getTableName());
        warnMsgInfoDTO.setTableId(dmpErrorLogEntity.getId());
        warnMsgInfoDTO.setKeyInfo(e.getMessage());
        mqProducerService.sendWarnMsg(warnMsgInfoDTO);
    }

}
