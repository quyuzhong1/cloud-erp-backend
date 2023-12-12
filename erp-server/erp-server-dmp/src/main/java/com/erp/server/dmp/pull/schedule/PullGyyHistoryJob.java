package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.constant.TaskConstant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportHistoryService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class PullGyyHistoryJob {

    @Resource
    private PlatformApiTaskService platformApiTaskService;
    @Resource
    @Qualifier("gyyHistoryDeliveryDetailServiceImpl")
    private IReportHistoryService historyDeliveryService;

    @Resource
    @Qualifier("gyyHistoryOrderInfoServiceImpl")
    private IReportHistoryService historyTradeService;

    @XxlJob("GyyDeliveryHistory")
    public ReturnT<String> gyyDeliveryHistory() throws Exception {
        XxlJobHelper.log("GyyDeliveryHistory 任务开始执行！");
        // 查询对应任务配置
        PlatformApiTaskEntity entity = platformApiTaskService.getByApiCode(PlatformApiEnum.GY_ERP_TRADE_DELIVERYS_HISTORY_GET.getTaskName());
        if(ObjectUtil.isEmpty(entity)){
            XxlJobHelper.log("{}任务task记录为空异常", PlatformApiEnum.GY_ERP_TRADE_DELIVERYS_HISTORY_GET.getTaskName());
            return ReturnT.SUCCESS;
        }
        JobTaskDTO jobTaskDTO = getJobTaskDTO(entity, TaskConstant.GYY_PULL_DATA_TASK);
        // 执行拉取任务
        //通过枚举获取对应service
        RequestDTO requestDTO = new RequestDTO(jobTaskDTO, PlatformApiEnum.GY_ERP_TRADE_DELIVERYS_HISTORY_GET);
        historyDeliveryService.pullHistoryOrderInfo(requestDTO);
        XxlJobHelper.log("GyyDeliveryHistory 任务执行结束！");
        return ReturnT.SUCCESS;
    }

    public static JobTaskDTO getJobTaskDTO(PlatformApiTaskEntity entity, String taskName) {
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setId(entity.getId());
        jobTaskDTO.setDictPlatform(entity.getDictPlatform());
        jobTaskDTO.setIntervalTime(entity.getIntervalTime());
        jobTaskDTO.setLastTime(entity.getLastTime());
        jobTaskDTO.setNextTime(entity.getNextTime());
        jobTaskDTO.setStatus(entity.getStatus());
        jobTaskDTO.setCreateTime(entity.getCreateTime());
        jobTaskDTO.setApiCode(entity.getApiCode());
        jobTaskDTO.setApiName(entity.getApiName());
        jobTaskDTO.setRetryTimes(entity.getRetryTimes());
        jobTaskDTO.setShopName(entity.getShopName());
        jobTaskDTO.setShopId(entity.getShopId());
        jobTaskDTO.setBillType(entity.getBillType());
        jobTaskDTO.setOperateType(entity.getOperateType());
        jobTaskDTO.setApiParam(entity.getApiParam());
        jobTaskDTO.setPlatformApiId(entity.getPlatformApiId());
        jobTaskDTO.setPlatformCategory(entity.getPlatformCategory());
        return jobTaskDTO;
    }

    @XxlJob("GyyOrderHistory")
    public ReturnT<String> gyyOrderHistory() throws Exception {
        XxlJobHelper.log("GyyOrderHistory 任务开始执行！");
        // 查询对应任务配置
        PlatformApiTaskEntity entity = platformApiTaskService.getByApiCode(PlatformApiEnum.GY_ERP_TRADE_HISTORY_GET.getTaskName());
        if(ObjectUtil.isEmpty(entity)){
            XxlJobHelper.log("{}任务task记录为空异常", PlatformApiEnum.GY_ERP_TRADE_HISTORY_GET.getTaskName());
            return ReturnT.SUCCESS;
        }
        JobTaskDTO jobTaskDTO = getJobTaskDTO(entity, TaskConstant.GYY_PULL_DATA_TASK);
        // 执行拉取任务
        //通过枚举获取对应service
        RequestDTO requestDTO = new RequestDTO(jobTaskDTO, PlatformApiEnum.GY_ERP_TRADE_HISTORY_GET);
        historyTradeService.pullHistoryOrderInfo(requestDTO);
        XxlJobHelper.log("GyyOrderHistory 任务执行结束！");
        return ReturnT.SUCCESS;
    }

}
