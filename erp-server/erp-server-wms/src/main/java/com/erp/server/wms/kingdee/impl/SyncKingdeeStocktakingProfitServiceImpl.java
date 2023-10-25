package com.erp.server.wms.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.wms.dto.StocktakingProfitLossDetailDTO;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeStocktakingProfitService;
import com.erp.server.wms.service.StocktakingProfitLossDetailService;
import com.erp.server.wms.service.StocktakingProfitLossService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname SyncKingdeeStocktakingProfitServiceImpl
 * @Description TODO
 * @Date 2023-08-14 15:13
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeStocktakingProfitServiceImpl implements SyncKingdeeStocktakingProfitService {

    @Resource
    private StocktakingProfitLossDetailService stocktakingProfitLossDetailService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private StocktakingProfitLossService stocktakingProfitLossService;

    @Resource
    private MQProducerService mQProducerService;

    /**
     * 同步金蝶
     *
     * @param entity
     * @param operate
     */
    @Override
    public void syncDataToKingdee(StocktakingProfitLossEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        if (Objects.isNull(entity)) {
            return;
        }
        List<StocktakingProfitLossDetailDTO.ViewDTO> detailDbList = stocktakingProfitLossDetailService.listByMainIds(Arrays.asList(entity.getId()));
        if (CollectionUtils.isEmpty(detailDbList)) {
            return;
        }
        //更新同步状态为待同步
        stocktakingProfitLossService.updateSyncKingdeeStatus(entity.getId(), SyncStatusEnum.TO_BE_SYNC.getCode(), "", operate);
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //业务id
        resultMap.put("code", entity.getCode());
        resultMap.put("sourceCode", entity.getSourceCode());
        //单据类型
        resultMap.put("billType", entity.getBillType().getCode());
        //单据日期
        resultMap.put("billDate", entity.getBillDate());
        String warehouseOrgCode = "";
        List<String> warehouseIdList = detailDbList.stream().map(StocktakingProfitLossDetailDTO.ViewDTO::getWarehouseId).collect(Collectors.toList());
        //仓库
        List<WarehouseEntity> warehouseList = CollectionUtils.isNotEmpty(warehouseIdList) ? warehouseService.listByIds(warehouseIdList) : Collections.emptyList();

        if (CollectionUtils.isNotEmpty(warehouseList)) {
            String orgId=warehouseList.get(0).getOrgId();
            //组织信息
            List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(orgId));
            if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                warehouseOrgCode = accountingCompanyList.get(0).getCode();
            }
        }
        //仓库组织 货主
        resultMap.put("warehouseOrgCode", warehouseOrgCode);
        List<JSONObject> list = new ArrayList<>(detailDbList.size());
        for (StocktakingProfitLossDetailDTO.ViewDTO item : detailDbList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("skuNo", item.getSkuNo());
            String unit = item.getUnit();
            jsonObject.set("unit", StringUtils.isNotBlank(unit) ? unit : "Pcs");
            jsonObject.set("qty", item.getQty());
            String kingdeeWarehouseCode =warehouseList.stream().filter(w->w.getId().equals(item.getWarehouseId())).
                    map(WarehouseEntity::getKingdeeWarehouseCode).findFirst().orElse("");
            jsonObject.set("kingdeeWarehouseCode", kingdeeWarehouseCode);
            Integer inventoryQty = item.getFrozenQty() + item.getUsableQty();
            jsonObject.set("inventoryQty", inventoryQty);
            jsonObject.set("warehouseLocation", item.getWarehouseLocation());
            jsonObject.set("warehouseOrgCode", warehouseOrgCode);
            jsonObject.set("diffQty", Math.abs(item.getDiffQty()));
            list.add(jsonObject);
        }
        resultMap.put("detailList", list);
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_STOCKTAKING_PROFIT_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return stocktakingProfitLossService.updateSyncKingdeeStatus(entity.getId(), SyncStatusEnum.IN_SYNC.getCode(), "", operate);
            }
            return Boolean.TRUE;
        });
    }
}
