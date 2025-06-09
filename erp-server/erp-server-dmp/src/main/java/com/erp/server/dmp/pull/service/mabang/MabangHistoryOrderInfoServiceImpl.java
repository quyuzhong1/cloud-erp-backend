package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.SaveData;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.BiDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.erp.server.dmp.utils.MabangApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 马帮订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.GET_HISTORY_ORDER_LIST)
public class MabangHistoryOrderInfoServiceImpl implements IReportSaveService<OrderEntity> {
    @Resource
    private MongoService mongoService;

    @Autowired
    private MQProducerService mqProducerService;
    /**
     * 拉取订单数据
     *
     * @param dto 任务信息
     */
    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        List<OrderEntity> entityList = MabangApiUtils.queryHistorySalesList(dto.getPlatformApiEnum().getTaskName(), nextTime);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取马帮历史销售订单列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取马帮历史销售订单列表数据 entityList.size = {} ", entityList.size());
        List<OrderEntity> insertList = new ArrayList<>();
        List<OrderEntity> pushToMqList = new ArrayList<>();
        for (OrderEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByOrderIdAndSaleNum(entity.getPlatformOrderId(), entity.getSalesRecordNumber());
            List<OrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            OrderEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            pushToMqList.add(entity);
            entity.set_id(null);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_MABANG_ORDER);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("马帮销售订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<BiOrderInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(MabangOrderInfoServiceImpl::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());
        Map<String, OrderEntity> orderEntityMap = pushToMqList.stream().collect(Collectors.toMap(OrderEntity::getPlatformOrderId, e -> e));
        // 异步推送到MQ
        List<BiOrderInfoEntity> biOrderInfoEntityList = entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_SALE_ORDER_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getPlatformOrderId(), msg.getSalesRecordNumber()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送马帮订单MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
            BiDeliveryDetailInfoEntity deliveryDetailInfo = MabangDeliveryDetailServiceImpl.initOrderInfoEntity(orderEntityMap.get(msg.getPlatformOrderId()));
            SendResult cleanResult = mqProducerService.syncClassMsgByDelayLevel(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_DELIVERY_ORDER_TAG.getName(),
                    deliveryDetailInfo, StrUtil.format("{}_{}", deliveryDetailInfo.getPlatformOrderId(), deliveryDetailInfo.getBillNo()));
            if (!SendStatus.SEND_OK.equals(cleanResult.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送马帮发货单MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("马帮历史订单数据为：{}" , JSON.toJSONString(biOrderInfoEntityList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {

    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(OrderEntity mongoDatum) {

    }

}
