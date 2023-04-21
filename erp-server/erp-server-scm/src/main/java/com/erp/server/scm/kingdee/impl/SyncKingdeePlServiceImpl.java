package com.erp.server.scm.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.server.scm.kingdee.SyncKingdeePlService;
import com.erp.server.scm.service.DictBasicService;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import com.erp.server.scm.service.PurchaseOrderService;
import com.erp.server.scm.service.PurchaseOrderSupplierService;
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
public class SyncKingdeePlServiceImpl implements SyncKingdeePlService {

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private DictBasicService dictBasicService;


    /**
     * 组装数据发送到金蝶
     */
    @Override
    public void syncDataToKingdee(PurchaseApplicationEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());

        //查询采购供应商
        PurchaseOrderSupplierEntity supplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(entity.getId());
        if (ObjectUtils.isEmpty(supplierEntity)) {
            return;
        }
        //供应商名称
        resultMap.put("supplierName",supplierEntity.getSupplierName());
        //供应商联系人
        resultMap.put("contactName",supplierEntity.getContactName());
        //是否是新品首批
        resultMap.put("isFirstMassProduct",entity.getIsFirstMassProduct());

        if (ObjectUtils.isNotEmpty(supplierEntity.getPayMethodId())) {
            DictBasicEntity dictBasicEntity = dictBasicService.getById(supplierEntity.getPayMethodId());
            if (ObjectUtils.isNotEmpty(dictBasicEntity)) {
                //付款方式
                resultMap.put("payMethodId",dictBasicEntity.getValue());
            }
        }

        //采购明细
        List<PurchaseOrderDetailEntity> details = purchaseOrderDetailService.listByPurchaseOrderId(entity.getId());
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<JSONObject> list = new ArrayList<>();
        for (PurchaseOrderDetailEntity detailEntity : details) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("skuNo",detailEntity.getSkuNo());
            jsonObject.set("purchaseQty",detailEntity.getPurchaseQty());
            jsonObject.set("planDeliveryDate",detailEntity.getPlanDeliveryDate());
            jsonObject.set("price", MathUtil.divide(detailEntity.getTaxPrice(),MathUtil.add(MathUtil.BigDecimal_1,detailEntity.getTaxRate())) );
            jsonObject.set("taxPrice",detailEntity.getTaxPrice());
            jsonObject.set("taxRate",detailEntity.getTaxRate());
            jsonObject.set("isGift",detailEntity.getIsGift());
            jsonObject.set("detailRemark",detailEntity.getRemark());
            list.add(jsonObject);
        }
        resultMap.put("list",list);

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_PURCHASE_APPLICATION_ORDER_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return purchaseOrderService.updateSyncKingdeeStatus(Arrays.asList(entity.getId()), SyncKingdeeStatusEnum.IN_SYNC.getCode(),"");
            }
            return Boolean.TRUE;
        });
    }
}
