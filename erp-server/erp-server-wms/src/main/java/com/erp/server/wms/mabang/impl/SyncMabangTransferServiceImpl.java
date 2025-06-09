package com.erp.server.wms.mabang.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.exception.ServiceException;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 直接调拨单推送到马帮
 * @CreateTime: 2023-06-27  16:34
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class SyncMabangTransferServiceImpl implements SyncMabangTransferService {

    @Resource
    private TransferInfoDetailService transferInfoDetailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private WarehouseService warehouseService;

    @Override
    public void syncDataToMabang(TransferInfoEntity entity, String operate) {
        // 直接调拨单明细信息
        List<TransferInfoDetailEntity> detailList = transferInfoDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            log.info("直接调拨单【{}】没有调拨明细信息，无需推送到马帮出入库", entity.getCode());
            return;
        }
        // 根据ids查询sku信息
        List<String> skuIdList = detailList.stream().map(TransferInfoDetailEntity::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);
        if (CollectionUtils.isEmpty(productDetailEntityList)) {
            log.info("直接调拨单【{}】未找到产品明细信息，无需推送到马帮出入库", entity.getCode());
            return;
        }

        //仓库
        List<String> warehouseIdList = detailList.stream().flatMap(obj -> Stream.of(obj.getInWarehouseId(), obj.getOutWarehouseId())).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);
        Map<String,WarehouseEntity> warehouseMap = warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getId, Function.identity()));


        //明细赋值仓库编码
        for (TransferInfoDetailEntity detailEntity : detailList)  {

            log.info("直接调拨单【{}】调入仓库id：{}，调出仓库id：{}", entity.getCode(), detailEntity.getInWarehouseId(), detailEntity.getOutWarehouseId());

            if(!warehouseMap.containsKey(detailEntity.getInWarehouseId())) {
                throw new ServiceException("调入仓库错误");
            } else {
                detailEntity.setInWarehouseCode(warehouseMap.get(detailEntity.getInWarehouseId()).getKingdeeWarehouseCode());
            }
            if(!warehouseMap.containsKey(detailEntity.getOutWarehouseId())) {
                throw new ServiceException("调出仓库错误");
            } else {
                detailEntity.setOutWarehouseCode(warehouseMap.get(detailEntity.getOutWarehouseId()).getKingdeeWarehouseCode());
            }
        }

        MabangTransferInfoDTO mabangTransferInfoDTO = new  MabangTransferInfoDTO();
        mabangTransferInfoDTO.setTransferInfo(entity);
        mabangTransferInfoDTO.setTransferList(detailList);
        mabangTransferInfoDTO.setOperate(operate);
        mabangTransferInfoDTO.setSourceType(SourceTypeEnum.TRANSFER_INFO.getCode());
        // 异步推送MQ
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_WMS_TO_DMP_TOPIC, RocketMqTagEnum.ERP_DMP_TRANSFER_INFO_TAG.getName(), mabangTransferInfoDTO, entity.getId());
            if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
                throw new RuntimeException(CharSequenceUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
            return Boolean.TRUE;
        });

    }

}