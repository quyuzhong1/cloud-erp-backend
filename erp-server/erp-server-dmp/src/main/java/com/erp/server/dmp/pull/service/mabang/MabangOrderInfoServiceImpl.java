package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.erp.common.business.constant.RocketMqTopic;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.RocketMqTagEnum;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.model.dmp.mabang.OrderItemEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.service.mq.MQProducerService;
import com.erp.server.dmp.utils.MabangApiUtils;
import com.erp.server.dmp.utils.MapCountUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private MQProducerService<DmpOrderInfoEntity> mqProducerService;

    public static void main(String[] args) {
        MabangOrderInfoServiceImpl getOrderInfoService = new MabangOrderInfoServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.ORDER_GET_ORDER_LIST.getTaskName());
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.now().minusHours(2));
        jobTaskDTO.setNextTime(LocalDateTime.now());
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
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
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<OrderEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取马帮销售订单列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取马帮销售订单列表数据 entityList.size = {} ", entityList.size());
        List<OrderEntity> insertList = new ArrayList<>();
        List<OrderEntity> pushToMqList = new ArrayList<>();
        for (OrderEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByOrderIdAndSaleNum(entity.getPlatformOrderId(), entity.getSalesRecordNumber());
            List<OrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
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
        List<DmpOrderInfoEntity> mabangToMqlist = pushToMqList.parallelStream()
                .map(MabangOrderInfoServiceImpl::initOrderInfoEntity)
                .collect(Collectors.toList());

        // 异步推送到MQ
        mabangToMqlist.stream().peek(msg ->
                        mqProducerService.asyncClassMsg(RocketMqTopic.DMP_TOPIC, RocketMqTagEnum.MABANG_SALE_ORDER_TAG.getName(),
                                msg, StrUtil.format("{}_{}", msg.getPlatformOrderId(), msg.getSalesRecordNumber())))
                .collect(Collectors.toList());
    }

    /**
     * 请求马帮订单接口
     *
     * @param dto
     * @return
     */
    private List<OrderEntity> pullDate(RequestDTO dto) throws Exception {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        return MabangApiUtils.querySalesList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析订单数据
     **/

    public static DmpOrderInfoEntity initOrderInfoEntity(OrderEntity orderEntity){
        DmpOrderInfoEntity dmpOrderInfoEntity = new DmpOrderInfoEntity();
        BeanUtil.copyProperties(orderEntity, dmpOrderInfoEntity);
        //订单状态 2.配货中 3.已发货 4.已完成 5.已作废 6.退货 7.退款
        Integer orderStatus = orderEntity.getOrderStatus();
        if (null !=  orderEntity.getIsReturned() && 1 == orderEntity.getIsReturned()) {
            orderStatus = 6;
        }
        if (null !=  orderEntity.getIsRefund() && 1 == orderEntity.getIsRefund()) {
            orderStatus = 7;
        }
        dmpOrderInfoEntity.setOrderStatus(orderStatus);
        //店铺编号
        dmpOrderInfoEntity.setShopNo(orderEntity.getShopId());
        // 平台订单时间
        dmpOrderInfoEntity.setPlatformCreateTime(orderEntity.getPaidTime());
        //订单来源平台
        dmpOrderInfoEntity.setSourcePlatform(orderEntity.getPlatformId());
        //买家地址1
        dmpOrderInfoEntity.setManStreet(orderEntity.getStreet1());
        //买家地址2
        dmpOrderInfoEntity.setSecondStreet(orderEntity.getStreet2());
        //买家电话1
        dmpOrderInfoEntity.setManPhone(orderEntity.getPhone1());
        //买家电话2
        dmpOrderInfoEntity.setSecondPhone(orderEntity.getPhone2());
        //币种
        dmpOrderInfoEntity.setCurrencyCode(orderEntity.getCurrencyId());
        //汇率
        dmpOrderInfoEntity.setCurrencyRate(BigDecimal.ONE);
        if (null != orderEntity.getCurrencyRate()
                && BigDecimal.ZERO.compareTo(orderEntity.getCurrencyRate()) < 0) {
            dmpOrderInfoEntity.setCurrencyRate(orderEntity.getCurrencyRate());
        }
        //国家英文名称
        dmpOrderInfoEntity.setCountryNameEn(orderEntity.getCountryNameEN());
        //国家中文名称
        dmpOrderInfoEntity.setCountryNameCn(orderEntity.getCountryNameCN());
        //平台标识
        dmpOrderInfoEntity.setPlatformSign(PlatformEnum.MABANG.getDesc());
        //企业Id
//        dmpOrderInfoEntity.setCompanyId(ApiKingdeeOrganizationEnum.ORGANIZATION_WEIJI.getCode());
        //企业名称
//        dmpOrderInfoEntity.setCompanyName(ApiKingdeeOrganizationEnum.ORGANIZATION_WEIJI.getName());
        //发货时间
        dmpOrderInfoEntity.setDeliveryTime(orderEntity.getTransportTime());
        dmpOrderInfoEntity.setCreateTime(LocalDateTime.now());
        dmpOrderInfoEntity.setItemList(initOrderItem(orderEntity));
        return dmpOrderInfoEntity;
    }

    /**
     * 解析订单商品数据
     **/
    public static List<DmpOrderItemEntity> initOrderItem(OrderEntity orderEntity) {
        List<OrderItemEntity> orderItems = orderEntity.getOrderItem();
        // 运费
        BigDecimal shippingFee = null != orderEntity.getShippingTotalOrigin() ? orderEntity.getShippingTotalOrigin() : BigDecimal.ZERO;
        BigDecimal itemTotal = orderEntity.getItemTotalOrigin();
        BigDecimal shareFeeAmount = BigDecimal.ZERO;
        HashMap<String, Integer> skuCountMap = new HashMap<>();
        List<DmpOrderItemEntity> items = new ArrayList<>();
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItemEntity orderItemBean = orderItems.get(i);
            DmpOrderItemEntity dmpOrderItemEntity = new DmpOrderItemEntity();
            BeanUtil.copyProperties(orderItemBean, dmpOrderItemEntity);
            //商品名称
            dmpOrderItemEntity.setItemName(orderItemBean.getTitle());;
            //sku
            String skuNo = orderItemBean.getStockSku();
            dmpOrderItemEntity.setSkuNo(skuNo);
            //erp平台商品id
            String erpOrderItemId = orderEntity.getPlatformOrderId() + "_" + orderItemBean.getStockSku();
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap,skuNo,erpOrderItemId);
            dmpOrderItemEntity.setErpOrderItemId(erpOrderItemId);
            //汇率
            dmpOrderItemEntity.setCurrencyRate(BigDecimal.ONE);
            if (orderEntity.getCurrencyRate() != null
                    && BigDecimal.ZERO.compareTo(orderEntity.getCurrencyRate()) < 0) {
                dmpOrderItemEntity.setCurrencyRate(orderEntity.getCurrencyRate());
            }
            BigDecimal sellPriceOrigin = ObjectUtil.isNotEmpty(dmpOrderItemEntity.getSellPriceOrigin()) ? dmpOrderItemEntity.getSellPriceOrigin() : BigDecimal.ZERO;
            Integer quantity = null != dmpOrderItemEntity.getQuantity() ? dmpOrderItemEntity.getQuantity() : 0;
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
            dmpOrderItemEntity.setShippingFee(fee);
            dmpOrderItemEntity.setAmountAfter(amountAfter.add(fee));
            items.add(dmpOrderItemEntity);
        }
        return items;
    }
}
