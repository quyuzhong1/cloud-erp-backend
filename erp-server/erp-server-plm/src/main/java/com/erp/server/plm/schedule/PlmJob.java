package com.erp.server.plm.schedule;

import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.tms.feign.CfgSettingFeign;
import com.erp.server.plm.rocketmq.sync.dmp.SyncProductService;
import com.erp.server.plm.rocketmq.sync.scm.ScmSyncProductService;
import com.erp.server.plm.rocketmq.sync.wms.WmsSyncProductService;
import com.erp.server.plm.service.NoticeMessageService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProductLogisticsService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.hutool.core.collection.CollUtil.isNotEmpty;

/**
 * 来源xxljob
 * plm 定时任务
 *
 * @Classname PlmJob

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

    @Autowired
    private WmsSyncProductService wmsSyncProductService;

    @Autowired
    private ProductDetailService productDetailService;

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private ScmSyncProductService scmSyncProductService;

    @Resource
    private ProductLogisticsService productLogisticsService;

    @Resource
    private CfgSettingFeign cfgSettingFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    /**
     * 生成发送任务预警通知 每天17:00
     */
    @XxlJob("sendTaskEarlyWarning")
    public void sendEarlyWarning() {
        noticeMessageService.sendEarlyWarning();
    }

    /**
     * 新老品同步
     */
    @XxlJob("newProductToDmp")
//    @Scheduled(cron = “0 0 2 * * ?"")
//    @PostConstruct
    public void newProductToDmp() {
        syncProductService.syncNewProductToDmp();
    }

    /**
     * 计算sku目的国申报单价
     */
    @XxlJob("recalDestDeclarePrice")
    public void recalDestDeclarePrice(){
        XxlJobHelper.log("recalDestDeclarePrice start : {}", LocalDateTime.now());
        List<ProductDetailEntity> details = productDetailService.getProductDetailByDestDeclarePrice();
        if (isNotEmpty(details)){
            XxlJobHelper.log("重算目的国申报价sku数量：{}", details.size());
            productDetailService.resetDestDeclarePrice(details, Boolean.FALSE);
        }
        XxlJobHelper.log("recalDestDeclarePrice end : {}", LocalDateTime.now());
    }
}
