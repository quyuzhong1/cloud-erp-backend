package com.erp.server.dmp.pull.service.gyy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.constant.RocketMqTopic;
import com.common.core.enums.CountrySiteEnum;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.constant.RocketMqTagEnum;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.DmpDeliveryDetailItemEntity;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.gyy.GyyDeliveryDetailEntity;
import com.erp.model.dmp.gyy.bean.DeliveryDetailsBean;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpDeliveryDetailInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpDeliveryDetailItemService;
import com.erp.server.dmp.service.mq.MQProducerService;
import com.erp.server.dmp.utils.GyyApiUtils;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 管易云出库详情
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.GY_ERP_TRADE_DELIVERY_GET)
public class GyyDeliveryDetailServiceImpl implements IReportSaveService<GyyDeliveryDetailEntity> {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpDeliveryDetailInfoService dmpDeliveryDetailInfoService;

    @Resource
    private DmpDeliveryDetailItemService dmpDeliveryDetailItemService;

    @Autowired
    private MQProducerService<DmpDeliveryDetailInfoEntity> rocketMQTemplate;

    @Resource
    @Qualifier("gyyDeliveryDetailServiceImpl")
    private IReportSaveService reportSaveService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<GyyDeliveryDetailEntity> gyyDeliveryDetailEntityList = pullDate(dto);
        if (CollectionUtil.isEmpty(gyyDeliveryDetailEntityList)) {
            log.info("拉取管易发货订单列表数据为空 gyyDeliveryDetailEntityList.size = 0 ");
            return;
        }
        log.info("拉取管易发货订单列表数据 gyyDeliveryDetailEntityList.size = {} ", gyyDeliveryDetailEntityList.size());
        XxlJobHelper.log("拉取管易发货订单列表数据 gyyDeliveryDetailEntityList.size = {} ", gyyDeliveryDetailEntityList.size());
        List<GyyDeliveryDetailEntity> insertList = new ArrayList<>();
        List<GyyDeliveryDetailEntity> pushToMqList = new ArrayList<>();
        for (GyyDeliveryDetailEntity entity : gyyDeliveryDetailEntityList) {
            OrderMongoDTO orderMongoDTO = new OrderMongoDTO(entity.getPlatformCode(), entity.getCode());
            List<GyyDeliveryDetailEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_DELIVERY_DETAIL, GyyDeliveryDetailEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            GyyDeliveryDetailEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                log.warn("管易发货订单 mongo数据无变化无需更新 entity={}", JSONUtil.toJsonStr(entity));
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_GYY_DELIVERY_DETAIL, GyyDeliveryDetailEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_GYY_DELIVERY_DETAIL);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("管易发货订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<DmpDeliveryDetailInfoEntity> mabangToMqlist = pushToMqList.parallelStream()
                .map(this::initOrderInfoEntity)
                .collect(Collectors.toList());

        // 异步推送到MQ
        mabangToMqlist.stream().peek(msg ->
                        rocketMQTemplate.asyncClassMsg(RocketMqTopic.DMP_TOPIC, RocketMqTagEnum.GYY_DELIVERY_ORDER_TAG.getName(),
                                msg, msg.getBillNo()))
                .collect(Collectors.toList());
    }

