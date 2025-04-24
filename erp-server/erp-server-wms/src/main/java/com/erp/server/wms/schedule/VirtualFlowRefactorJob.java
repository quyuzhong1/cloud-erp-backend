package com.erp.server.wms.schedule;

import com.erp.server.wms.service.VirtualFlowRefactorService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 重新生成流水
 * @author will
 * @date 2025/3/31 10:07
 */
@Component
@Slf4j
public class VirtualFlowRefactorJob {

    @Resource
    private VirtualFlowRefactorService virtualFlowRefactorService;

    /**
     * 重新生成流水
     * @author will
     * @date 2025/3/31 10:08
     */
    @XxlJob("virtualFlowRefactorJob")
    public ReturnT<String> VirtualFlowRefactorJob() {
        XxlJobHelper.log("VirtualFlowRefactorJob 执行开始");
        String jobParam = XxlJobHelper.getJobParam();
        virtualFlowRefactorService.rebuildFlow(jobParam);
        return ReturnT.SUCCESS;
    }
}
