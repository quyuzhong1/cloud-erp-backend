package com.erp.server.plm.schedule;

import com.erp.model.plm.entity.MoldMonitorEntity;
import com.erp.server.plm.service.MoldMonitorService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;

/**
 * 模具监控统计任务
 */
@Component
@Slf4j
public class MoldMonitorGenJob {

    @Resource
    private MoldMonitorService moldMonitorService;

    /**
     * 模具监控统计任务
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
