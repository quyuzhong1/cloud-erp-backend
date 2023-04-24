package com.erp.server.scm.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.server.scm.kingdee.SyncKingdeePurchasePriceService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchasePriceService;
import com.erp.server.scm.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeePurchasePriceServiceImpl implements SyncKingdeePurchasePriceService {

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;



    /**
     * 组装数据发送到金蝶
     */
    @Override
    public void syncDataToKingdee(PurchasePriceEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //名称
        resultMap.put("name",entity.getCode());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());

        //查询供应商
        SupplierEntity supplierEntity = supplierService.getById(entity.getSupplierId());
        if (ObjectUtils.isEmpty(supplierEntity)) {
            return;
        }
        //供应商名称
        resultMap.put("supplierName",supplierEntity.getName());
        //采购组织
        resultMap.put("purchaseOrgName",entity.getPurchaseOrgName());
        //定价员
        resultMap.put("pricingUserName",entity.getPricingUserName());

        //价目明细
        List<PurchasePriceDetailDTO.ViewDTO> details = purchasePriceDetailService.getByPurchasePriceId(entity.getId());
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<JSONObject> list = new ArrayList<>();
        for (PurchasePriceDetailDTO.ViewDTO detailEntity : details) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("skuNo",detailEntity.getSkuNo());
            jsonObject.set("price", MathUtil.divide(detailEntity.getTaxPrice(),MathUtil.add(MathUtil.BigDecimal_1,detailEntity.getTaxRate())) );
            jsonObject.set("taxPrice",detailEntity.getTaxPrice());
            jsonObject.set("taxRate",MathUtil.multiply(detailEntity.getTaxRate(),MathUtil.BigDecimal_100));
            jsonObject.set("minQty",detailEntity.getMinQty());
            jsonObject.set("maxQty",detailEntity.getMaxQty());
            jsonObject.set("effectiveDate",detailEntity.getEffectiveDate());
            jsonObject.set("expireDate",detailEntity.getExpireDate());
            list.add(jsonObject);
        }
        resultMap.put("list",list);

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_PURCHASE_PRICE_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return purchasePriceService.updateSyncKingdeeStatus(Arrays.asList(entity.getId()), SyncKingdeeStatusEnum.IN_SYNC.getCode(),"");
            }
            return Boolean.TRUE;
        });
    }
}
