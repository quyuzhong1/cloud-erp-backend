package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.util.ObjectUtil;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.pull.service.IReportHistoryService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.ModelService;
import com.erp.server.dmp.pull.service.dmp.PlatformApiTaskService;
import com.erp.server.dmp.pull.service.gyy.GyyHistoryDeliveryDetailServiceImpl;
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
    public void gyyDeliveryHistory() throws Exception {
        // 查询对应任务配置
        PlatformApiTaskEntity entity = platformApiTaskService.getByApiCode(PlatformApiEnum.GY_ERP_TRADE_DELIVERYS_HISTORY_GET.getTaskName());
        if(ObjectUtil.isEmpty(entity)){
            XxlJobHelper.log("{}任务task记录为空异常", PlatformApiEnum.GY_ERP_TRADE_DELIVERYS_HISTORY_GET.getTaskName());
            return;
        }
        JobTaskDTO jobTaskDTO = new JobTaskDTO(entity);
        // 执行拉取任务
        //通过枚举获取对应service
        RequestDTO requestDTO = new RequestDTO(jobTaskDTO, PlatformApiEnum.GY_ERP_TRADE_DELIVERYS_HISTORY_GET);
        historyDeliveryService.pullHistoryOrderInfo(requestDTO);
    }

    @XxlJob("GyyOrderHistory")
    public void gyyOrderHistory() throws Exception {
        // 查询对应任务配置
        PlatformApiTaskEntity entity = platformApiTaskService.getByApiCode(PlatformApiEnum.GY_ERP_TRADE_HISTORY_GET.getTaskName());
        if(ObjectUtil.isEmpty(entity)){
            XxlJobHelper.log("{}任务task记录为空异常", PlatformApiEnum.GY_ERP_TRADE_HISTORY_GET.getTaskName());
            return;
        }
        JobTaskDTO jobTaskDTO = new JobTaskDTO(entity);
        // 执行拉取任务
        //通过枚举获取对应service
        RequestDTO requestDTO = new RequestDTO(jobTaskDTO, PlatformApiEnum.GY_ERP_TRADE_HISTORY_GET);
        historyTradeService.pullHistoryOrderInfo(requestDTO);
    }
}
