package com.erp.server.plm.schedule;

import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.MoldMonitorEntity;
import com.erp.server.plm.service.CfgMoldAlertRuleService;
import com.erp.server.plm.service.CfgMoldReturnAlertRuleService;
import com.erp.server.plm.service.MoldMonitorRefOrderService;
import com.erp.server.plm.service.MoldMonitorService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 任务节点记录表补偿重试
 */
@Component
@Slf4j
public class MoldMonitorGenJob {

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private MoldMonitorService moldMonitorService;

    @Resource
    private CfgMoldAlertRuleService cfgMoldAlertRuleService;

    @Resource
    private CfgMoldReturnAlertRuleService cfgMoldReturnAlertRuleService;

    @Resource
    private MoldMonitorRefOrderService moldMonitorRefOrderService;

    /**
     * 任务节点记录表补偿重试
     * @Author jack
     **/
    @XxlJob("MoldMonitorGenJob")
    public ReturnT<String> MoldMonitorGenJob() {
        XxlJobHelper.log("MoldMonitorGenJob 执行开始");

        List<MoldMonitorEntity> moldMonitorEntities = moldMonitorService.buildMonitor();

        moldMonitorService.calMonitorOrder(moldMonitorEntities);

        XxlJobHelper.log("MoldMonitorGenJob 执行任务列表结束");
        return ReturnT.SUCCESS;
    }
}
