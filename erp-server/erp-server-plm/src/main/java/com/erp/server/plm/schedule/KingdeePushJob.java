package com.erp.server.plm.schedule;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeBomInfoService;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeProductDetailService;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.ProductDetailService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
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
    private SyncKingdeeBomInfoService syncKingdeeBomInfoService;

    @Resource
    private SyncKingdeeProductDetailService syncKingdeeProductDetailService;

    /**
     * 推送产品信息数据
     */
    @XxlJob("kingdeePushProductDetail")
    public void kingdeePushProductDetail() {
        List<ProductDetailEntity> list = productDetailService.lambdaQuery()
                .in(ProductDetailEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的产品信息");
            return;
        }
        list.forEach(obj->{
            syncKingdeeProductDetailService.syncDataToKingdee(obj);
        });

    }

    /**
     * 推送bom数据
     */
    @XxlJob("kingdeePushBomInfo")
    public void kingdeePushBomInfo() {
        List<BomInfoEntity> list = bomInfoService.lambdaQuery()
                .in(BomInfoEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的bom信息");
            return;
        }
        list.forEach(obj->{
            syncKingdeeBomInfoService.syncDataToKingdee(obj);
        });
    }
}
