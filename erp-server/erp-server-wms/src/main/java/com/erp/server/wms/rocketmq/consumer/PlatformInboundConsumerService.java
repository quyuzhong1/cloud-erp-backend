package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.enums.*;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.wms.entity.OverseasWarehouseInboundDetailEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundReceivedEntity;
import com.erp.model.wms.enums.OverseasFinishStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.service.OverseasWarehouseInboundDetailService;
import com.erp.server.wms.service.OverseasWarehouseInboundReceivedService;
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 下载平台入库数据消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_inbound_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_inbound_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformInboundConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private OverseasWarehouseInboundService overseasWarehouseInboundService;

    @Resource
    private OverseasWarehouseInboundDetailService overseasWarehouseInboundDetailService;

    @Resource
    private OverseasWarehouseInboundReceivedService overseasWarehouseInboundReceivedService;

    @Resource
    private MQProducerService mqProducerService;

    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    public void sendWarnMsg(String syncTaskId) {
        DmpPullTaskEntity dmpPullTaskEntity = dmpTaskFeign.getPullTaskById(syncTaskId);
        WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(dmpPullTaskEntity);
        mqProducerService.sendWarnMsg(msgInfoDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        PlatformInboundDTO dto = JSONUtil.toBean(ext.toString(), PlatformInboundDTO.class);
        //海外仓
        if(WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode().equals(dto.getWarehousePlatformType())){
            boolean updateMainFlag = Boolean.FALSE;
            //根据sku汇总数量
            this.groupBySku(dto);
            //通过单号查询主表记录
            OverseasWarehouseInboundEntity mainEntity = overseasWarehouseInboundService.getByCode(dto.getReceivingCode());
            if (Objects.isNull(mainEntity)) {
                return ApiResult.success();
            }
            if(Objects.nonNull(mainEntity.getReceiveTime()) && dto.getDownloadTime().isBefore(mainEntity.getReceiveTime())){
                return ApiResult.success();
            }
            //查询明细数据
            List<OverseasWarehouseInboundDetailEntity> detailList = overseasWarehouseInboundDetailService.getByMainId(mainEntity.getId());
            Map<String, OverseasWarehouseInboundDetailEntity> detailEntityMap = detailList.stream().collect(Collectors.toMap(OverseasWarehouseInboundDetailEntity::getPlatformSkuNo, Function.identity()));
            Map<String, PlatformInboundDTO.Item> itemMap = dto.getItems().stream().collect(Collectors.toMap(PlatformInboundDTO.Item::getProductSku, Function.identity()));
            List<OverseasWarehouseInboundReceivedEntity> insertReceiveEntityList = new ArrayList<>();
            List<OverseasWarehouseInboundDetailEntity> updateList = new ArrayList<>();
            //更新明细表
            for (OverseasWarehouseInboundDetailEntity detailEntity : detailList) {
                PlatformInboundDTO.Item item = itemMap.get(detailEntity.getPlatformSkuNo());
                if (Objects.isNull(item)) {
                    continue;
                }
                //签收数量不一致才更新
                if (item.getReceivedQuantity().equals(detailEntity.getReceiveQty())) {
                    continue;
                }
                updateMainFlag = true;
                Integer thisSignNumber = item.getReceivedQuantity() - detailEntity.getReceiveQty();
                detailEntity.setReceiveQty(item.getReceivedQuantity());
                detailEntity.setDiffQty(detailEntity.getReceiveQty() - detailEntity.getPackQty());
                detailEntity.setTransportQty(Math.max((detailEntity.getPackQty() - detailEntity.getReceiveQty()), 0));
                detailEntity.setReceiveTime(dto.getDownloadTime());
                detailEntity.setReceiveStatus("already");
                detailEntity.setReceiveType("system");
                updateList.add(detailEntity);
                //如果没有签收数据，在这里封装签收记录
                if(!dto.getHasReceivedData()){
                    OverseasWarehouseInboundReceivedEntity receivedEntity = new OverseasWarehouseInboundReceivedEntity();
                    receivedEntity.setDetailId(detailEntity.getId());
                    receivedEntity.setReceiveQty(thisSignNumber);
                    receivedEntity.setReceiveTime(dto.getDownloadTime());
                    insertReceiveEntityList.add(receivedEntity);
                }
            }
            overseasWarehouseInboundDetailService.updateBatchById(updateList);
            List<String> detailIds = detailList.stream().map(OverseasWarehouseInboundDetailEntity :: getId).collect(Collectors.toList());
            List<OverseasWarehouseInboundReceivedEntity> receivedEntityList = overseasWarehouseInboundReceivedService.listByDetailIds(detailIds);
            Map<String,OverseasWarehouseInboundReceivedEntity> receivedEntityMap = receivedEntityList.stream().collect(Collectors.toMap(v->v.getDetailId()+v.getReceiveQty()+LocalDateTimeUtil.formatNormal(v.getReceiveTime()),Function.identity()));
            //有签收记录直接保存，没有签收记录判断签收数量与数据库是否一致，不一致的话用签收数量-数据库签收数量
            if(dto.getHasReceivedData()){
                //判断是否存在，通过明细id+数量+时间
                for(PlatformInboundDTO.Receiving receiving : dto.getReceivingDataList()){
                    String detailId = detailEntityMap.get(receiving.getProductSku()).getId();
                    if(StringUtil.isBlank(detailId)){
                        continue;
                    }
                    String key = detailId+receiving.getReceiveQty()+LocalDateTimeUtil.formatNormal(receiving.getReceiveTime());
                    if(receivedEntityMap.containsKey(key)){
                        continue;
                    }
                    updateMainFlag = true;
                    OverseasWarehouseInboundReceivedEntity receivedEntity = new OverseasWarehouseInboundReceivedEntity();
                    receivedEntity.setDetailId(detailId);
                    receivedEntity.setReceiveQty(receiving.getReceiveQty());
                    receivedEntity.setReceiveTime(receiving.getReceiveTime());
                    insertReceiveEntityList.add(receivedEntity);
                }
            }
            overseasWarehouseInboundReceivedService.saveBatch(insertReceiveEntityList);

            if(updateMainFlag){
                mainEntity.setReceiveTime(dto.getDownloadTime());
                mainEntity.setInstockStatus(dto.getReceivingStatus());
                mainEntity.setFinishStatus(this.getFinishStatusByReceiveStatus(dto.getReceivingStatus()));
                //更新主表
                overseasWarehouseInboundService.updateById(mainEntity);
            }
        }
        return ApiResult.success();
    }

    private String getFinishStatusByReceiveStatus(String receiveStatus){
        if(receiveStatus.equals(OverseasInstockStatusEnum.SIGNED.getCode()) || receiveStatus.equals(OverseasInstockStatusEnum.FINISH.getCode())|| receiveStatus.equals(OverseasInstockStatusEnum.CANCELED.getCode())){
            return OverseasFinishStatusEnum.AUTO.getCode();
        }
        return OverseasFinishStatusEnum.NOT.getCode();
    }

    private void groupBySku(PlatformInboundDTO dto) {
        List<PlatformInboundDTO.Item> items = dto.getItems();
        Map<String, Integer> receivedQuantityMap = items.stream()
                .collect(Collectors.groupingBy(PlatformInboundDTO.Item::getProductSku, Collectors.summingInt(PlatformInboundDTO.Item::getReceivedQuantity)));
        items = items.stream()
                .peek(item -> {
                    item.setReceivedQuantity(receivedQuantityMap.get(item.getProductSku()));
                })
                .distinct()
                .collect(Collectors.toList());
        dto.setItems(items);
    }

    private WarnMsgInfoDTO buildWarnMsgInfoDTO(DmpPullTaskEntity dmpPullTaskEntity) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_WMS);
        warnMsgInfo.setTitle(StrUtil.format("平台入库消息消费失败，来源平台:{},目标平台:{}",dmpPullTaskEntity.getSourcePlatformName(),dmpPullTaskEntity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setTableId(dmpPullTaskEntity.getId());
        warnMsgInfo.setKeyInfo("");
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }
}
