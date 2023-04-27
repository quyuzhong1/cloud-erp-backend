package com.erp.server.scm.kingdee.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.kingdee.SyncKingdeePurchasePriceService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchasePriceService;
import com.erp.server.scm.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    @Resource
    private SysUserFeign sysUserFeign;

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
        //供应商编码
        resultMap.put("supplierCode",supplierEntity.getCode());
        //采购组织
        resultMap.put("purchaseOrgName",entity.getPurchaseOrgName());

        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getPricingUserId());

        if (ObjectUtils.isNotEmpty(findUserDTO)) {
            //定价员
            resultMap.put("pricingUserCode",findUserDTO.getCode());
        }

        //价目明细
        List<PurchasePriceDetailDTO.ViewDTO> details = purchasePriceDetailService.getByPurchasePriceId(entity.getId());
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<JSONObject> list = new ArrayList<>();
        for (PurchasePriceDetailDTO.ViewDTO detailEntity : details) {
            BigDecimal rate = MathUtil.divide(detailEntity.getTaxRate(), MathUtil.BigDecimal_100);
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("detailId",detailEntity.getId());
            jsonObject.set("skuNo",detailEntity.getSkuNo());
            jsonObject.set("taxRate",detailEntity.getTaxRate());
            jsonObject.set("price", MathUtil.divide(detailEntity.getTaxPrice(),MathUtil.add(MathUtil.BigDecimal_1,rate)) );
            jsonObject.set("taxPrice",detailEntity.getTaxPrice());
            jsonObject.set("minQty",detailEntity.getMinQty());
            jsonObject.set("maxQty",detailEntity.getMaxQty());
            jsonObject.set("effectiveDate",detailEntity.getEffectiveDate());
            jsonObject.set("expireDate",detailEntity.getExpireDate());
            jsonObject.set("disabled",detailEntity.getDisabled());
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

    /**
     * 组装数据发送到金蝶
     */
    @Override
    public void syncDataDetailToKingdee(List<PurchasePriceDetailEntity> details, Boolean disabled) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<String> purchasePriceIds = details.stream().map(PurchasePriceDetailEntity::getPurchasePriceId).distinct().collect(Collectors.toList());
        List<PurchasePriceEntity> list = purchasePriceService.listByIds(purchasePriceIds);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, Object> resultMap = new HashMap<>();

        JSONArray jsonArray = new JSONArray();

        for (PurchasePriceDetailEntity entity : details) {
            JSONObject jsonObject = new JSONObject();
            String syncKingdeeId = list.stream().filter( obj -> obj.getId().equals(entity.getPurchasePriceId())).map(PurchasePriceEntity::getSyncKingdeeId).findFirst().orElse(null);
            if (StringUtils.isBlank(syncKingdeeId)) {
                continue;
            }
            jsonObject.set("syncKingdeeId",syncKingdeeId);
            jsonObject.set("skuNo",entity.getSkuNo());
            jsonArray.put(jsonObject);
        }
        String operate;
        if (disabled) {
            operate = SyncKingdeeOperateEnum.OPERATE_SUB_UN_EFFECTIVE.getCode();
        } else {
            operate = SyncKingdeeOperateEnum.OPERATE_SUB_EFFECTIVE.getCode();
        }

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        resultMap.put("list",jsonArray);
        //异步推送mq
        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_PURCHASE_PRICE_TAG.getName(), resultMap, String.join(",",purchasePriceIds));

    }

}
