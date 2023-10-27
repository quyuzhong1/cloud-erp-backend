package com.erp.server.plm.schedule;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductBomHistoryEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeBomInfoService;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeProductDetailService;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.ProductBomHistoryService;
import com.erp.server.plm.service.ProductDetailService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0

 * @date 2023/1/12 15:19
 */
@Component
@Slf4j
@EnableScheduling
public class KingdeePushJob {

   @Resource
   private ProductDetailService productDetailService;

    @Resource
    private BomInfoService bomInfoService;

    @Resource
    private ProductBomHistoryService productBomHistoryService;

    @Resource
    private SyncKingdeeBomInfoService syncKingdeeBomInfoService;

    @Resource
    private SyncKingdeeProductDetailService syncKingdeeProductDetailService;

    /**
     * 推送产品信息数据
     */
    @XxlJob("kingdeePushProductDetail")
    public void kingdeePushProductDetail() {
        List<ProductDetailEntity> list = productDetailService.lambdaQuery()
                .in(ProductDetailEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(ProductDetailEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(ProductDetailEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的产品信息");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeProductDetailService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode());
            } catch (Exception e) {
                XxlJobHelper.log("SKU【{}】推送金蝶失败,error = {}",obj.getSkuNo(),e);
                log.error("SKU【{}】推送金蝶失败",obj.getSkuNo(),e);
            }
        });
    }

    /**
     * 推送bom数据
     */
    @XxlJob("kingdeePushBomInfo")
    public void kingdeePushBomInfo() {
        List<BomInfoEntity> list = bomInfoService.lambdaQuery()
                .in(BomInfoEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(BomInfoEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(BomInfoEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的bom信息");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeBomInfoService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode());
            } catch (Exception e) {
                XxlJobHelper.log("BOM【{}】推送金蝶失败,error = {}",obj.getParentSkuNo(),e);
                log.error("BOM【{}】推送金蝶失败",obj.getParentSkuNo(),e);
            }
        });
    }

    /**
     * 推送bom历史数据
     */
    @XxlJob("kingdeePushBomHistory")
    public void kingdeePushBomHistory() {
        List<ProductBomHistoryEntity> list = productBomHistoryService.lambdaQuery()
                .in(ProductBomHistoryEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(ProductBomHistoryEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(ProductBomHistoryEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的bom历史信息");
            return;
        }
        List<String> bomIdList = list.stream().map(ProductBomHistoryEntity::getBomId).distinct().collect(Collectors.toList());
        List<BomInfoEntity> bomList = bomInfoService.listByIds(bomIdList);
        if (ObjectUtils.isEmpty(bomList)) {
            log.info("kingdeePushBomHistory>>>>>>>>>>无需要同步的bom信息");
            return;
        }
        bomList.forEach(obj->{
            try {
                syncKingdeeBomInfoService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode());
            } catch (Exception e) {
                XxlJobHelper.log("BOM【{}】推送金蝶失败,error = {}",obj.getParentSkuNo(),e);
                log.error("BOM【{}】推送金蝶失败",obj.getParentSkuNo(),e);
            }
        });
    }
}
