package com.erp.server.dmp.service.mq;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.CleanBaseDTO;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.lingxing.FbaReceiveGroupEntity;
import com.erp.model.dmp.lingxing.ShopEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.rpc.wms.feign.WmsShipmentFeign;
import com.erp.server.dmp.convert.DmpFbaShipmentReceiveConverter;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.ShopInfoMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DMP领星消费者
 */
@Slf4j
@Component
public class MQLingxingConsumerService {

    @Resource
    private ShopInfoMappingService shopInfoMappingService;
    @Resource
    private WmsShipmentFeign wmsShipmentFeign;
    @Resource
    private MongoService mongoService;

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "lx_shop_info_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-lx_shop_info_consumer")
    public class ConsumerErpShopInfo implements RocketMQListener {
        @Override
        public void onMessage(Object extObj) {
            log.info("监听领星店铺信息消息：entity={}", JSONUtil.toJsonStr(extObj));
            ShopEntity ext = JSONUtil.toBean(extObj.toString(), ShopEntity.class);
            // 检查任务和记录平台店铺ID
            shopInfoMappingService.saveAndHandle(ext);
            MapUtil mapUtil = getMapParam();
            OrderMongoDTO updateDto = OrderMongoDTO.getUniqId(ext.getUniqueId());
            finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_LX_SHOP_LIST, ShopEntity.class);
        }
    }

    /**
     * rocketmq 监听发货订单相关数据
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "lx_fba_shipment_receive_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-lx_fba_receive_consumer")
    public class ConsumerErpFbaReceive implements RocketMQListener {
        @Override
        public void onMessage(Object extObj) {
            log.info("监听领星Fba签收明细消息：entity={}", JSONUtil.toJsonStr(extObj));
            FbaReceiveGroupEntity ext = JSONUtil.toBean(extObj.toString(), FbaReceiveGroupEntity.class);
            List<FbaShipmentReceiveEntity> receiveEntityList = DmpFbaShipmentReceiveConverter.INSTANCE.sourceListToEntityList(ext.getDetailList());
            // 保存和检查调拨
            wmsShipmentFeign.saveAndCheckTransfer(receiveEntityList);
            MapUtil mapUtil = getMapParam();
            OrderMongoDTO updateDto = OrderMongoDTO.getUniqId(ext.getUniqueId());
            finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_LX_FBA_SHIPMENT_RECEIVE, FbaReceiveGroupEntity.class);
        }
    }

    private static MapUtil getMapParam() {
        CleanBaseDTO updateParam = new CleanBaseDTO();
        updateParam.setIsClean(CleanStatusEnum.CLEANED.getCode());
        updateParam.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
        return JSONObject.parseObject(JSONObject.toJSONString(updateParam), MapUtil.class);
    }

    private <T extends CleanBaseDTO> void finishClean(MapUtil mapUtil, OrderMongoDTO updateDto,String tableName, Class<T> clazz) {
        List<T> mongoData = mongoService.findMongoData(updateDto, 0, 0, tableName, clazz);
        if(CollectionUtil.isEmpty(mongoData)){
            log.warn("mongo暂未写入数据, 请稍后重试");
            throw new RuntimeException("mongo暂未写入数据, 请稍后重试");
        }
        if(CleanStatusEnum.CLEANED.getCode().equals(mongoData.get(0).getIsClean())){
            return;
        }
        mongoService.updateMongoData(updateDto, mapUtil, tableName, clazz);
    }
}
