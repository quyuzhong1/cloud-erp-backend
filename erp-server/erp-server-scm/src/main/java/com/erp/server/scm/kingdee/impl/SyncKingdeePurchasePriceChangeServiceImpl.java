package com.erp.server.scm.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PushSyncStatusDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.scm.entity.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.kingdee.SyncKingdeePurchasePriceChangeService;
import com.erp.server.scm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeePurchasePriceChangeServiceImpl implements SyncKingdeePurchasePriceChangeService {

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

    @Resource
    private PurchasePriceChangeDetailService purchasePriceChangeDetailService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    public void syncDataToKingdee(PurchasePriceChangeEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //更新同步状态为待同步
        PushSyncStatusDTO.KingdeeDTO kingdeeDTO = new PushSyncStatusDTO.KingdeeDTO(entity.getId(),operate,"", SyncStatusEnum.TO_BE_SYNC.getCode());
        purchasePriceChangeService.updateSyncKingdeeStatus(kingdeeDTO);

        //如果上游单据未发送成功则无需发送
        PurchasePriceEntity purchasePriceEntity = purchasePriceService.getById(entity.getPurchasePriceId());
        //采购价目主表数据
        if (ObjectUtils.isEmpty(purchasePriceEntity)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }

        if (!SyncStatusEnum.SUCCESS_SYNC.getCode().equals(purchasePriceEntity.getSyncKingdeeStatus()) && !SyncStatusEnum.NO_NEED_SYNC.getCode().equals(purchasePriceEntity.getSyncKingdeeStatus())) {
            log.error("采购价目未推送成功，不支持推送采购调价，采购价目单号【{}】",purchasePriceEntity.getCode());
            return;
        }

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //名称
        resultMap.put("name",entity.getCode());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());

        //调价原因
        resultMap.put("reason",entity.getReason());
        //调价日期
        resultMap.put("adjustDate",entity.getAdjustDate());

        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getPurchaseOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            //采购组织
            String orgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("purchaseOrgCode", orgCode);
        }

        //查询供应商
        SupplierEntity supplierEntity = supplierService.getById(entity.getSupplierId());
        if (ObjectUtils.isEmpty(supplierEntity)) {
            return;
        }

        //调价明细
        List<PurchasePriceChangeDetailEntity> details = purchasePriceChangeDetailService.listByPurchasePriceChangeId(entity.getId());
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<String> detailIds = details.stream().map(PurchasePriceChangeDetailEntity::getPurchasePriceDetailId).collect(Collectors.toList());

        List<PurchasePriceDetailEntity> purchasePriceDetailList = purchasePriceDetailService.listByIds(detailIds);
        //价目明细
        if (CollectionUtils.isEmpty(purchasePriceDetailList)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }



        List<JSONObject> list = new ArrayList<>();
        for (PurchasePriceChangeDetailEntity detailEntity : details) {

            PurchasePriceDetailEntity purchasePriceDetailEntity = purchasePriceDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getPurchasePriceDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchasePriceDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_98024);
            }

            JSONObject jsonObject = new JSONObject();
            //采购价目编号
            jsonObject.set("CGJM_code",detailEntity.getCJJMCode());
            //采购价目明细金蝶id
            jsonObject.set("kingdeeDetailId",purchasePriceDetailEntity.getKingdeeDetailId());
            //供应商编号
            jsonObject.set("supplierCode",supplierEntity.getCode());
            //从
            jsonObject.set("minQty",detailEntity.getMinQty());
            //至
            jsonObject.set("maxQty",detailEntity.getMaxQty());

            jsonObject.set("skuNo",detailEntity.getSkuNo());
            jsonObject.set("beforeTaxPrice",purchasePriceDetailEntity.getTaxPrice());
            jsonObject.set("afterTaxPrice",detailEntity.getTaxPrice());
            jsonObject.set("beforeTaxRate",MathUtil.multiply(purchasePriceDetailEntity.getTaxRate(),MathUtil.BigDecimal_100));
            jsonObject.set("afterTaxRate",MathUtil.multiply(detailEntity.getTaxRate(),MathUtil.BigDecimal_100));
            jsonObject.set("effectiveDate",detailEntity.getEffectiveDate());
            jsonObject.set("expireDate",detailEntity.getExpireDate());
            list.add(jsonObject);
        }
        resultMap.put("list",list);

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_PURCHASE_PRICE_CHANGE_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                PushSyncStatusDTO.KingdeeDTO syncKingdeeDTO = new PushSyncStatusDTO.KingdeeDTO(entity.getId(),operate,"", SyncStatusEnum.IN_SYNC.getCode());
                return purchasePriceChangeService.updateSyncKingdeeStatus(syncKingdeeDTO);
            }
            return Boolean.TRUE;
        });
    }
}
