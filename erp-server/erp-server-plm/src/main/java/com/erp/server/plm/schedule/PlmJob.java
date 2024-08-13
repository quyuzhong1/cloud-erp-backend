package com.erp.server.plm.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.enums.CfgSettingEnum;
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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.hutool.json.XMLTokener.entity;

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
     * 产品信息同步到其他表冗余
     */
//    @XxlJob("productInfoSyncDmp")
    public void productInfoSyncDmp() {
//        List<ProductInfoEntity> list = productInfoService.getProductInfoAll();
//        syncProductService.syncProductInfoToDmp(list);
//        scmSyncProductService.syncProductInfoToScm(list);
    }

    /**
     * 产品sku表同步到其他表冗余
     */
//    @XxlJob("productSkuSyncDmp")
    public void productSkuSyncDmp() {
//        wmsSyncProductService.syncProductInfoToWms();
//        wmsSyncProductService.syncProductSkuSaleToWms();

//        List<ProductDetailEntity> list = productDetailService.getProductDetailAll();
//        syncProductService.syncProductSkuToDmp(list);
//        wmsSyncProductService.syncProductSkuToWms(list);
//        scmSyncProductService.syncProductSkuToScm(list);
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
        if (CollectionUtil.isNotEmpty(details)){
            XxlJobHelper.log("重算目的国申报价sku数量：{}", details.size());
            productDetailService.recalDestDeclarePrice(details);
        }
        XxlJobHelper.log("recalDestDeclarePrice end : {}", LocalDateTime.now());
    }
}
