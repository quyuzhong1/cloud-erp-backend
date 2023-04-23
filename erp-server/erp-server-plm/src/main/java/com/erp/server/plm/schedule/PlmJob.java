package com.erp.server.plm.schedule;

import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.plm.rocketmq.sync.dmp.SyncPlmProductService;
import com.erp.server.plm.service.NoticeMessageService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductInfoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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
    private SyncPlmProductService syncPlmProductService;

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
        syncPlmProductService.syncProductInfoToDmp();
    }

    /**
     * 产品sku表同步到中台
     */
    @XxlJob("productSkuSyncDmp")
    public void productSkuSyncDmp() {
        syncPlmProductService.syncProductSkuToDmp();
    }
}
