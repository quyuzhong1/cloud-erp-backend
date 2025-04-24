package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.SaveData;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.BiOrderItemSplitEntity;
import com.erp.model.dmp.entity.BiDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.MabangSourcePlatformEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.model.dmp.mabang.item.OrderItemEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.erp.server.dmp.utils.MabangApiUtils;
import com.erp.server.dmp.utils.MapCountUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 马帮订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.ORDER_GET_ORDER_LIST)
public class MabangOrderInfoServiceImpl implements IReportSaveService<OrderEntity> {
    @Resource
    private MongoService mongoService;

    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private MQProducerService mqProducerService;

    public static void main(String[] args) {
        MabangOrderInfoServiceImpl getOrderInfoService = new MabangOrderInfoServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.ORDER_GET_ORDER_LIST.getTaskName());
        jobTaskDTO.setPlatformApiId("5");
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId("30");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.now().minusHours(2));
        jobTaskDTO.setNextTime(LocalDateTime.now());
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(PlatformApiEnum.ORDER_GET_ORDER_LIST);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<OrderEntity> orderEntities = null;
        try {
            orderEntities = getOrderInfoService.pullDate(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(orderEntities);
    }

    /**
     * 拉取订单数据
     *
     * @param dto 任务信息
     */
    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<OrderEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取马帮销售订单列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取马帮销售订单列表数据 entityList.size = {} ", entityList.size());
        List<OrderEntity> insertList = new ArrayList<>();
        List<OrderEntity> pushToMqList = new ArrayList<>();
        for (OrderEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByPlatformOrderId(entity.getPlatformOrderId());
            List<OrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setCleanToDelivery(CleanStatusEnum.UNCLEAN.getCode());
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
                    msg,  msg.getPlatformOrderId());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
            BiDeliveryDetailInfoEntity deliveryDetailInfo = MabangDeliveryDetailServiceImpl.initOrderInfoEntity(orderEntityMap.get(msg.getPlatformOrderId()));
            SendResult cleanResult = mqProducerService.syncClassMsgByDelayLevel(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_DELIVERY_ORDER_TAG.getName(),
                    deliveryDetailInfo, StrUtil.format("{}_{}", deliveryDetailInfo.getPlatformOrderId(), deliveryDetailInfo.getBillNo()));
            if (!SendStatus.SEND_OK.equals(cleanResult.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送马帮发货单MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("马帮订单数据为：{}" , JSON.toJSONString(biOrderInfoEntityList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<OrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            log.warn("马帮需要清洗订单为空 tableName ={} size = {}",tableName,size);
            return;
        }
        for (OrderEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(OrderEntity mongoDatum) {
        BiOrderInfoEntity orderInfo = initOrderInfoEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
        if(null == orderInfo){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil,  MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_SALE_ORDER_TAG.getName(),
                orderInfo, StrUtil.format("{}_{}", orderInfo.getPlatformOrderId(), orderInfo.getSalesRecordNumber()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }


    /**
     * 请求马帮订单接口
     *
     * @param dto
     * @return
     */
    private List<OrderEntity> pullDate(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        return MabangApiUtils.querySalesList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析订单数据
     **/
    public static BiOrderInfoEntity initOrderInfoEntity(OrderEntity orderEntity){
        if (orderEntity.getOrderFee().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (null != orderEntity.getIsResend() && ObjectUtil.equals(orderEntity.getIsResend(),1)) {
            orderEntity.setOrderFee(BigDecimal.ZERO);
        }
        if (ObjectUtil.equals(orderEntity.getOrderStatus(),2) && ObjectUtil.equals(orderEntity.getCanSend(),2) && ObjectUtil.equals(orderEntity.getPlatform(), "Amazon")) {
            return null;
        }
        if (ObjectUtil.equals(orderEntity.getOrderStatus(),5) && (StringUtils.isBlank(orderEntity.getBeforeStatus()) || orderEntity.getBeforeStatus().equals("2"))) {
            return null;
        }

        if (orderEntity.getOrderFee().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (orderEntity.getIsResend().equals(1)) {
            orderEntity.setOrderFee(BigDecimal.ZERO);
        }
        if (orderEntity.getOrderStatus().equals(5) && (StringUtils.isBlank(orderEntity.getBeforeStatus()) || orderEntity.getBeforeStatus().equals("2"))) {
            return null;
        }
        BiOrderInfoEntity biOrderInfoEntity = new BiOrderInfoEntity();
        BeanUtil.copyProperties(orderEntity, biOrderInfoEntity);
        //订单状态 2.配货中 3.已发货 4.已完成 5.已作废 6.退货 7.退款
        Integer orderStatus = orderEntity.getOrderStatus();
        if (null !=  orderEntity.getIsReturned() && 1 == orderEntity.getIsReturned()) {
            orderStatus = 6;
        }
        if (null !=  orderEntity.getIsRefund() && 1 == orderEntity.getIsRefund()) {
            orderStatus = 7;
        }
        biOrderInfoEntity.setOrderStatus(orderStatus);
        //店铺编号
        biOrderInfoEntity.setShopNo(orderEntity.getShopId());
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        // 平台订单时间
        if (!"null".equalsIgnoreCase(orderEntity.getCreateDate()) && StrUtil.isNotBlank(orderEntity.getCreateDate())) {
            biOrderInfoEntity.setPlatformCreateTime(LocalDateUtil.strToLocalDateTime(orderEntity.getCreateDate()));
        }
        //订单来源平台
        MabangSourcePlatformEnum sourcePlatformEnum = MabangSourcePlatformEnum.getByCode(orderEntity.getPlatformId());
        if(StrUtil.isNotBlank(orderEntity.getPlatformId()) && orderEntity.getPlatformId().contains("亚马逊")){
            sourcePlatformEnum = MabangSourcePlatformEnum.AMAZON_FBA;
        }
        biOrderInfoEntity.setSourcePlatform(null != sourcePlatformEnum ? sourcePlatformEnum.getDesc() : orderEntity.getPlatformId());
        if (orderEntity.getOrderStatus().equals(2) && orderEntity.getCanSend().equals(2) && "Amazon".equals(biOrderInfoEntity.getSourcePlatform())) {
            return null;
        }
        //买家地址1
        biOrderInfoEntity.setManStreet(orderEntity.getStreet1());
        //买家地址2
        biOrderInfoEntity.setSecondStreet(orderEntity.getStreet2());
        //买家电话1
        biOrderInfoEntity.setManPhone(orderEntity.getPhone1());
        //买家电话2
        biOrderInfoEntity.setSecondPhone(orderEntity.getPhone2());
        //币种
        biOrderInfoEntity.setCurrencyCode(orderEntity.getCurrencyId());
        //汇率
        biOrderInfoEntity.setCurrencyRate(BigDecimal.ONE);
        if (null != orderEntity.getCurrencyRate()
                && BigDecimal.ZERO.compareTo(orderEntity.getCurrencyRate()) < 0) {
            biOrderInfoEntity.setCurrencyRate(orderEntity.getCurrencyRate());
        }
        //国家英文名称
        biOrderInfoEntity.setCountryNameEn(orderEntity.getCountryNameEN());
        //国家中文名称
        biOrderInfoEntity.setCountryNameCn(orderEntity.getCountryNameCN());
        //平台标识
        biOrderInfoEntity.setPlatformSign(PlatformEnum.MABANG.getDesc());
        //企业Id
//        dmpOrderInfoEntity.setCompanyId(ApiKingdeeOrganizationEnum.ORGANIZATION_WEIJI.getCode());
        //企业名称
//        dmpOrderInfoEntity.setCompanyName(ApiKingdeeOrganizationEnum.ORGANIZATION_WEIJI.getName());
        //发货时间
        if (!"null".equalsIgnoreCase(orderEntity.getExpressTime()) && StrUtil.isNotBlank(orderEntity.getExpressTime())) {
            biOrderInfoEntity.setDeliveryTime(LocalDateUtil.strToLocalDateTime(orderEntity.getCreateDate()));
        }
        biOrderInfoEntity.setCreateTime(LocalDateTime.now());
        biOrderInfoEntity.setItemList(initOrderItem(orderEntity));
        return biOrderInfoEntity;
    }

    /**
     * 解析订单商品数据
     **/
    public static List<BiOrderItemSplitEntity> initOrderItem(OrderEntity orderEntity) {
        List<OrderItemEntity> orderItems = orderEntity.getOrderItem();
        if(CollectionUtil.isEmpty(orderItems)){
            log.warn("MabangOrderInfoServiceImpl>>>initOrderItem>>>orderEntity 详情列表为空 {}", JSONUtil.toJsonStr(orderItems));
            return null;
        }
        // 运费
        BigDecimal shippingFee = null != orderEntity.getShippingTotalOrigin() ? orderEntity.getShippingTotalOrigin() : BigDecimal.ZERO;
        BigDecimal itemTotal = orderEntity.getItemTotalOrigin();
        BigDecimal shareFeeAmount = BigDecimal.ZERO;
        HashMap<String, Integer> skuCountMap = new HashMap<>();
        List<BiOrderItemSplitEntity> items = new ArrayList<>();
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItemEntity orderItemBean = orderItems.get(i);
            BiOrderItemSplitEntity biOrderItemSplitEntity = new BiOrderItemSplitEntity();
            BeanUtil.copyProperties(orderItemBean, biOrderItemSplitEntity);
            //商品名称
            biOrderItemSplitEntity.setItemName(orderItemBean.getTitle());;
            //sku
            String skuNo = orderItemBean.getStockSku();
            biOrderItemSplitEntity.setSkuNo(skuNo);
            //erp平台商品id
            String erpOrderItemId = orderEntity.getPlatformOrderId() + "_" + orderItemBean.getStockSku();
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap,skuNo,erpOrderItemId);
            biOrderItemSplitEntity.setErpOrderItemId(erpOrderItemId);
            //汇率
            biOrderItemSplitEntity.setCurrencyRate(BigDecimal.ONE);
            if (orderEntity.getCurrencyRate() != null
                    && BigDecimal.ZERO.compareTo(orderEntity.getCurrencyRate()) < 0) {
                biOrderItemSplitEntity.setCurrencyRate(orderEntity.getCurrencyRate());
            }
            BigDecimal sellPriceOrigin = ObjectUtil.isNotEmpty(biOrderItemSplitEntity.getSellPriceOrigin()) ? biOrderItemSplitEntity.getSellPriceOrigin() : BigDecimal.ZERO;
            Integer quantity = null != biOrderItemSplitEntity.getQuantity() ? biOrderItemSplitEntity.getQuantity() : 0;
            BigDecimal amountAfter = sellPriceOrigin.multiply(new BigDecimal(quantity));
            // 运费分摊
            // 最后一笔订单 分摊剩余运费
            BigDecimal fee = BigDecimal.ZERO;
            if(i == orderItems.size() - 1){
                fee = shippingFee.subtract(shareFeeAmount);
            }else if (BigDecimal.ZERO.compareTo(shippingFee) < 0 && BigDecimal.ZERO.compareTo(amountAfter) < 0 && BigDecimal.ZERO.compareTo(itemTotal) < 0){
                // 其他订单按照订单金额比例分摊运费 保留4位小数向上取整
                fee = shippingFee.multiply(amountAfter).divide(itemTotal, 4, RoundingMode.HALF_DOWN);
                shareFeeAmount = shareFeeAmount.add(fee);
            }
            biOrderItemSplitEntity.setShippingFee(fee);
            biOrderItemSplitEntity.setAmountAfter(amountAfter.add(fee));
            items.add(biOrderItemSplitEntity);
        }
        return items;
    }


}
