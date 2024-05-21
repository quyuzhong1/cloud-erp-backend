package com.erp.server.dmp.pull.service.wangdian;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.api.sales.TradeAPI;
import cn.wangdian.erp.sdk.api.sales.dto.TradeQueryRequest;
import cn.wangdian.erp.sdk.api.sales.dto.TradeQueryResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;
import com.alibaba.fastjson.JSON;
import com.common.business.annotation.SaveData;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.DmpDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.DmpDeliveryDetailItemEntity;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.wangdian.OrderEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@SaveData(method = PlatformApiEnum.WANGDIAN_TRADE)
public class WangDianOrderInfoServiceImpl  implements IReportSaveService<OrderEntity> {

    @Resource
    private Client defaultClient;

    @Resource
    private MongoService mongoService;
    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private MQProducerService mqProducerService;
    @Override
    public void pullDataSave(RequestDTO dto) {
        List<OrderEntity> entityList = pullData(dto);
        if (CollectionUtils.isEmpty(entityList)) {
            log.info("拉取旺店通销售订单列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取旺店通销售订单列表数据 entityList.size = {} ", entityList.size());
        List<OrderEntity> insertList = new ArrayList<>();
        List<OrderEntity> pushToMqList = new ArrayList<>();
        for (OrderEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByPlatformOrderId(entity.getStockoutId());
            List<OrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.WANGDIAN_TRADE, OrderEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setCleanToDelivery(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtils.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            OrderEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSON.parseObject(JSON.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.WANGDIAN_TRADE, OrderEntity.class);
        }
        if(!CollectionUtils.isEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.WANGDIAN_TRADE);
        }
        if (CollectionUtils.isEmpty(pushToMqList)){
            log.warn("旺店通销售订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }

        // 构造订单结构
        List<DmpDeliveryDetailInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());
        // 异步推送到MQ
        entityToMqlist.forEach(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_SALE_ORDER_TAG.getName(),
                    msg,  msg.getPlatformOrderId());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(CharSequenceUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        });
    }

    private DmpDeliveryDetailInfoEntity initOrderInfoEntity(OrderEntity orderEntity) {
        DmpDeliveryDetailInfoEntity entity = new DmpDeliveryDetailInfoEntity();
        //出库单ID
        entity.setId(orderEntity.getStockoutId());
        //出库单号
        entity.setBillNo(orderEntity.getOrderNo());
        //旺店通订单编号
        entity.setOrderNo(orderEntity.getSrcOrderNo());
        //订单编号
        entity.setPlatformOrderId(orderEntity.getTradeNo());
        //物流单号
        entity.setLogisticsNo(orderEntity.getLogisticsNo());
        //客户名称
        entity.setCustomerName(orderEntity.getCustomerName());
        //平台名称
        entity.setPlatformName(orderEntity.getPlatformId());
        //店铺编号
        entity.setShopNo(orderEntity.getShopId());
        //店铺名称
        entity.setShopName(orderEntity.getShopName());
        //出库成本价
        entity.setItemTotalCost(orderEntity.getGoodsTotalCost());
        //出库总价
        entity.setOrderTotalCost(orderEntity.getGoodsTotalAmount());
        //国家英文名称
        entity.setCountryNameEn("");
        //国家中文名称
        entity.setCountryNameCn(orderEntity.getReceiverCountry());
        //买家城市
        entity.setCity(orderEntity.getReceiverCity());
        //买家省份
        entity.setProvince(orderEntity.getReceiverProvince());
        //买家地址1
        entity.setManStreet(orderEntity.getReceiverAddress());
        //买家地址2
        entity.setSecondStreet("");
        //所属区域
        entity.setDistrict(orderEntity.getReceiverDistrict());
        //币种
        entity.setCurrencyCode(orderEntity.getCurrency());
        //汇率
        entity.setCurrencyRate(new BigDecimal(1));
        //运费
        entity.setShippingFee(orderEntity.getPostAmount());
        //补贴金额
        entity.setSubsidyAmount(orderEntity.getDiscount());
        //销售部门
        entity.setSaleDeptName("");
        //销售员编号
        entity.setSalesManId("");
        //销售员名称
        entity.setSalesManName("");
        //状态 1.已发货 2.已作废
        entity.setStatus(1);
        //平台单据创建时间
        if (!StringUtils.isEmpty(orderEntity)) {
            entity.setPlatformCreateTime(LocalDateUtil.strToLocalDateTime(orderEntity.getCreatedDate()));
        }
        //平台单据修改时间
        if (!StringUtils.isEmpty(orderEntity.getModified())) {
            entity.setPlatformUpdateTime(LocalDateUtil.strToLocalDateTime(orderEntity.getModified()));
        }
        //发货时间
        if (!StringUtils.isEmpty(orderEntity.getConsignTime())) {
            entity.setDeliveryDate(LocalDateUtil.strToLocalDateTime(orderEntity.getConsignTime()));
        }
        //备注
        entity.setRemark(orderEntity.getRemark());
        //平台标识
        entity.setPlatformSign(PlatformEnum.WANGDIAN.getDesc());
        //企业Id
        entity.setCompanyId(ApiKingdeeOrganizationEnum.ORGANIZATION_WEIJI.getCode());
        //企业名称
        entity.setCompanyName(ApiKingdeeOrganizationEnum.ORGANIZATION_WEIJI.getName());
        entity.setPlatformApproveTime(LocalDateUtil.strToLocalDateTime(orderEntity.getCreatedDate()));
        //创建时间
        entity.setCreateTime(LocalDateTime.now());
        entity.setDetails(initOrderItem(orderEntity));
        return entity;
    }

    private List<DmpDeliveryDetailItemEntity> initOrderItem(OrderEntity orderEntity) {
        List<OrderEntity.DetailItem> detailsList = orderEntity.getDetailsList();
        if(CollectionUtils.isEmpty(detailsList)){
            log.warn("WangDianOrderInfoServiceImpl>>>initOrderItem>>>orderEntity 详情列表为空 {}", JSONUtil.toJsonStr(detailsList));
            return null;
        }
        List<DmpDeliveryDetailItemEntity> items = new ArrayList<>();
        for (OrderEntity.DetailItem itemEntity : detailsList) {
            DmpDeliveryDetailItemEntity delivery = new DmpDeliveryDetailItemEntity();
            //商品id
            delivery.setItemId(itemEntity.getSaleOrderId());
            //平台sku
            delivery.setPlatformSku(itemEntity.getSpecNo());
            //商品sku编号
            delivery.setSkuNo(itemEntity.getSpecNo());
            //商品名称
            delivery.setItemName(itemEntity.getGoodsName());
            //商品成本价
            delivery.setCostPrice(itemEntity.getCostPrice());
            //商品售价
            delivery.setSellPrice(itemEntity.getSellPrice());
            //商品数量
            delivery.setQuantity(itemEntity.getNum().intValue());
            //总售价
            delivery.setAmount(itemEntity.getShareAmount());
            //商品单位
            delivery.setProductUnit(itemEntity.getUnitName());
            //是否是赠品 1. 是 2. 否
            if (itemEntity.getGiftType() == 0) {
                delivery.setIsGift(2);
            } else {
                delivery.setIsGift(1);
            }
            //属性
            delivery.setSpecifics(itemEntity.getSpecCode());
            //出库商品备注
            delivery.setItemRemark(itemEntity.getRemark());
            //仓库
            delivery.setStockName(orderEntity.getWarehouseName());
            //库位
            String warehouseLocation = Optional.ofNullable(itemEntity.getPositionDetailsList()).orElse(Collections.emptyList())
                    .stream().map(OrderEntity.PositionDetailsList::getPositionNo)
                    .collect(Collectors.joining(","));
            delivery.setWarehouseLocation(warehouseLocation);
            delivery.setPlatformOrderId(orderEntity.getTradeNo());
            delivery.setSaleOrderNo(orderEntity.getSrcOrderNo());
            items.add(delivery);
        }
        return items;
    }

    private List<OrderEntity> pullData(RequestDTO dto) {
        List<TradeQueryResponse.OrderItem> result = new ArrayList<>();
        TradeAPI tradeAPI = ApiFactory.get(defaultClient, TradeAPI.class);
        TradeQueryRequest request = new TradeQueryRequest();
        request.setStatus(TradeQueryRequest.STATUS_COMPLETE);
        request.setStatusType(3);
        request.setStartTime(dto.getJobTaskDTO().getLastTime().minusMinutes(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        request.setEndTime(dto.getJobTaskDTO().getNextTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Pager pager = new Pager();
        int pageSize = 200;
        pager.setPageSize(pageSize);
        pager.setPageNo(0);
        boolean hasNext = true;
        while (hasNext) {
            TradeQueryResponse response = tradeAPI.query(request, pager);
            if (ObjectUtil.isEmpty(response) || ObjectUtil.isEmpty(response.getOrders())){
                return BeanMapperUtils.copyList(OrderEntity.class, result);
            }
            result.addAll(response.getOrders());
            Integer totalCount = response.getTotalCount();
            if (totalCount <= pager.getPageNo() * pageSize) {
                hasNext = false;
            }
            pager.setPageNo(pager.getPageNo() + 1);
        }
        return BeanMapperUtils.copyList(OrderEntity.class, result);
    }

    public static void main(String[] args) {
        TradeAPI tradeAPI = ApiFactory.get(DefaultClient.get("wdtapi3","http://47.92.239.46/","wjkj03-test","b6412a9b6:806828718719806966febbfe948893e8"), TradeAPI.class);
        TradeQueryRequest request = new TradeQueryRequest();
        request.setStatus(TradeQueryRequest.STATUS_COMPLETE);
        request.setStatusType(3);
        request.setStartTime("2024-04-29 11:00:00");
        request.setEndTime("2024-04-29 12:00:00");
        Pager pager = new Pager();
        int pageSize = 200;
        pager.setPageSize(pageSize);
        pager.setPageNo(0);
        Object query = tradeAPI.query(request, pager);
        System.out.println(query);

    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<OrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.WANGDIAN_TRADE, OrderEntity.class);
        if (CollectionUtils.isEmpty(mongoData)) {
            log.warn("旺店通需要清洗销售单为空 tableName ={} size = {}",tableName,size);
            return;
        }
        for (OrderEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAndSaveDb(OrderEntity mongoDatum) {
        DmpDeliveryDetailInfoEntity orderInfo = initOrderInfoEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
        if(ObjectUtil.isEmpty(orderInfo)){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSON.parseObject(JSON.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil,  MongoTableNameContant.WANGDIAN_TRADE, OrderEntity.class);
            return;
        }
        MapUtil mapUtil = JSON.parseObject(JSON.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.WANGDIAN_TRADE, OrderEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_SALE_ORDER_TAG.getName(),
                orderInfo, CharSequenceUtil.format("{}_{}", orderInfo.getPlatformOrderId(), orderInfo.getBillNo()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new ServiceException(CharSequenceUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }
}