    /**
     * 请求管易云退款信息接口
     *
     * @param dto
     * @return
     */
    private List<GyyDeliveryDetailEntity> pullDate(RequestDTO dto) throws Exception {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        boolean flag = Objects.equals(dto.getPlatformApiEnum(), PlatformApiEnum.GY_ERP_TRADE_DELIVERYS_HISTORY_GET);
        return GyyApiUtils.queryDeliveryList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime, flag);
    }

    /**
     * 解析订单数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public DmpDeliveryDetailInfoEntity initOrderInfoEntity(GyyDeliveryDetailEntity gyyDeliveryDetailEntity){
        DmpDeliveryDetailInfoEntity deliveryDetailInfoEntity = new DmpDeliveryDetailInfoEntity();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        //单据编号
        deliveryDetailInfoEntity.setBillNo(gyyDeliveryDetailEntity.getCode());

        //订单编号
        deliveryDetailInfoEntity.setOrderNo(gyyDeliveryDetailEntity.getPlatformCode());
        //物流单号
        deliveryDetailInfoEntity.setLogisticsNo(gyyDeliveryDetailEntity.getExpressNo());
        //客户名称
        deliveryDetailInfoEntity.setCustomerName(gyyDeliveryDetailEntity.getVipName());
        //平台名称
        deliveryDetailInfoEntity.setPlatformName("");
        //店铺编号
        deliveryDetailInfoEntity.setShopNo(gyyDeliveryDetailEntity.getShopCode());
        //店铺名称
        deliveryDetailInfoEntity.setShopName(gyyDeliveryDetailEntity.getShopName());
        String currencyCode = "";
        BigDecimal totalCostPrice = BigDecimal.ZERO;
        String BusinessmanName = "";
        for (DeliveryDetailsBean detail : gyyDeliveryDetailEntity.getDetails()) {
            totalCostPrice = totalCostPrice.add(detail.getTotalCostPrice());
            currencyCode = detail.getCurrencyCode();
            BusinessmanName = detail.getBusinessmanName();
        }
        //订单成本价
        deliveryDetailInfoEntity.setItemTotalCost(totalCostPrice);
        //订单总价
        deliveryDetailInfoEntity.setOrderTotalCost(gyyDeliveryDetailEntity.getAmount());
        //国家英文名称
        deliveryDetailInfoEntity.setCountryNameEn(CountrySiteEnum.CHINA.getCurrencyCode());
        //国家中文名称
        deliveryDetailInfoEntity.setCountryNameCn(CountrySiteEnum.CHINA.getCurrencyName());
        if (StringUtils.isNotBlank(gyyDeliveryDetailEntity.getAreaName())) {
            String[] split = gyyDeliveryDetailEntity.getAreaName().split("-");
            if (split.length >= 1) {
                //买家省份
                deliveryDetailInfoEntity.setProvince(split[0]);
            }
            if (split.length >= 2) {
                //买家城市
                deliveryDetailInfoEntity.setCity(split[1]);
            }
            if (split.length >= 3) {
                //所属区域
                deliveryDetailInfoEntity.setDistrict(split[2]);
            }
        }
        //买家地址1
        deliveryDetailInfoEntity.setManStreet(gyyDeliveryDetailEntity.getReceiverAddress());
        //买家地址2
        deliveryDetailInfoEntity.setSecondStreet("");
        //币种
        deliveryDetailInfoEntity.setCurrencyCode(currencyCode);
        //汇率
        deliveryDetailInfoEntity.setCurrencyRate(BigDecimal.ONE);
        //运费
        deliveryDetailInfoEntity.setShippingFee(gyyDeliveryDetailEntity.getPostFee());
        //补贴金额
        deliveryDetailInfoEntity.setSubsidyAmount(BigDecimal.ZERO);
        //销售部门
        deliveryDetailInfoEntity.setSaleDeptName("");
        //销售员编号
        deliveryDetailInfoEntity.setSalesManId("");
        //销售员名称
        deliveryDetailInfoEntity.setSalesManName(BusinessmanName);
        Integer status = gyyDeliveryDetailEntity.getCancel() ? 2 : 1;
        //状态 1.已发货 2..已作废
        deliveryDetailInfoEntity.setStatus(status);
        //平台单据审核时间
        deliveryDetailInfoEntity.setPlatformApproveTime(null);
        //平台单据创建时间
        if (StringUtils.isNotBlank(gyyDeliveryDetailEntity.getCreateDate()) && !gyyDeliveryDetailEntity.getCreateDate().equals("null")) {
            deliveryDetailInfoEntity.setPlatformCreateTime(LocalDateTime.parse(gyyDeliveryDetailEntity.getCreateDate(), sdf));
        }
        //平台单据修改时间
        if (StringUtils.isNotBlank(gyyDeliveryDetailEntity.getModifyDate()) && !gyyDeliveryDetailEntity.getModifyDate().equals("null")) {
            deliveryDetailInfoEntity.setPlatformUpdateTime(LocalDateTime.parse(gyyDeliveryDetailEntity.getModifyDate(), sdf));
        }
        //发货时间
        if (StringUtils.isNotBlank(gyyDeliveryDetailEntity.getDeliveryStatusInfo().getDeliveryDate()) && !gyyDeliveryDetailEntity.getDeliveryStatusInfo().getDeliveryDate().equals("null")) {
            deliveryDetailInfoEntity.setDeliveryDate(LocalDateTime.parse(gyyDeliveryDetailEntity.getDeliveryStatusInfo().getDeliveryDate(),sdf));
        }
        //备注
        deliveryDetailInfoEntity.setRemark(gyyDeliveryDetailEntity.getSellerMemo());
        //平台标识
        deliveryDetailInfoEntity.setPlatformSign(PlatformEnum.GYY.getDesc());
        //企业Id
        deliveryDetailInfoEntity.setCompanyId(ApiKingdeeOrganizationEnum.ORGANIZATION_WEIJI.getCode().toString());
        //企业名称
        deliveryDetailInfoEntity.setCompanyName(ApiKingdeeOrganizationEnum.ORGANIZATION_WEIJI.getName());
        deliveryDetailInfoEntity.setCreateTime(LocalDateTime.now());
        deliveryDetailInfoEntity.setDetails(initOrderItem(gyyDeliveryDetailEntity));
        return deliveryDetailInfoEntity;
    }
    /**
     *         //新增订单信息
     *         String deliveryDetailId = dmpDeliveryDetailInfoService.checkOrder(deliveryDetailInfoEntity);
     *         if (StringUtils.isNotBlank(deliveryDetailId)) {
     *             //新增订单商品信息
     *             analysisReturnOrderItem(gyyDeliveryDetailEntity, gyyDeliveryDetailEntity.getDetails(), deliveryDetailId);
     *         }
     */

    /**
     * 解析出库详情商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public List<DmpDeliveryDetailItemEntity> initOrderItem(GyyDeliveryDetailEntity gyyDeliveryDetailEntity) {
        List<DmpDeliveryDetailItemEntity> orderItemList = new ArrayList<>();
        gyyDeliveryDetailEntity.getDetails().stream().forEach(itemEntity -> {
            DmpDeliveryDetailItemEntity dmpReturnOrderItemEntity = new DmpDeliveryDetailItemEntity();
            //商品id
            dmpReturnOrderItemEntity.setItemId(itemEntity.getItemId());
            //平台sku
            dmpReturnOrderItemEntity.setPlatformSku(itemEntity.getSkuCode());
            //商品sku编号
            dmpReturnOrderItemEntity.setSkuNo(itemEntity.getItemCode());
            //商品名称
            dmpReturnOrderItemEntity.setItemName(itemEntity.getItemName());
            //商品成本价
            dmpReturnOrderItemEntity.setCostPrice(itemEntity.getTotalCostPrice());
            //商品单价
            dmpReturnOrderItemEntity.setSellPrice(itemEntity.getPrice());
            //商品数量
            dmpReturnOrderItemEntity.setQuantity(itemEntity.getQty().intValue());
            //商品总价
            dmpReturnOrderItemEntity.setAmount(itemEntity.getAmount());
            //商品单位
            dmpReturnOrderItemEntity.setProductUnit(itemEntity.getItemUnitName());
            //是否是赠品 1. 是 2. 否
            dmpReturnOrderItemEntity.setIsGift(itemEntity.getIsGift() == 0 ? 1 : 2);
            //属性
            dmpReturnOrderItemEntity.setSpecifics(itemEntity.getPlatformSkuName());
            //订单商品备注
            dmpReturnOrderItemEntity.setItemRemark(itemEntity.getMemo());
            //仓库
            dmpReturnOrderItemEntity.setStockName(gyyDeliveryDetailEntity.getWarehouseName());
            //库位
            dmpReturnOrderItemEntity.setWarehouseLocation(itemEntity.getLocationCode());
            orderItemList.add(dmpReturnOrderItemEntity);
        });
       return orderItemList;
    }

    /**
     *  checkOrderItem(orderItemList);
     */
    /**
     * 校验出库详情商品信息在中台是否存在，存在就修改不存在则新增
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    public void checkOrderItem(List<DmpDeliveryDetailItemEntity> orderItem, String deliveryDetailId) {
        dmpDeliveryDetailItemService.deleteDeliveryDetailItemByDetailId(deliveryDetailId);
        dmpDeliveryDetailItemService.batchAdd(orderItem);
    }
}
