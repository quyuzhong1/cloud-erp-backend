package com.erp.server.plm.schedule;

import com.erp.server.plm.rocketmq.sync.dmp.SyncProductService;
import com.erp.server.plm.service.NoticeMessageService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 来源xxljob
 * plm 定时任务
 *
 * @Classname PlmJob
 * @Description TODO
 * @Date 2022-11-16 10:25
 * @Created by yl
 */
@Component
@Slf4j
public class PlmJob {

    @Autowired
    private NoticeMessageService noticeMessageService;

    @Autowired
    private SyncProductService syncProductService;


    /**
     * 生成发送任务预警通知 每天17:00
     */
    @XxlJob("sendTaskEarlyWarning")
    public void sendEarlyWarning() {
        noticeMessageService.sendEarlyWarning();
    }

    /**
     * 产品信息同步到中台
     */
    @XxlJob("productInfoSyncDmp")
    public void productInfoSyncDmp() {
        syncProductService.syncProductInfoToDmp();
    }

    /**
     * 产品sku表同步到中台
     */
    @XxlJob("productSkuSyncDmp")
    public void productSkuSyncDmp() {
        syncProductService.syncProductSkuToDmp();
    }

    /**
     * 新老品同步
     */
//    @XxlJob("newProductToDmp")
//    @Scheduled(cron = “0 0 2 * * ?"")
    @PostConstruct
    public void newProductToDmp() {
        syncProductService.syncNewProductToDmp();
    }
}
