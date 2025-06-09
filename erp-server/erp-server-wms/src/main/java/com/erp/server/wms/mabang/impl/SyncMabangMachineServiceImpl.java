package com.erp.server.wms.mabang.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.sync.MabangMachineInfoDTO;
import com.erp.model.wms.entity.MachineDetailEntity;
import com.erp.model.wms.entity.MachineInfoEntity;
import com.erp.model.wms.entity.MachineSubComponentsEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mabang.SyncMabangMachineService;
import com.erp.server.wms.service.MachineDetailService;
import com.erp.server.wms.service.MachineSubComponentsService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @CreateTime: 2023-07-03  11:36
 * @Author: zhangchunlin
 */
@Service
@Slf4j
public class SyncMabangMachineServiceImpl implements SyncMabangMachineService {

    @Resource
    private MachineDetailService machineDetailService;

    @Resource
    private MachineSubComponentsService machineSubComponentsService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private MQProducerService mQProducerService;

    @Override
    public void syncDataToMabang(MachineInfoEntity entity, String operate) {
        // TODO 此处还有问题，加工单需要是马帮同步过来的发货单SKU，产品反馈后期会做限制
        // 直接加工单明细信息
        List<MachineDetailEntity> detailList = machineDetailService.listByMainId(entity.getId());
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        // 根据ids查询sku信息
        List<String> skuIdList = detailList.stream().map(MachineDetailEntity::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);
        if (CollUtil.isEmpty(productDetailEntityList)) {
            return;
        }

        List<String> detailIds = detailList.stream().map(MachineDetailEntity::getId).collect(Collectors.toList());
        List<MachineSubComponentsEntity>  subList = machineSubComponentsService.listByDetailIds(detailIds);
        if (CollUtil.isEmpty(subList)) {
            return;
        }

        List<String> warehouseIds = Lists.newArrayList();
        warehouseIds.add(entity.getWarehouseId());
        for(MachineSubComponentsEntity sub : subList) {
            warehouseIds.add(sub.getWarehouseId());
        }
        warehouseIds = warehouseIds.stream().distinct().collect(Collectors.toList());

        //仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIds);
        Map<String,WarehouseEntity> warehouseMap = warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getId, Function.identity()));
        if(!warehouseMap.containsKey(entity.getWarehouseId())) {
            throw new ServiceException("仓库错误");
        } else {
            entity.setWarehouseCode(warehouseMap.get(entity.getWarehouseId()).getKingdeeWarehouseCode());
        }
        subList.stream().forEach(sub->{
            if(!warehouseMap.containsKey(sub.getWarehouseId())) {
                throw new ServiceException("仓库错误");
            } else {
                sub.setWarehouseCode(warehouseMap.get(sub.getWarehouseId()).getKingdeeWarehouseCode());
            }
        });

        MabangMachineInfoDTO mabangMachineInfoDTO = new MabangMachineInfoDTO();
        mabangMachineInfoDTO.setMachineInfoEntity(entity);
        mabangMachineInfoDTO.setMachineDetailEntityList(detailList);
        mabangMachineInfoDTO.setMachineSubComponentsEntityList(subList);
        mabangMachineInfoDTO.setOperate(operate);
        mabangMachineInfoDTO.setSourceType(SourceTypeEnum.MACHINE_INFO.getCode());
        // 异步推送MQ
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_WMS_TO_DMP_TOPIC, RocketMqTagEnum.ERP_DMP_MACHINE_INFO_TAG.getName(), mabangMachineInfoDTO, entity.getId());
            if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
                log.error("发送消息异常，消息内容：【{}】", JSONObject.toJSONString(mabangMachineInfoDTO));
            }
            return Boolean.TRUE;
        });


    }

}