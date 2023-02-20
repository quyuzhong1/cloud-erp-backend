package com.erp.server.dmp.pull.service.gyy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.erp.common.business.constant.RocketMqTopic;
import com.common.core.enums.CountrySiteEnum;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.RocketMqTagEnum;
import com.erp.model.dmp.gyy.GyyOrderEntity;
import com.erp.model.dmp.gyy.bean.DetailsBean;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.service.mq.MQProducerService;
import com.erp.server.dmp.utils.GyyApiUtils;
import com.erp.server.dmp.utils.MapCountUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管易云订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.GY_ERP_TRADE_GET)
public class GyyOrderInfoServiceImpl implements IReportSaveService<GyyOrderEntity> {
    @Resource
    private MongoService mongoService;
    @Autowired
    private MQProducerService<DmpOrderInfoEntity> mqProducerService;

    /**
     * 拉取订单数据
     *
     * @param dto 任务信息
     * @return
     */
    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        //请求api
        List<GyyOrderEntity> gyyOrderEntityList = pullDate(dto);
        if (CollectionUtil.isEmpty(gyyOrderEntityList)) {
            log.info("拉取管易订单列表数据为空 .gyyOrderEntityList size = 0 ");
            return;
        }
        //过滤数据
        List<GyyOrderEntity> insertList = new ArrayList<>();
        List<GyyOrderEntity> pushToMqList = new ArrayList<>();
        for (GyyOrderEntity gyyOrderEntity : gyyOrderEntityList) {
            OrderMongoDTO orderMongoDTO = new OrderMongoDTO(gyyOrderEntity.getPlatformCode(), gyyOrderEntity.getCode());
            List<GyyOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_ORDER, GyyOrderEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(gyyOrderEntity);
                pushToMqList.add(gyyOrderEntity);
                continue;
            }
            GyyOrderEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(gyyOrderEntity.toString())) {
                continue;
            }
            pushToMqList.add(gyyOrderEntity);
            // 修改数据
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(gyyOrderEntity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_GYY_ORDER, GyyOrderEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_GYY_ORDER);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("管易销售订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<DmpOrderInfoEntity> mabangToMqlist = pushToMqList.parallelStream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        mabangToMqlist.stream().peek(msg ->
                        mqProducerService.asyncClassMsg(RocketMqTopic.DMP_TOPIC, RocketMqTagEnum.GYY_SALE_ORDER_TAG.getName(),
                                msg, StrUtil.format("{}_{}", msg.getPlatformOrderId(), msg.getSalesRecordNumber())))
                .collect(Collectors.toList());

    }

    /**
     * 请求管易云订单接口
     *
     * @param dto
     * @return
     */
    private List<GyyOrderEntity> pullDate(RequestDTO dto) throws Exception {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        return GyyApiUtils.querySalesList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime, Boolean.FALSE);
    }

    /**
     * 解析订单数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/

    public DmpOrderInfoEntity initOrderInfoEntity(GyyOrderEntity gyyOrderEntity){
        if (StringUtils.isEmpty(gyyOrderEntity.getOrderTypeName()) || !"销售订单".equals(gyyOrderEntity.getOrderTypeName())) {
            return null;
        }
        if (StringUtils.isNotBlank(gyyOrderEntity.getPlatformTradingState())) {
            if (gyyOrderEntity.getPlatformTradingState().contains("取消")) {
                return null;
            }
        }
        DmpOrderInfoEntity dmpOrderInfoEntity = new DmpOrderInfoEntity();
        //平台订单id
        dmpOrderInfoEntity.setPlatformOrderId(gyyOrderEntity.getCode());
        //订单状态 2.配货中 3.已发货 4.已完成 5.已作废 6.退货 7.退款
        Integer orderState = 4;
        //0:未配货 1:部分配货 2:全部配货
        Integer assignState = gyyOrderEntity.getAssignState();
        if (assignState.equals(1) || assignState.equals(2)) {
            orderState = 2;
        }
        //0:未发货 1:部分发货 2:全部发货
        Integer deliveryState = gyyOrderEntity.getDeliveryState();
        if (deliveryState.equals(1) || deliveryState.equals(2)) {
            orderState = 3;
        }
        //0:未退款 1:部分退款 2:全部退款
        Integer refundState = gyyOrderEntity.getRefundState();
        if (refundState.equals(1) || refundState.equals(2)) {
            orderState = 7;
        }
        dmpOrderInfoEntity.setOrderStatus(orderState);
        //买家账号
        dmpOrderInfoEntity.setBuyerUserId(gyyOrderEntity.getVipCode());
        //买家姓名
        dmpOrderInfoEntity.setBuyerName(gyyOrderEntity.getVipName());
        //店铺编号
        dmpOrderInfoEntity.setShopNo(gyyOrderEntity.getShopCode());
        //店铺名称
        dmpOrderInfoEntity.setShopName(gyyOrderEntity.getShopName());
        //待审核订单 1.否 2.是
        dmpOrderInfoEntity.setCanSend(gyyOrderEntity.getApprove() ? 2 : 1);
        //是否退款 1.退款 2.非退款
        dmpOrderInfoEntity.setIsRefund((refundState.equals(1) || refundState.equals(2))? 1 : 2);
        //是否退货 1.退货 2.非退货
        dmpOrderInfoEntity.setIsReturned(2);
        //订单付款时间
        dmpOrderInfoEntity.setPaidTime(gyyOrderEntity.getPaytime());
        //平台订单时间
        dmpOrderInfoEntity.setPlatformCreateTime(gyyOrderEntity.getDealtime());
        //平台交易号
        dmpOrderInfoEntity.setSalesRecordNumber(gyyOrderEntity.getPlatformCode());
        //平台的订单状态
        dmpOrderInfoEntity.setPlatformOrderStatus(gyyOrderEntity.getPlatformTradingState());
        //订单来源平台
        dmpOrderInfoEntity.setSourcePlatform(gyyOrderEntity.getFromTypeName());
        if (StringUtils.isNotBlank(gyyOrderEntity.getReceiverArea())) {
            String[] split = gyyOrderEntity.getReceiverArea().split("-");
            if (split.length >= 1) {
                //买家省份
                dmpOrderInfoEntity.setProvince(split[0]);
            }
            if (split.length >= 2) {
                //买家城市
                dmpOrderInfoEntity.setCity(split[1]);
            }
            if (split.length >= 3) {
                //所属区域
                dmpOrderInfoEntity.setDistrict(split[2]);
            }
        }
        //买家地址1
        dmpOrderInfoEntity.setManStreet(gyyOrderEntity.getReceiverAddress());
        //买家地址2
        dmpOrderInfoEntity.setSecondStreet("");
        //交易关闭时间
        dmpOrderInfoEntity.setCloseDate(null);
        //买家电话1
        dmpOrderInfoEntity.setManPhone(gyyOrderEntity.getReceiverMobile());
        //买家电话2
        dmpOrderInfoEntity.setSecondPhone(gyyOrderEntity.getReceiverPhone());
        //是否平台发货订单 1.否 2.是
        dmpOrderInfoEntity.setFbaFlag(0);
        //平台备注
        dmpOrderInfoEntity.setSellerMessage(gyyOrderEntity.getBuyerMemo());
        //币种
        dmpOrderInfoEntity.setCurrencyCode("CNY");
        //汇率
        dmpOrderInfoEntity.setCurrencyRate(BigDecimal.ONE);
        //商品总售价
        dmpOrderInfoEntity.setItemTotal(gyyOrderEntity.getPaymentAmount());
        //订单金额
        dmpOrderInfoEntity.setOrderFee(gyyOrderEntity.getAmount());
        //运费收入
        dmpOrderInfoEntity.setShippingFee(gyyOrderEntity.getPostFee());
        //平台费
        dmpOrderInfoEntity.setPlatformFee(BigDecimal.ZERO);
        //原始运费收入
        dmpOrderInfoEntity.setShippingTotalOrigin(gyyOrderEntity.getPostFee());
        List<DetailsBean> details = gyyOrderEntity.getDetails();
        BigDecimal costPrice = BigDecimal.ZERO;
        for (DetailsBean detail : details) {
            costPrice = costPrice.add(detail.getCostPrice());
        }
        //订单成本价
        dmpOrderInfoEntity.setOrderCost(costPrice);
        //商品总成本
        dmpOrderInfoEntity.setItemTotalCost(costPrice);
        //商品原始总售价
        dmpOrderInfoEntity.setItemTotalOrigin(gyyOrderEntity.getPaymentAmount());
        //补贴金额
        dmpOrderInfoEntity.setSubsidyAmount(gyyOrderEntity.getDiscountFee());
        //国家英文名称
        dmpOrderInfoEntity.setCountryNameEn(CountrySiteEnum.CHINA.getCurrencyCode());
        //国家中文名称
        dmpOrderInfoEntity.setCountryNameCn(CountrySiteEnum.CHINA.getCurrencyName());
        //平台标识
        dmpOrderInfoEntity.setPlatformSign(PlatformEnum.GYY.getDesc());
        dmpOrderInfoEntity.setCreateTime(LocalDateTime.now());
        dmpOrderInfoEntity.setItemList(initOrderItem(gyyOrderEntity, orderState));
        return dmpOrderInfoEntity;
    }

    /**
     * 解析订单商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public List<DmpOrderItemEntity> initOrderItem(GyyOrderEntity gyyOrderEntity, Integer orderState) {
        List<DetailsBean> orderItem = gyyOrderEntity.getDetails();
        String warehouseCode = gyyOrderEntity.getWarehouseCode();
        List<DmpOrderItemEntity> orderItemList = new ArrayList<>();
        Map<String, Integer> skuCountMap = new HashMap<>();
        for (DetailsBean detailsBean : orderItem) {
            DmpOrderItemEntity dmpOrderItemEntity = new DmpOrderItemEntity();
            //商品id
            dmpOrderItemEntity.setItemId(detailsBean.getItemCode());
            //平台sku
            dmpOrderItemEntity.setPlatformSku(detailsBean.getSkuCode());
            //平台原始sku数量
            dmpOrderItemEntity.setPlatformQuantity(detailsBean.getDeliveringQty());
            //商品名称
            dmpOrderItemEntity.setItemName(detailsBean.getItemName());
            //商品图片
            dmpOrderItemEntity.setPictureUrl("");
            //商品成本价
            dmpOrderItemEntity.setCostPrice(detailsBean.getCostPrice());
            //商品原始售价
            dmpOrderItemEntity.setSellPriceOrigin(detailsBean.getPrice());
            //商品售价
            dmpOrderItemEntity.setSellPrice(detailsBean.getPrice());
            //商品数量
            dmpOrderItemEntity.setQuantity(detailsBean.getQty());
            //商品单位
            dmpOrderItemEntity.setProductUnit(detailsBean.getItemUnitName());
            //是否是赠品 1. 是 2. 否
            dmpOrderItemEntity.setIsGift(detailsBean.getIsGift() ? 1 : 2);
            //缺货订单 0.正在计算是否缺货 1.有货 2.缺货 3.已补货
            dmpOrderItemEntity.setHasGoods(0);
            //是否是组合商品 1.组合 2非组合
            dmpOrderItemEntity.setIsCombo(0);
            //订单商品备注
            dmpOrderItemEntity.setItemRemark(detailsBean.getSkuNote());
            //商品多属性
            if (StringUtils.isNotBlank(detailsBean.getPlatformSkuName())) {
                dmpOrderItemEntity.setSpecifics(detailsBean.getPlatformSkuName());
            }
            //商品状态 1：未付款 2：未发货 3：已发货 4：已作废
            dmpOrderItemEntity.setStatus(orderState);
            //商品仓位
            dmpOrderItemEntity.setStockGrid("");
            //sku
            dmpOrderItemEntity.setSkuNo(detailsBean.getItemCode());
            //库存状态：1.自动创建 2.待开发 3.正常 4.清仓 5.停止销售
            dmpOrderItemEntity.setStockStatus(0);
            //商品仓库编号
            dmpOrderItemEntity.setStockWarehouseId(warehouseCode);
            //erp平台商品id
            String erpOrderItemId = gyyOrderEntity.getCode() + "_" + detailsBean.getItemCode();
            String skuNo = detailsBean.getItemCode();
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, skuNo, erpOrderItemId);
            dmpOrderItemEntity.setErpOrderItemId(erpOrderItemId);
            //汇率
            dmpOrderItemEntity.setCurrencyRate(BigDecimal.ONE);
            //折扣后金额
            dmpOrderItemEntity.setAmountAfter(detailsBean.getAmountAfter());
            orderItemList.add(dmpOrderItemEntity);
        }
        return orderItemList;
    }
}
