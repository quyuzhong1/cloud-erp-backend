package com.erp.server.dmp.pull.service.gyy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.SaveData;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.core.enums.CountrySiteEnum;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.entity.BiOrderItemSplitEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.gyy.GyyOrderEntity;
import com.erp.model.dmp.gyy.bean.DetailsBean;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.erp.server.dmp.utils.GyyApiUtils;
import com.erp.server.dmp.utils.MapCountUtils;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @Resource
    private MQProducerService<BiOrderInfoEntity> mqProducerService;
    @Resource
    private CfgSettingService cfgSettingService;

    /**
     * 拉取订单数据
     *
     * @param dto 任务信息
     * @return
     */
    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto){
        // 请求列表API获取更新订单列表
        List<GyyOrderEntity> gyyOrderEntityList = pullDate(dto);
        if (CollectionUtil.isEmpty(gyyOrderEntityList)) {
            log.info("拉取管易订单列表数据为空 .gyyOrderEntityList size = 0 ");
            return;
        }
        log.info("拉取管易销售订单列表数据 gyyDeliveryDetailEntityList.size = {} ", gyyOrderEntityList.size());
        XxlJobHelper.log("拉取管易销售订单列表数据 gyyDeliveryDetailEntityList.size = {} ", gyyOrderEntityList.size());
        // 对数据进行检查类，需要新增和更新数据
        List<GyyOrderEntity> gyyOrderEntities = gyyOrderEntityList.stream().distinct().collect(Collectors.toList());
        List<GyyOrderEntity> insertList = new ArrayList<>();
        for (GyyOrderEntity gyyOrderEntity : gyyOrderEntities) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByCode(gyyOrderEntity.getCode());
            gyyOrderEntity.setDownloadStatus(0);
            gyyOrderEntity.setApiCode(dto.getPlatformApiEnum().getTaskName());
            List<GyyOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_ORDER, GyyOrderEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(gyyOrderEntity);
                continue;
            }
            GyyOrderEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , gyyOrderEntity)) {
                log.warn("管易销售订单 mongo数据无变化无需更新 deliveryEntity={}", JSONUtil.toJsonStr(gyyOrderEntity));
                continue;
            }
            // 修改数据
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(gyyOrderEntity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_GYY_ORDER, GyyOrderEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            insertList = insertList.stream().distinct().collect(Collectors.toList());
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_GYY_ORDER);
        }

    }


    public void addOrderDetail(GyyOrderEntity gyyOrderEntity){
        // 查询订单详情
        boolean isHistory = gyyOrderEntity.getApiCode().contains("history");
        String apiCode = isHistory ? PlatformApiEnum.GY_ERP_TRADE_HISTORY_DETAIL_GET.getTaskName() : PlatformApiEnum.GY_ERP_TRADE_DETAIL_GET.getTaskName();
        GyyOrderEntity gyyOrder = GyyApiUtils.querySalesOrderDetail(apiCode, gyyOrderEntity.getCode());
        if(null == gyyOrder){
            return;
        }
        gyyOrder.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
        gyyOrder.setDownloadStatus(1);
        gyyOrder.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
        // 修改数据
        updateAndSaveDb(gyyOrder);
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(GyyOrderEntity gyyOrder) {
        BiOrderInfoEntity infoEntity = initOrderInfoEntity(gyyOrder);

        OrderMongoDTO updateDto = OrderMongoDTO.getByCode(gyyOrder.getCode());
        if(null == infoEntity){
            gyyOrder.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(gyyOrder), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_GYY_ORDER, GyyOrderEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(gyyOrder), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_GYY_ORDER, GyyOrderEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.GYY_SALE_ORDER_TAG.getName(),
                infoEntity, StrUtil.format("{}_{}", infoEntity.getPlatformOrderId(), infoEntity.getSalesRecordNumber()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        orderMongoDTO.setDownloadStatus(1);
        List<GyyOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_GYY_ORDER, GyyOrderEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (GyyOrderEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    /**
     * 请求管易云订单接口
     *
     * @param dto
     * @return
     */
    private List<GyyOrderEntity> pullDate(RequestDTO dto){
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime().minusMinutes(10);
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        return GyyApiUtils.querySalesList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime, Boolean.FALSE);
    }

    /**
     * 解析订单数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/

    public BiOrderInfoEntity initOrderInfoEntity(GyyOrderEntity gyyOrderEntity){
        // 查询店铺信息
        if (assertOrgIsVijim(gyyOrderEntity.getShopName())){
            return null;
        }
        BiOrderInfoEntity biOrderInfoEntity = new BiOrderInfoEntity();
        //源平台订单id
        biOrderInfoEntity.setPlatformOrderId(gyyOrderEntity.getPlatformCode());
        //订单状态 2.配货中 3.已发货 4.已完成 5.已作废 6.退货 7.退款
        int orderState = 4;
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
        biOrderInfoEntity.setOrderStatus(orderState);
        //买家账号
        biOrderInfoEntity.setBuyerUserId(gyyOrderEntity.getVipCode());
        //买家姓名
        biOrderInfoEntity.setBuyerName(gyyOrderEntity.getVipName());
        //店铺编号
        biOrderInfoEntity.setShopNo(gyyOrderEntity.getShopCode());
        //店铺名称
        biOrderInfoEntity.setShopName(gyyOrderEntity.getShopName());
        //待审核订单 1.否 2.是
        biOrderInfoEntity.setCanSend(gyyOrderEntity.getApprove() ? 2 : 1);
        //是否退款 1.退款 2.非退款
        biOrderInfoEntity.setIsRefund((refundState.equals(1) || refundState.equals(2))? 1 : 2);
        //是否退货 1.退货 2.非退货
        biOrderInfoEntity.setIsReturned(2);
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        //订单付款时间
        if (!"null".equalsIgnoreCase(gyyOrderEntity.getPaytime()) && StrUtil.isNotBlank(gyyOrderEntity.getPaytime())) {
            biOrderInfoEntity.setPaidTime(LocalDateTime.parse(gyyOrderEntity.getPaytime(), sdf));
        }
        //平台订单时间
        if (!"null".equalsIgnoreCase(gyyOrderEntity.getDealtime()) && StrUtil.isNotBlank(gyyOrderEntity.getDealtime())) {
            biOrderInfoEntity.setPlatformCreateTime(LocalDateTime.parse(gyyOrderEntity.getDealtime(), sdf));
        }
        //平台交易号
        biOrderInfoEntity.setSalesRecordNumber(gyyOrderEntity.getCode());
        //平台的订单状态
        biOrderInfoEntity.setPlatformOrderStatus(gyyOrderEntity.getPlatformTradingState());
        //订单来源平台
        biOrderInfoEntity.setSourcePlatform(gyyOrderEntity.getFromTypeName());
        biOrderInfoEntity.setOrderTypeName(gyyOrderEntity.getOrderTypeName());
        if (StringUtils.isNotBlank(gyyOrderEntity.getReceiverArea())) {
            String[] split = gyyOrderEntity.getReceiverArea().split("-");
            if (split.length >= 1) {
                //买家省份
                biOrderInfoEntity.setProvince(split[0]);
            }
            if (split.length >= 2) {
                //买家城市
                biOrderInfoEntity.setCity(split[1]);
            }
            if (split.length >= 3) {
                //所属区域
                biOrderInfoEntity.setDistrict(split[2]);
            }
        }
        //买家地址1
        biOrderInfoEntity.setManStreet(gyyOrderEntity.getReceiverAddress());
        //买家地址2
        biOrderInfoEntity.setSecondStreet("");
        //交易关闭时间
        biOrderInfoEntity.setCloseDate(null);
        //买家电话1
        biOrderInfoEntity.setManPhone(gyyOrderEntity.getReceiverMobile());
        //买家电话2
        biOrderInfoEntity.setSecondPhone(gyyOrderEntity.getReceiverPhone());
        //是否平台发货订单 1.否 2.是
        biOrderInfoEntity.setFbaFlag(0);
        //平台备注
        biOrderInfoEntity.setSellerMessage(gyyOrderEntity.getBuyerMemo());
        //币种
        biOrderInfoEntity.setCurrencyCode("CNY");
        //汇率
        biOrderInfoEntity.setCurrencyRate(BigDecimal.ONE);
        //商品总售价
        biOrderInfoEntity.setItemTotal(gyyOrderEntity.getAmount());
        //订单金额
        biOrderInfoEntity.setOrderFee(gyyOrderEntity.getPaymentAmount());
        //运费收入
        biOrderInfoEntity.setShippingFee(gyyOrderEntity.getPostFee());
        //平台费
        biOrderInfoEntity.setPlatformFee(BigDecimal.ZERO);
        //原始运费收入
        biOrderInfoEntity.setShippingTotalOrigin(gyyOrderEntity.getPostFee());
        List<DetailsBean> details = gyyOrderEntity.getDetails();
        BigDecimal costPrice = BigDecimal.ZERO;
        for (DetailsBean detail : details) {
            costPrice = costPrice.add(detail.getCostPrice());
        }
        //订单成本价
        biOrderInfoEntity.setOrderCost(costPrice);
        //商品总成本
        biOrderInfoEntity.setItemTotalCost(costPrice);
        //商品原始总售价
        biOrderInfoEntity.setItemTotalOrigin(gyyOrderEntity.getPaymentAmount());
        //补贴金额
        biOrderInfoEntity.setSubsidyAmount(gyyOrderEntity.getDiscountFee());
        //国家英文名称
        biOrderInfoEntity.setCountryNameEn(CountrySiteEnum.CHINA.getCurrencyCode());
        //国家中文名称
        biOrderInfoEntity.setCountryNameCn(CountrySiteEnum.CHINA.getCurrencyName());
        //平台标识
        biOrderInfoEntity.setPlatformSign(PlatformEnum.GYY.getDesc());
        biOrderInfoEntity.setCreateTime(LocalDateTime.now());
        List<BiOrderItemSplitEntity> dmpOrderItemEntities = initOrderItem(gyyOrderEntity, orderState);
        if (CollectionUtil.isEmpty(dmpOrderItemEntities)){
            return null;
        }
        biOrderInfoEntity.setSite("CN");
        biOrderInfoEntity.setItemList(dmpOrderItemEntities);
        return biOrderInfoEntity;
    }

    public static boolean assertOrgIsVijim(String shopCode) {
        return StrUtil.isNotBlank(shopCode) && (shopCode.contains("小隼") || shopCode.contains("优至胜"));
//        DmpShopInfoEntity shopInfo = dmpShopInfoService.getShopByShopNo(shopCode, PlatformEnum.GYY.getDesc());
//        return null != shopInfo && (ApiKingdeeOrganizationEnum.ORGANIZATION_XX.getCode().equals(shopInfo.getUseOrgId().toString()) || ApiKingdeeOrganizationEnum.ORGANIZATION_YZS.getCode().equals(shopInfo.getUseOrgId().toString()));
    }

    /**
     * 解析订单商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public List<BiOrderItemSplitEntity> initOrderItem(GyyOrderEntity gyyOrderEntity, Integer orderState) {
        List<DetailsBean> orderItem = gyyOrderEntity.getDetails();
        String warehouseCode = gyyOrderEntity.getWarehouseCode();
        List<BiOrderItemSplitEntity> orderItemList = new ArrayList<>();
        Map<String, Integer> skuCountMap = new HashMap<>();
        for (DetailsBean detailsBean : orderItem) {
            if(StrUtil.isBlank(detailsBean.getItemCode())){
                continue;
            }
            BiOrderItemSplitEntity biOrderItemSplitEntity = new BiOrderItemSplitEntity();
            //商品id
            biOrderItemSplitEntity.setItemId(detailsBean.getItemCode());
            //平台sku
            biOrderItemSplitEntity.setPlatformSku(detailsBean.getSkuCode());
            //平台原始sku数量
            biOrderItemSplitEntity.setPlatformQuantity(detailsBean.getDeliveringQty());
            //商品名称
            biOrderItemSplitEntity.setItemName(detailsBean.getItemName());
            biOrderItemSplitEntity.setShippingFee(detailsBean.getPostFee());
            //商品图片
            biOrderItemSplitEntity.setPictureUrl("");
            //商品成本价
            biOrderItemSplitEntity.setCostPrice(detailsBean.getCostPrice());
            //商品原始售价
            biOrderItemSplitEntity.setSellPriceOrigin(detailsBean.getPrice());
            //商品售价
            biOrderItemSplitEntity.setSellPrice(detailsBean.getPrice());
            //商品数量
            biOrderItemSplitEntity.setQuantity(detailsBean.getQty());
            //商品单位
            biOrderItemSplitEntity.setProductUnit(detailsBean.getItemUnitName());
            //是否是赠品 1. 是 2. 否
            biOrderItemSplitEntity.setIsGift(detailsBean.getIsGift() ? 1 : 2);
            //缺货订单 0.正在计算是否缺货 1.有货 2.缺货 3.已补货
            biOrderItemSplitEntity.setHasGoods(0);
            //是否是组合商品 1.组合 2非组合
            biOrderItemSplitEntity.setIsCombo(0);
            //订单商品备注
            biOrderItemSplitEntity.setItemRemark(detailsBean.getSkuNote());
            //商品多属性
            if (StringUtils.isNotBlank(detailsBean.getPlatformSkuName())) {
                biOrderItemSplitEntity.setSpecifics(detailsBean.getPlatformSkuName());
            }
            //商品状态 1：未付款 2：未发货 3：已发货 4：已作废
            biOrderItemSplitEntity.setStatus(String.valueOf(orderState));
            //商品仓位
            biOrderItemSplitEntity.setStockGrid("");
            //sku
            biOrderItemSplitEntity.setSkuNo(detailsBean.getItemCode());
            //库存状态：1.自动创建 2.待开发 3.正常 4.清仓 5.停止销售
            biOrderItemSplitEntity.setStockStatus(0);
            //商品仓库编号
            biOrderItemSplitEntity.setStockWarehouseId(warehouseCode);
            //erp平台商品id
            String erpOrderItemId = gyyOrderEntity.getCode() + "_" + detailsBean.getItemCode();
            String skuNo = detailsBean.getItemCode();
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, skuNo, erpOrderItemId);
            biOrderItemSplitEntity.setErpOrderItemId(erpOrderItemId);
            //汇率
            biOrderItemSplitEntity.setCurrencyRate(BigDecimal.ONE);
            //折扣后金额
            biOrderItemSplitEntity.setAmountAfter(detailsBean.getAmountAfter().add(detailsBean.getPostFee()));
            orderItemList.add(biOrderItemSplitEntity);
        }
        return orderItemList;
    }
}
