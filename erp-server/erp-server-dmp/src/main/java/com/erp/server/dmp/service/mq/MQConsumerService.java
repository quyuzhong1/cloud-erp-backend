package com.erp.server.dmp.service.mq;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.CleanBaseDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.gyy.*;
import com.erp.model.dmp.kingdee.*;
import com.erp.model.dmp.mabang.*;
import com.erp.model.dmp.mabang.item.RefundOrderItemEntity;
import com.erp.model.plm.dto.NewProductDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.dmp.*;
import com.erp.server.dmp.service.DmpBomService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MQConsumerService {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Resource
    private DmpDeliveryDetailInfoService deliveryDetailInfoService;
    @Resource
    private DmpRefundInfoService dmpRefundInfoService;

    @Resource
    private DmpReturnOrderInfoService dmpReturnOrderInfoService;
    @Resource
    private DmpShopInfoService dmpShopInfoService;
    @Resource
    private DmpSkuInfoService dmpSkuInfoService;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private DmpOrderItemService dmpOrderItemService;
    @Resource
    private DmpBomService dmpBomService;
    @Resource
    private MongoService mongoService;

    // topic需要和生产者的topic一致，consumerGroup属性是必须指定的，内容可以随意
    // selectorExpression的意思指的就是tag，默认为“*”，不设置的话会监听所有消息

    // 注意：这个ConsumerSend2和上面ConsumerSend在没有添加tag做区分时，不能共存，
    // 不然生产者发送一条消息，这两个都会去消费，如果类型不同会有一个报错，所以实际运用中最好加上tag，写这只是让你看知道就行

    /**
     * rocketmq 监听销售订单相关数据
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "gyy_sales_order_tag||gyy_sales_history_order_tag||kingdee_sales_order_tag||mabang_sales_order_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-sales_order_consumer")
    public class ConsumerErpSalesOrder implements RocketMQListener<DmpOrderInfoEntity> {
        @Override
        public void onMessage(DmpOrderInfoEntity ext) {
            log.info("监听到销售订单消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            dmpOrderInfoService.checkOrder(ext);
            MapUtil mapUtil = getMapParam();

            if(PlatformEnum.GYY.getDesc().equals(ext.getPlatformSign())){
                OrderMongoDTO updateDto = OrderMongoDTO.getByCode(ext.getSalesRecordNumber());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_GYY_ORDER, GyyOrderEntity.class);
            }
            if(PlatformEnum.MABANG.getDesc().equals(ext.getPlatformSign())){
                OrderMongoDTO updateDto = OrderMongoDTO.getByPlatformOrderId(ext.getPlatformOrderId());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
            }
            if(PlatformEnum.KINGDEE.getDesc().equals(ext.getPlatformSign())){
                OrderMongoDTO updateDto = OrderMongoDTO.getByFBillNo(ext.getPlatformOrderId());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_KINGDEE_ORDER, KingdeeOrderEntity.class);
            }
        }
    }

    /**
     * rocketmq 监听发货订单相关数据
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "gyy_delivery_order_tag||kingdee_delivery_order_tag||mabang_delivery_order_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-sales_delivery_consumer")
    public class ConsumerErpDeliveryOrder implements RocketMQListener<DmpDeliveryDetailInfoEntity> {
        @Override
        public void onMessage(DmpDeliveryDetailInfoEntity ext) {
            log.info("监听到发货订单消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            deliveryDetailInfoService.checkOrder(ext);
            MapUtil mapUtil = getMapParam();
            if(PlatformEnum.GYY.getDesc().equals(ext.getPlatformSign())){
                OrderMongoDTO updateDto = OrderMongoDTO.getByCode(ext.getBillNo());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_GYY_DELIVERY_DETAIL, GyyDeliveryDetailEntity.class);
            }
            if(PlatformEnum.MABANG.getDesc().equals(ext.getPlatformSign())){
                OrderEntity updateParam = new OrderEntity();
                updateParam.setCleanToDelivery(CleanStatusEnum.CLEANED.getCode());
                updateParam.setLastPushDeliveryTime(LocalDateTime.now());
                mapUtil = JSONObject.parseObject(JSONObject.toJSONString(updateParam), MapUtil.class);
                OrderMongoDTO updateDto = OrderMongoDTO.getByOrderIdAndSaleNum(ext.getPlatformOrderId(), ext.getBillNo());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
            }
            if(PlatformEnum.KINGDEE.getDesc().equals(ext.getPlatformSign())){
                OrderMongoDTO updateDto = OrderMongoDTO.getByFBillNo(ext.getBillNo());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_KINGDEE_DELIVERY_DETAIL, KingdeeDeliveryDetailEntity.class);
            }
        }
    }

    /**
     * rocketmq 监听退款订单相关数据
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "gyy_refund_order_tag||kingdee_refund_order_tag||mabang_refund_order_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-sales_refund_order_consumer")
    public class ConsumerErpRefundOrder implements RocketMQListener<DmpRefundInfoEntity> {
        @Override
        public void onMessage(DmpRefundInfoEntity ext) {
            log.info("监听退款订单消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            dmpRefundInfoService.checkOrder(ext);
            MapUtil mapUtil = getMapParam();
            if(PlatformEnum.GYY.getDesc().equals(ext.getPlatformSign())){
                OrderMongoDTO updateDto = OrderMongoDTO.getByCode(ext.getRefundCode());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_GYY_REFUND, GyyRefundEntity.class);
            }
            if(PlatformEnum.MABANG.getDesc().equals(ext.getPlatformSign())){
                OrderMongoDTO updateDto = new OrderMongoDTO(ext.getRefundCode());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_MABANG_REFUND, RefundOrderEntity.class);
            }
            if(PlatformEnum.KINGDEE.getDesc().equals(ext.getPlatformSign())){
                OrderMongoDTO updateDto = OrderMongoDTO.getByFBillNo(ext.getRefundCode());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_KINGDEE_REFUND, KingdeeRefundOrderEntity.class);
            }
        }
    }

    /**
     * rocketmq 监听退款订单相关数据
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "gyy_return_order_tag||kingdee_return_order_tag||mabang_return_order_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-sales_return_order_consumer")
    public class ConsumerErpReturnOrder implements RocketMQListener<DmpReturnOrderInfoEntity> {
        @Override
        public void onMessage(DmpReturnOrderInfoEntity ext) {
            log.info("监听退货订单消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            dmpReturnOrderInfoService.checkOrder(ext);
            MapUtil mapUtil = getMapParam();

            if(PlatformEnum.GYY.getDesc().equals(ext.getPlatformSign())){
                OrderMongoDTO updateDto = OrderMongoDTO.getByCode(ext.getReturnCode());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_GYY_RETURN_ORDER, GyyReturnOrderEntity.class);
            }
            if(PlatformEnum.MABANG.getDesc().equals(ext.getPlatformSign())){
                OrderMongoDTO updateDto = OrderMongoDTO.getByReturnOrderId(ext.getReturnCode());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_MABANG_RETURN_ORDER, ReturnOrderEntity.class);
            }
            if(PlatformEnum.KINGDEE.getDesc().equals(ext.getPlatformSign())){
                OrderMongoDTO updateDto = OrderMongoDTO.getByFBillNo(ext.getReturnCode());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_KINGDEE_RETURN_ORDER, KingdeeReturnOrderEntity.class);
            }
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "gyy_shop_info_tag||kingdee_shop_info_tag||mabang_shop_info_tag||kingdee_ecc_shop_info_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-sales_shop_info_consumer")
    public class ConsumerErpShopInfo implements RocketMQListener<DmpShopInfoEntity> {
        @Override
        public void onMessage(DmpShopInfoEntity ext) {
            log.info("监听店铺信息消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            if(PlatformEnum.KINGDEE.getDesc().equals(ext.getPlatformSign()) || PlatformEnum.KINGDEE_ECC.getDesc().equals(ext.getPlatformSign())){
                dmpShopInfoService.checkShopByKingDee(ext);
            }else {
                dmpShopInfoService.checkOrder(ext);
            }
            MapUtil mapUtil = getMapParam();
            if(PlatformEnum.GYY.getDesc().equals(ext.getPlatformSign())){
                OrderMongoDTO updateDto = OrderMongoDTO.getByCode(ext.getPlatformShopNo());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_GYY_SHOP, GyyShopInfoEntity.class);
            }
            if(PlatformEnum.MABANG.getDesc().equals(ext.getPlatformSign())){
                OrderMongoDTO updateDto = new OrderMongoDTO(ext.getPlatformShopNo());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_MABANG_SHOP, ShopEntity.class);
            }
            OrderMongoDTO updateDto = OrderMongoDTO.getByFNumber(ext.getPlatformShopNo());
            if(PlatformEnum.KINGDEE.getDesc().equals(ext.getPlatformSign())){
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_KINGDEE_SHOP, KingdeeShopEntity.class);
            }
            if(PlatformEnum.KINGDEE_ECC.getDesc().equals(ext.getPlatformSign())){
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_KINGDEE_ECC_SHOP, KingdeeEccShopEntity.class);
            }
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "kingdee_sku_info_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-sales_sku_info_consumer")
    public class ConsumerErpSkuInfo implements RocketMQListener<DmpSkuInfoEntity> {
        @Override
        public void onMessage(DmpSkuInfoEntity ext) {
            log.info("监听商品信息消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            dmpSkuInfoService.checkOrder(ext);
            MapUtil mapUtil = getMapParam();
            if(PlatformEnum.KINGDEE.getDesc().equals(ext.getPlatformSign())){
                OrderMongoDTO updateDto = OrderMongoDTO.getByFNumber(ext.getSkuNo());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_KINGDEE_SKU, KingdeeSkuEntity.class);
            }
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC,
            selectorExpression = "sync_dmp_product_info_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-plm_product_info_consumer")
    public class ConsumerPlmProductInfo implements RocketMQListener<List<ProductInfoEntity>> {
        @Override
        public void onMessage(List<ProductInfoEntity> ext) {
            productInfoService.saveOrUpdateProductInfo(ext);
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC,
            selectorExpression = "sync_dmp_product_sku_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-plm_product_detail_consumer")
    public class ConsumerPlmProductDetail implements RocketMQListener<List<ProductDetailEntity>> {
        @Override
        public void onMessage(List<ProductDetailEntity> ext) {
            productDetailService.saveOrUpdateProductDetail(ext);
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC,
            selectorExpression = "sync_dmp_product_listing_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-plm_product_listing_consumer")
    public class ConsumerPlmProductListing implements RocketMQListener<Map<String, List<NewProductDTO>>>  {
        @Override
        public void onMessage(Map<String,List<NewProductDTO>> ext) {
            dmpOrderItemService.updateNewSign(ext);
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC,
            selectorExpression = "get_dmp_product_listing_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-get_product_listing_consumer")
    public class ConsumerGetProductListing implements RocketMQListener<Map<String, List<NewProductDTO>>>  {
        @Override
        public void onMessage(Map<String,List<NewProductDTO>> ext) {
            dmpOrderItemService.getProductListing(ext);
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "mabang_sku_combo_info_tag||mabang_sku_machining_info_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-sku_bom_consumer")
    public class ConsumerErpSkuBomOrder implements RocketMQListener<ComboSkuInfoEntity> {
        @Override
        public void onMessage(ComboSkuInfoEntity ext) {
            log.info("监听组合加工SKU消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            dmpBomService.checkOrder(ext);
            MapUtil mapUtil = getMapParam();
            if("combine".equals(ext.getRelationType())){
                OrderMongoDTO updateDto = OrderMongoDTO.getByComboSku(ext.getComboSku());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_MABANG_COMBO_SKU, ComboSkuInfoEntity.class);
            }
            if ("machining".equals(ext.getRelationType())){
                OrderMongoDTO updateDto = new OrderMongoDTO(ext.get_id());
                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_MABANG_SKU, SkuInfoEntity.class);
            }
        }
    }

    private <T extends CleanBaseDTO> void finishClean(MapUtil mapUtil, OrderMongoDTO updateDto,String tableName, Class<T> clazz) {
        List<T> mongoData = mongoService.findMongoData(updateDto, 0, 0, tableName, clazz);
        if(CollectionUtil.isEmpty(mongoData)){
            throw new RuntimeException("mongo暂未写入数据, 请稍后重试");
        }
        if(CleanStatusEnum.CLEANED.getCode().equals(mongoData.get(0).getIsClean())){
            return;
        }
        mongoService.updateMongoData(updateDto, mapUtil, tableName, clazz);
    }
    private static MapUtil getMapParam() {
        CleanBaseDTO updateParam = new CleanBaseDTO();
        updateParam.setIsClean(CleanStatusEnum.CLEANED.getCode());
        updateParam.setLastPushTime(LocalDateTime.now());
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(updateParam), MapUtil.class);
        return mapUtil;
    }
}
