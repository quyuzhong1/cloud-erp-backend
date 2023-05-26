package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.DmpDeliveryDetailItemEntity;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.model.dmp.mabang.OrderItemEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.utils.MabangApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 马帮出库详情
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.ORDER_GET_HISTORY_DELIVERY_LIST)
public class MabangHistoryDeliveryServiceImpl implements IReportSaveService<OrderEntity> {

    @Resource
    private MongoService mongoService;

    @Resource
    private MQProducerService<DmpDeliveryDetailInfoEntity> mqProducerService;

    /**
     * 拉取订单数据
     *
     * @param dto 任务信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        dto.setPlatformApiEnum(PlatformApiEnum.GET_HISTORY_ORDER_LIST);
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        List<OrderEntity> entityList = MabangApiUtils.queryHistorySalesList(dto.getPlatformApiEnum().getTaskName(), nextTime);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取马帮发货历史订单列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取马帮发货历史订单列表数据 entityList.size = {} ", entityList.size());
        List<OrderEntity> insertList = new ArrayList<>();
        List<OrderEntity> pushToMqList = new ArrayList<>();
        for (OrderEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByOrderIdAndSaleNum(entity.getPlatformOrderId(), entity.getSalesRecordNumber());
            List<OrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_DELIVERY_DETAIL, OrderEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            OrderEntity mongoDatum = mongoData.get(0);
            String id = mongoDatum.get_id();
            mongoDatum.set_id(null);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(id);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_DELIVERY_DETAIL, OrderEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_MABANG_DELIVERY_DETAIL);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("马帮发货历史订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<DmpDeliveryDetailInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(MabangDeliveryDetailServiceImpl::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        List<DmpDeliveryDetailInfoEntity> collect = entityToMqlist.stream().peek(msg -> {
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_DELIVERY_ORDER_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getBillNo(), msg.getBillNo()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
    }

}
