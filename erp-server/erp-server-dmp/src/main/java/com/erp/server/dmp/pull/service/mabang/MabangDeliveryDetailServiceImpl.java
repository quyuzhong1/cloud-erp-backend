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
//@SaveData(method = PlatformApiEnum.ORDER_GET_DELIVERY_LIST)
public class MabangDeliveryDetailServiceImpl implements IReportSaveService<OrderEntity> {

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
        dto.setPlatformApiEnum(PlatformApiEnum.ORDER_GET_ORDER_LIST);
        List<OrderEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取马帮发货订单列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取马帮发货订单列表数据 entityList.size = {} ", entityList.size());
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
            log.warn("马帮发货订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
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
                    msg, msg.getBillNo());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
    }

    /**
     * 请求马帮订单接口
     *
     * @param dto
     * @return java.util.List<com.erp.server.dmp.pull.entity.OrderEntity>
     */
    private List<OrderEntity> pullDate(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        return MabangApiUtils.querySalesList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析订单数据
     **/
    public static DmpDeliveryDetailInfoEntity initOrderInfoEntity(OrderEntity orderEntity){
        DmpDeliveryDetailInfoEntity deliveryDetailInfoEntity = new DmpDeliveryDetailInfoEntity();
        //单据编号
        deliveryDetailInfoEntity.setPlatformOrderId(orderEntity.getPlatformOrderId());
        deliveryDetailInfoEntity.setBillNo(orderEntity.getPlatformOrderId());
        //出库编号
        deliveryDetailInfoEntity.setOrderNo(orderEntity.getErpOrderId());
        //物流单号
        deliveryDetailInfoEntity.setLogisticsNo(orderEntity.getTrackNumber());
        //客户名称
        deliveryDetailInfoEntity.setCustomerName(orderEntity.getBuyerName());
        //平台名称
        deliveryDetailInfoEntity.setPlatformName(orderEntity.getPlatformId());
        //店铺编号
        deliveryDetailInfoEntity.setShopNo(orderEntity.getShopId());
        //店铺名称
        deliveryDetailInfoEntity.setShopName(orderEntity.getShopName());
        //出库成本价
        deliveryDetailInfoEntity.setItemTotalCost(orderEntity.getItemTotalCost());
        //出库总价
        deliveryDetailInfoEntity.setOrderTotalCost(orderEntity.getOrderFee());
        //国家英文名称
        deliveryDetailInfoEntity.setCountryNameEn(orderEntity.getCountryNameEN());
        //国家中文名称
        deliveryDetailInfoEntity.setCountryNameCn(orderEntity.getCountryNameCN());
        //买家城市
        deliveryDetailInfoEntity.setCity(orderEntity.getCity());
        //买家省份
        deliveryDetailInfoEntity.setProvince(orderEntity.getProvince());
        //买家地址1
        deliveryDetailInfoEntity.setManStreet(orderEntity.getStreet1());
        //买家地址2
        deliveryDetailInfoEntity.setSecondStreet(orderEntity.getStreet2());
        //所属区域
        deliveryDetailInfoEntity.setDistrict(orderEntity.getDistrict());
        //币种
        deliveryDetailInfoEntity.setCurrencyCode(orderEntity.getCarrierCode());
        //汇率
        deliveryDetailInfoEntity.setCurrencyRate(orderEntity.getCurrencyRate());
        //运费
        deliveryDetailInfoEntity.setShippingFee(orderEntity.getShippingFee());
        //补贴金额
        deliveryDetailInfoEntity.setSubsidyAmount(orderEntity.getSubsidyAmount());
        //销售部门
        deliveryDetailInfoEntity.setSaleDeptName("");
        //销售员编号
        deliveryDetailInfoEntity.setSalesManId("");
        //销售员名称
        deliveryDetailInfoEntity.setSalesManName("");
        //"orderStatus":"出库状态 2.配货中 3.已发货 4.已完成 5.已作废",
        int status = 1;
        switch (orderEntity.getOrderStatus()) {
            case 3:
            case 4:
            default:
                break;
            case 5:
                status = 2;
                break;
        }

        //状态 1.已发货 2.已作废
        deliveryDetailInfoEntity.setStatus(status);

        //平台单据审核时间
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        //平台单据创建时间
        if (StringUtils.isNotBlank(orderEntity.getCreateDate()) && !"null".equals(orderEntity.getCreateDate())) {
            deliveryDetailInfoEntity.setPlatformCreateTime(LocalDateTime.parse(orderEntity.getCreateDate(),sdf));
        }

        //平台单据修改时间
        if (StringUtils.isNotBlank(orderEntity.getOperTime()) && !"null".equals(orderEntity.getOperTime())) {
            deliveryDetailInfoEntity.setPlatformUpdateTime(LocalDateTime.parse(orderEntity.getOperTime(),sdf));
        }
        //发货时间
        if (StringUtils.isNotBlank(orderEntity.getExpressTime()) && !"null".equals(orderEntity.getExpressTime())) {
            deliveryDetailInfoEntity.setDeliveryDate(LocalDateTime.parse(orderEntity.getExpressTime(),sdf));
        }
        //备注
        deliveryDetailInfoEntity.setRemark(orderEntity.getRemark());
        //平台标识
        deliveryDetailInfoEntity.setPlatformSign(PlatformEnum.MABANG.getDesc());
        //企业Id
        deliveryDetailInfoEntity.setCompanyId(ApiKingdeeOrganizationEnum.ORGANIZATION_WEIJI.getCode());
        //企业名称
        deliveryDetailInfoEntity.setCompanyName(ApiKingdeeOrganizationEnum.ORGANIZATION_WEIJI.getName());
        deliveryDetailInfoEntity.setPlatformApproveTime(LocalDateTime.parse(orderEntity.getCreateDate(), sdf));
        //创建时间
        deliveryDetailInfoEntity.setCreateTime(LocalDateTime.now());
        deliveryDetailInfoEntity.setDetails(initOrderItem(orderEntity));
        return deliveryDetailInfoEntity;
    }

    /**
     * 解析订单商品数据
     **/
    public static List<DmpDeliveryDetailItemEntity> initOrderItem(OrderEntity orderEntity) {
        List<OrderItemEntity> orderItems = orderEntity.getOrderItem();
        if(CollectionUtil.isEmpty(orderItems)){
            log.warn("MabangOrderInfoServiceImpl>>>initOrderItem>>>orderEntity 详情列表为空 {}", JSONUtil.toJsonStr(orderItems));
            return null;
        }
        List<DmpDeliveryDetailItemEntity> items = new ArrayList<>();
        for (OrderItemEntity itemEntity : orderItems) {
            DmpDeliveryDetailItemEntity delivery = new DmpDeliveryDetailItemEntity();
            //商品id
            delivery.setItemId(itemEntity.getItemId());
            //平台sku
            delivery.setPlatformSku(itemEntity.getPlatformSku());
            //商品sku编号
            delivery.setSkuNo(itemEntity.getStockSku());
            //商品名称
            delivery.setItemName(itemEntity.getTitle());
            //商品成本价
            delivery.setCostPrice(itemEntity.getCostPrice());
            //商品售价
            delivery.setSellPrice(itemEntity.getSellPrice());
            //商品数量
            delivery.setQuantity(itemEntity.getQuantity());
            //总售价
            delivery.setAmount(itemEntity.getSellPrice());
            //商品单位
            delivery.setProductUnit(itemEntity.getProductUnit());
            //是否是赠品 1. 是 2. 否
            if (itemEntity.getIsGift() == 1) {
                delivery.setIsGift(1);
            } else {
                delivery.setIsGift(2);
            }
            //属性
            delivery.setSpecifics(itemEntity.getSpecifics());
            //出库商品备注
            delivery.setItemRemark(itemEntity.getItemRemark());
            //仓库
            delivery.setStockName(itemEntity.getStockWarehouseName());
            //库位
            delivery.setWarehouseLocation(itemEntity.getStockGrid());
            items.add(delivery);
        }
        return items;
    }
}
