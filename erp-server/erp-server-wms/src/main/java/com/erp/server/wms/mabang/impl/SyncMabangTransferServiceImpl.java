package com.erp.server.wms.mabang.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.sync.MabangTransferInfoDTO;
import com.erp.model.wms.entity.TransferInfoDetailEntity;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mabang.SyncMabangTransferService;
import com.erp.server.wms.service.TransferInfoDetailService;
import com.erp.server.wms.service.WarehouseService;
import com.google.common.collect.Maps;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 直接调拨单推送到马帮
 * @CreateTime: 2023-06-27  16:34
 * @Author: zhangchunlin
 */
@Service
public class SyncMabangTransferServiceImpl implements SyncMabangTransferService {

    @Autowired
    private TransferInfoDetailService transferInfoDetailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private MQProducerService mQProducerService;

    @Autowired
    private WarehouseService warehouseService;

    @Override
    public void syncDataToMabang(TransferInfoEntity entity, String operate) {
        // 直接调拨单明细信息
        List<TransferInfoDetailEntity> detailList = transferInfoDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        // 根据ids查询sku信息
        List<String> skuIdList = detailList.stream().map(TransferInfoDetailEntity::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);
        if (CollectionUtils.isEmpty(productDetailEntityList)) {
            return;
        }

        //仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(Arrays.asList(entity.getInWarehouseId(), entity.getOutWarehouseId()));
        Map<String,WarehouseEntity> warehouseMap = warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getId, Function.identity()));
        if(!warehouseMap.containsKey(entity.getInWarehouseId())) {
            throw new ServiceException("调入仓库错误");
        } else {
            entity.setInWarehouseCode(warehouseMap.get(entity.getInWarehouseId()).getKingdeeWarehouseCode());
        }
        if(!warehouseMap.containsKey(entity.getOutWarehouseId())) {
            throw new ServiceException("调出仓库错误");
        } else {
            entity.setOutWarehouseCode(warehouseMap.get(entity.getOutWarehouseId()).getKingdeeWarehouseCode());
        }

        MabangTransferInfoDTO mabangTransferInfoDTO = new  MabangTransferInfoDTO();
        mabangTransferInfoDTO.setTransferInfo(entity);
        mabangTransferInfoDTO.setTransferList(detailList);
        mabangTransferInfoDTO.setOperate(operate);
        mabangTransferInfoDTO.setSourceType(SourceTypeEnum.TRANSFER_INFO.getCode());
        // 异步推送MQ
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_WMS_TO_DMP_TOPIC, RocketMqTagEnum.ERP_DMP_TRANSFER_INFO_TAG.getName(), mabangTransferInfoDTO, entity.getId());
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {

            }
            return Boolean.TRUE;
        });

    }

}