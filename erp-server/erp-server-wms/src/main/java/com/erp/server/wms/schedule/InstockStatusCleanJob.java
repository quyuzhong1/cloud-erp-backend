package com.erp.server.wms.schedule;

import com.erp.server.wms.service.PoReturnService;
import com.erp.server.wms.service.WarehouseReceiveService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@EnableScheduling
public class InstockStatusCleanJob {
    @Resource
    private WarehouseReceiveService warehouseReceiveService;

    @XxlJob("instockStatusCleanJob")
    public ReturnT<String> instockStatusCleanJob(){
        XxlJobHelper.log("=====采购退货单自动确认 开始任务=====");
        long start = System.currentTimeMillis();
        warehouseReceiveService.instockStatusCleanJob();
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====采购退货单自动确认 结束任务=====");
        return ReturnT.SUCCESS;
    }
}
