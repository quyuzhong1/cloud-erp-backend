package com.erp.server.plm.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
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
    @XxlJob("productInfoSyncDmp")
    public void productInfoSyncDmp() {
        List<ProductInfoEntity> list = productInfoService.getProductInfoAll();
        syncProductService.syncProductInfoToDmp(list);
        scmSyncProductService.syncProductInfoToScm(list);
    }

    /**
     * 产品sku表同步到其他表冗余
     */
    @XxlJob("productSkuSyncDmp")
    public void productSkuSyncDmp() {
        wmsSyncProductService.syncProductInfoToWms();
        wmsSyncProductService.syncProductSkuSaleToWms();

        List<ProductDetailEntity> list = productDetailService.getProductDetailAll();
        syncProductService.syncProductSkuToDmp(list);
        wmsSyncProductService.syncProductSkuToWms(list);
        scmSyncProductService.syncProductSkuToScm(list);
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
            recalDestDeclarePriceList(details);
        }
        XxlJobHelper.log("recalDestDeclarePrice end : {}", LocalDateTime.now());
    }

    /**
     * 批量计算目的国申报价
     */
    private void recalDestDeclarePriceList(List<ProductDetailEntity> details){
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<String> skuIds = details.stream().filter(Objects::nonNull).map(ProductDetailEntity::getId).collect(Collectors.toList());
        List<String> skuNoList = details.stream().filter(Objects::nonNull).map(ProductDetailEntity::getSkuNo).collect(Collectors.toList());
        List<ProductLogisticsEntity> productLogisticsList = productLogisticsService.listBySkuIdList(skuIds);
        if (Objects.isNull(productLogisticsList)) {
            return;
        }
        //系统配置
        CfgSettingEntity setting = cfgSettingFeign.getByKey(CfgSettingEnum.LOGISTICS_PRODUCT_DEST_DECLARE_PRICE.getCode());
        if (Objects.isNull(setting) || Objects.isNull(setting.getDataJson()) || CollectionUtils.isEmpty(setting.getDataJson().getJSONArray("data"))) {
            return;
        }
        //sku信息
        List<DmpSkuCostEntity> skuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList);
        BigDecimal usdRate = dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), CurrencyEnum.USD.getCurrencyCode());
        if (Objects.isNull(usdRate)) {
            return;
        }
        List<ProductLogisticsEntity> updateList = new ArrayList<>();
        for(ProductLogisticsEntity productLogistics : productLogisticsList){
            XxlJobHelper.log("recalDestDeclarePrice : skuId:{},skuNo:{}", productLogistics.getSkuId(),productLogistics.getSkuNo());
            //是否重算目的国申报价
            BigDecimal destDeclarePrice = productLogistics.getDestDeclarePrice();
            if (Objects.isNull(destDeclarePrice) || destDeclarePrice.compareTo(BigDecimal.ZERO) == 0) {
                DmpSkuCostEntity skuCostDTO = skuCostList.stream().filter(e -> e.getSkuId().equals(productLogistics.getSkuId())).findFirst().orElse(null);
                if (Objects.isNull(skuCostDTO)) {
                    continue;
                }
                //含税成本 默认是人民币
                BigDecimal actualTaxCost = BigDecimal.ZERO;
                if (Objects.nonNull(skuCostDTO.getCostPrice())) {
                    actualTaxCost = skuCostDTO.getCostPrice();
                }
                //统一换算成美元汇率
                BigDecimal actualTaxCostUsd = MathUtil.divide(actualTaxCost, usdRate);
                List<CfgSettingValueDTO.LogisticsProductDestDeclarePrice> data = JSONUtil.toList(setting.getDataJson().getJSONArray("data"), CfgSettingValueDTO.LogisticsProductDestDeclarePrice.class);
                //根据美元计算比例
                BigDecimal finalActualTaxCostUsd = actualTaxCostUsd;
                CfgSettingValueDTO.LogisticsProductDestDeclarePrice declarePrice = data.stream().filter(e -> e.getStartPrice().compareTo(finalActualTaxCostUsd) < 0 && e.getEndPrice().compareTo(finalActualTaxCostUsd) >= 0).findFirst().orElse(null);
                if (Objects.isNull(declarePrice) || Objects.isNull(declarePrice.getRate())) {
                    continue;
                }
                BigDecimal resultDestDeclarePrice = actualTaxCostUsd.multiply(declarePrice.getRate()).divide(MathUtil.BigDecimal_100, 4, RoundingMode.HALF_UP);
                productLogistics.setDestDeclarePrice(resultDestDeclarePrice);
                productLogistics.setDestCurrency(CurrencyEnum.USD.getCurrencyCode());
                productLogistics.setDestCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());
                updateList.add(productLogistics);
                log.info("更新目的国申报价 sku:{},目的国申报价：{}",skuCostDTO.getSkuNo(), resultDestDeclarePrice);
            }
        }
        if (CollectionUtils.isNotEmpty(updateList)){
            productLogisticsService.updateBatchById(updateList);
        }

    }
}
