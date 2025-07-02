package com.erp.server.dmp.pull.service.kingdee;

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
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.core.constant.CommonConstants;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.KingdeeOutStockDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.BiDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.BiDeliveryDetailItemEntity;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.dmp.kingdee.item.KingdeeDeliveryDetailItemEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 金蝶云星空销售出库单详情
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.SAL_OUTSTOCK)
public class KingdeeDeliveryDetailServiceImpl implements IReportSaveService<KingdeeDeliveryDetailEntity> {

    @Resource
    private MongoService mongoService;

    @Autowired
    private MQProducerService mqProducerService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private CfgSettingService settingService;

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<KingdeeDeliveryDetailEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取金蝶发货订单列表数据为空 entityList.size = 0 ");
            return;
        }
        List<KingdeeDeliveryDetailEntity> insertList = new ArrayList<>();
        List<KingdeeDeliveryDetailEntity> pushToMqList = new ArrayList<>();
        for (KingdeeDeliveryDetailEntity entity : entityList) {
            //从mongo里面查询
            KingdeeOutStockDTO outStockDTO = new KingdeeOutStockDTO(entity.getFBillNo());
            List<KingdeeDeliveryDetailEntity> mongoData = mongoService.findMongoData(outStockDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_DELIVERY_DETAIL, KingdeeDeliveryDetailEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            //当没有查询到的时候 就添加
            if (CollectionUtil.isEmpty(mongoData)) {
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            KingdeeDeliveryDetailEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            //过滤无效数据
            if (!mongoDatum.getIsValid()) {
                continue;
            }

            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_DELIVERY_DETAIL, KingdeeDeliveryDetailEntity.class);
        }

        if (CollectionUtil.isNotEmpty(insertList)) {
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_KINGDEE_DELIVERY_DETAIL);
        }
        if (CollectionUtil.isEmpty(pushToMqList)) {
            log.warn("金蝶发货订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        /**
         * 获取到想要同步的销售出库单列表
         */
        List<KingdeeDeliveryDetailEntity> wantToMqList = listWantToMqSoOutstock(pushToMqList);
        if (CollectionUtils.isEmpty(wantToMqList)) {
            log.warn("金蝶发货订单,推送消息的没有数据 dto>>>>>>{}", JSONUtil.toJsonStr(dto));
        }

        // 异步推送B2C销售出库单到MQ
        wantToMqList.forEach(obj -> {
           if (StringUtils.isNotBlank(obj.getFDate()) && !obj.getFDate().equals("null")) {
                if (LocalDateTime.parse(obj.getFDate()).toLocalDate().compareTo(LocalDate.parse("2023-07-06")) > 0) {
                    mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_B2C_SO_OUTSTOCK_TAG.getName(), obj, obj.getFBillNo());
                }
            }
        });
        //判断是否需要推送MQ
        KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName());
        if (kingdeeApiUtils.notNeedPushMQ(dto.getJobTaskDTO().getLastTime())){
            pushToMqList = pushToMqList.stream().filter(e -> !CommonConstants.SYSTEM.equals(e.getDataSources())).collect(Collectors.toList());
        }
        // 构造订单结构
        List<BiDeliveryDetailInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        List<BiDeliveryDetailInfoEntity> biDeliveryDetailInfoEntityList = entityToMqlist.stream().peek(msg -> {
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_DELIVERY_ORDER_TAG.getName(),
                    msg, msg.getBillNo());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("kingdeeDelivery发送数据为：{}" , JSON.toJSONString(biDeliveryDetailInfoEntityList));
    }


    /**
     * 获取到想要同步的销售出库单的列表
     *
     * @param pushToMqList
     * @return java.util.List<com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity>
     * @author yl
     * @date 2023-06-28 16:52
     */
    private List<KingdeeDeliveryDetailEntity> listWantToMqSoOutstock(List<KingdeeDeliveryDetailEntity> pushToMqList) {
        List<KingdeeDeliveryDetailEntity> wantList = new ArrayList<>(pushToMqList.size());
        String dataSources = CommonConstants.SYSTEM;
        for (KingdeeDeliveryDetailEntity item : pushToMqList) {
            // 跳过非唯迹订单 和本身的自研ERP
            if (StrUtil.isEmpty(item.getFSaleOrgId()) ||
                    ApiKingdeeOrganizationEnum.ORGANIZATION_YZS.getCode().equals(item.getFSaleOrgId()) ||
                    ApiKingdeeOrganizationEnum.ORGANIZATION_XX.getCode().equals(item.getFSaleOrgId()) ||
                    dataSources.equals(item.getDataSources())
            ) {
                continue;
            }

            wantList.add(item);
        }
        return wantList;
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<KingdeeDeliveryDetailEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_KINGDEE_DELIVERY_DETAIL, KingdeeDeliveryDetailEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (KingdeeDeliveryDetailEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(KingdeeDeliveryDetailEntity mongoDatum) {
        BiDeliveryDetailInfoEntity deliveryDetailInfo = initOrderInfoEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
        if (null == deliveryDetailInfo) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_DELIVERY_DETAIL, KingdeeDeliveryDetailEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_DELIVERY_DETAIL, KingdeeDeliveryDetailEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_DELIVERY_ORDER_TAG.getName(),
                deliveryDetailInfo, deliveryDetailInfo.getBillNo());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求金蝶云星空出库详情接口
     *
     * @param dto
     * @return
     */
    public List<KingdeeDeliveryDetailEntity> pullDate(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        LinkedList<String> queryFilters = new LinkedList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        //审核状态
        queryFilters.add(StrUtil.format(" FDocumentStatus in ({})", "'C'"));
        // 过滤组织内订单
        queryFilters.add(StrUtil.format(" FISGENFORIOS = {}", "0"));
        queryFilters.add(StrUtil.format(" (FApproveDate >= '{}' and FApproveDate < '{}')",sdf.format(lastTime.minusMinutes(8)),sdf.format(nextTime)));
        String filterStr = String.join(" and ", queryFilters);
        log.info("拉取金蝶条件为>>>>>>>>>>{}", filterStr);

        String fieldKeys = "FID,FBillTypeID,FBillTypeID.FName,FBillNo,FSoOrDerNo,FDate,FSaleOrgId,FSaleOrgId.FName,FCarriageNO,FStockerID.FNumber,FStockerID.FName," +
                "FCustomerID,FCustomerID.FName,FCustomerID.FNumber,FSaleDeptID.FName,FSalesManID,FSalesManID.FName,FSalesManID.FNumber,FReceiverID.FName," +
                "FTransferBizType.FName,F_ulz_BaseProperty2,F_ulz_BaseProperty2.FNumber,FLinkPhone,FLinkMan,FBussinessType,FDocumentStatus," +
                "FNote,FReceiveAddress,FCreatorId.FName,FCreateDate,FModifierId.FName,FModifyDate,FApproverID.FName," +
                "FApproveDate,FCancelStatus,FGYDATE,FLogisticsNos,F_ulz_Text3,FSettleCurrID.FCode,FExchangeRate,FISGENFORIOS," +
                "FEntity_FENTRYID,FBillAllAmount,FBillAllAmount_LC,FAllAmount,FAllAmount_LC,FAmount_LC,FTaxAmount,FTaxAmount_LC,FBillTaxAmount,FEntryTaxAmount," +
                "FSrcBillNo,FCustMatName,F_ulz_BaseProperty1,FMaterialID,FMaterialID.FNumber,FMaterialID.FName,FStockLocID," +
                "FBarcode,FMateriaModel,FMateriaType,FRealQty,FUnitID.FName,FPrice,FIsFree,FArrivalStatus,FArrivalDate," +
                "FAmount,FStockStatusID,FStockStatusID.FName,FStockID.FName,FStockID.FNumber,F_ulz_Text1,FEntryCostAmount,FEntrynote,FSrcType,FTaxPrice," +
                "FCostPrice,FCostAmount_LC,FSalCostPrice,F_ULZ_data_sources,FETHIRDBILLNO";

        Boolean dataSign = true;
        //当前页数
        Integer pageIndex = 0;

        //每次最多获取100条
        Integer pageSize = 10000;
        List<Map<String, Object>> resultAll = new ArrayList<>();
        while (dataSign) {
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName());
            List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            XxlJobHelper.log("获取金蝶发货数据第[{}]页 有{}条记录", pageIndex, pageSize);
            if (result.size() < pageSize) {
                dataSign = false;
            }
            if (CollectionUtil.isEmpty(result)) {
                break;
            }
            resultAll.addAll(result);
            pageIndex++;
        }
        List<KingdeeDeliveryDetailEntity> entityList = resultAll.stream()
                .map(shopEntity -> BeanUtil.toBean(shopEntity, KingdeeDeliveryDetailEntity.class))
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                // key 映射函数
                                KingdeeDeliveryDetailEntity::getFBillNo,
                                // value 映射函数
                                Function.identity(),
                                // 如果有重复，保留第一个
                                (existing, replacement) -> existing,
                                // 使用 LinkedHashMap 来保持插入顺序
                                LinkedHashMap::new
                        ),
                        // 从 Map 的值集合创建一个新的 ArrayList
                        map -> new ArrayList<>(map.values())
                ));
        // 金蝶发货单主数据
//        List<KingdeeDeliveryDetailEntity> entityList = resultAll.stream().map(shopEntity ->
//                BeanUtil.toBean(shopEntity, KingdeeDeliveryDetailEntity.class))
//                .distinct().collect(Collectors.toList());
        // 金蝶发货单明细数据拆单
        Map<String, List<KingdeeDeliveryDetailItemEntity>> itemMap = resultAll.stream().map(entity ->
                BeanUtil.toBean(entity, KingdeeDeliveryDetailItemEntity.class)).distinct()
                .collect(Collectors.groupingBy(KingdeeDeliveryDetailItemEntity::getFBillNo));
        // 金蝶发货单主数据关联明细数据
        List<KingdeeDeliveryDetailEntity> kingdeeDeliveryDetailEntityList = entityList.stream().peek(m -> m.setKingdeeOutStockItemEntityList(itemMap.get(m.getFBillNo())))
                .collect(Collectors.toList());
        log.debug("KingdeeDeliveryDetailEntity发送数据为：{}" , JSON.toJSONString(kingdeeDeliveryDetailEntityList));
        return entityList;
    }

    /**
     * 解析出库订单数据
     **/
    private BiDeliveryDetailInfoEntity initOrderInfoEntity(KingdeeDeliveryDetailEntity kingdeeOutStockEntity) {
        List<String> list = Arrays.asList("020", "021", "3003");
        Map<SettingEnum, String> map = settingService.getMap(SettingEnum.KD_TO_ERP_FILTER);
        // 标准销售出库单
        String fBillTypeID = map.get(SettingEnum.KD_TO_ERP_DELIVERY_FILTER_BILL_TYPE);
        //020 B2B线下国内 021 B2B线下国外 3003 官网线上
        String platformTypeCode = map.get(SettingEnum.KD_TO_ERP_DELIVERY_FILTER_PLATFORM_TYPE_CODE);
        if (StrUtil.isNotBlank(platformTypeCode)) {
            list = Arrays.asList(platformTypeCode.split(","));
        }
        // 跳过非唯迹订单
        if (StrUtil.isEmpty(kingdeeOutStockEntity.getFSaleOrgId()) ||
                ApiKingdeeOrganizationEnum.ORGANIZATION_YZS.getCode().equals(kingdeeOutStockEntity.getFSaleOrgId()) ||
                ApiKingdeeOrganizationEnum.ORGANIZATION_XX.getCode().equals(kingdeeOutStockEntity.getFSaleOrgId()) ||
                ObjectUtil.equals(fBillTypeID, kingdeeOutStockEntity.getFBillTypeID()) ||
                !list.contains(kingdeeOutStockEntity.getF_ulz_BaseProperty2Code())
        ) {
            return null;
        }
        // 跳过 跨组织结算自动生成的单据
        if (ObjectUtil.isNotEmpty(kingdeeOutStockEntity.getFIsGenForIos()) && kingdeeOutStockEntity.getFIsGenForIos()) {
            return null;
        }
        BiDeliveryDetailInfoEntity deliveryDetailInfoEntity = new BiDeliveryDetailInfoEntity();
        //单据编号
        deliveryDetailInfoEntity.setBillNo(kingdeeOutStockEntity.getFBillNo());
        //订单编号
        List<KingdeeDeliveryDetailItemEntity> itemEntityList = kingdeeOutStockEntity.getKingdeeOutStockItemEntityList();
        if (CollectionUtil.isNotEmpty(itemEntityList)) {
            deliveryDetailInfoEntity.setOrderNo(itemEntityList.get(0).getFSrcBillNo());
        }
        deliveryDetailInfoEntity.setPlatformOrderId(kingdeeOutStockEntity.getFBillNo());
        //物流单号
        deliveryDetailInfoEntity.setLogisticsNo(kingdeeOutStockEntity.getFLogisticsNos());
        //客户名称
        deliveryDetailInfoEntity.setCustomerName(kingdeeOutStockEntity.getFCustomerName());
        //平台名称
        if (StringUtils.isNotBlank(kingdeeOutStockEntity.getF_ulz_BaseProperty2()) && !kingdeeOutStockEntity.getF_ulz_BaseProperty2().equals("null")) {
            deliveryDetailInfoEntity.setPlatformName(kingdeeOutStockEntity.getF_ulz_BaseProperty2());
        } else {
            deliveryDetailInfoEntity.setPlatformName("");
        }
        //店铺编号
        deliveryDetailInfoEntity.setShopNo("B2B");
        //店铺名称
        deliveryDetailInfoEntity.setShopName("B2B");
        List<KingdeeDeliveryDetailItemEntity> kingdeeOutStockItemEntityList = kingdeeOutStockEntity.getKingdeeOutStockItemEntityList();
        BigDecimal orderTotalCost = BigDecimal.ZERO;
        BigDecimal itemTotalCost = BigDecimal.ZERO;
        for (KingdeeDeliveryDetailItemEntity itemEntity : kingdeeOutStockItemEntityList) {
            orderTotalCost = orderTotalCost.add(itemEntity.getFAllAmount_LC());
            itemTotalCost = itemTotalCost.add(itemEntity.getFCostPrice());
        }
        //订单成本价
        deliveryDetailInfoEntity.setItemTotalCost(itemTotalCost);
        //订单总价
        deliveryDetailInfoEntity.setOrderTotalCost(orderTotalCost);
        //国家英文名称
        deliveryDetailInfoEntity.setCountryNameEn("");
        //国家中文名称
        deliveryDetailInfoEntity.setCountryNameCn("");
        //买家城市
        deliveryDetailInfoEntity.setCity("");
        //买家省份
        deliveryDetailInfoEntity.setProvince("");
        //买家地址1
        deliveryDetailInfoEntity.setManStreet(kingdeeOutStockEntity.getFReceiveAddress());
        //买家地址2
        deliveryDetailInfoEntity.setSecondStreet("");
        //所属区域
        deliveryDetailInfoEntity.setDistrict("");
        //币种
        deliveryDetailInfoEntity.setCurrencyCode(kingdeeOutStockEntity.getFSettleCurrCode());
        //汇率
        deliveryDetailInfoEntity.setCurrencyRate(new BigDecimal(kingdeeOutStockEntity.getFExchangeRate()));
        //运费
        deliveryDetailInfoEntity.setShippingFee(BigDecimal.ZERO);
        //补贴金额
        deliveryDetailInfoEntity.setSubsidyAmount(BigDecimal.ZERO);
        //销售部门
        deliveryDetailInfoEntity.setSaleDeptName(kingdeeOutStockEntity.getFSaleDeptName());
        //销售员编号
        deliveryDetailInfoEntity.setSalesManId(kingdeeOutStockEntity.getFSalesManID());
        //销售员名称
        deliveryDetailInfoEntity.setSalesManName(kingdeeOutStockEntity.getFSalesManName());
        Integer status = 1;
        if (kingdeeOutStockEntity.getFCancelStatus().equals("C")) {
            status = 2;
        }
        if (kingdeeOutStockEntity.getFDocumentStatus().equals("C")) {
            status = 1;
        }
        //状态 1.已发货 2..已作废
        deliveryDetailInfoEntity.setStatus(status);
        //平台单据审核时间
        if (StringUtils.isNotBlank(kingdeeOutStockEntity.getFApproveDate()) && !kingdeeOutStockEntity.getFApproveDate().equals("null")) {
            deliveryDetailInfoEntity.setPlatformApproveTime(LocalDateTime.parse(kingdeeOutStockEntity.getFApproveDate()));
        }
        //平台单据创建时间
        if (StringUtils.isNotBlank(kingdeeOutStockEntity.getFCreateDate()) && !kingdeeOutStockEntity.getFCreateDate().equals("null")) {
            deliveryDetailInfoEntity.setPlatformCreateTime(LocalDateTime.parse(kingdeeOutStockEntity.getFCreateDate()));
        }
        //平台单据修改时间
        if (StringUtils.isNotBlank(kingdeeOutStockEntity.getFModifyDate()) && !kingdeeOutStockEntity.getFModifyDate().equals("null")) {
            deliveryDetailInfoEntity.setPlatformUpdateTime(LocalDateTime.parse(kingdeeOutStockEntity.getFModifyDate()));
        }
        //发货时间
        if (StringUtils.isNotBlank(kingdeeOutStockEntity.getFDate()) && !kingdeeOutStockEntity.getFDate().equals("null")) {
            deliveryDetailInfoEntity.setDeliveryDate(LocalDateTime.parse(kingdeeOutStockEntity.getFDate()));
        }
        //备注
        deliveryDetailInfoEntity.setRemark(kingdeeOutStockEntity.getF_ulz_Text3());
        //平台标识
        deliveryDetailInfoEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
        //企业Id
        deliveryDetailInfoEntity.setCompanyId(kingdeeOutStockEntity.getFSaleOrgId());
        //企业名称
        deliveryDetailInfoEntity.setCompanyName(kingdeeOutStockEntity.getFSaleOrgName());
        deliveryDetailInfoEntity.setCreateTime(LocalDateTime.now());
        deliveryDetailInfoEntity.setDetails(initOrderItem(kingdeeOutStockEntity));
        return deliveryDetailInfoEntity;
    }

    /**
     * 解析出库详情商品数据
     **/
    private List<BiDeliveryDetailItemEntity> initOrderItem(KingdeeDeliveryDetailEntity kingdeeOutStockEntity) {
        List<BiDeliveryDetailItemEntity> orderItemList = new ArrayList<>();
        kingdeeOutStockEntity.getKingdeeOutStockItemEntityList().stream()
                .forEach(itemEntity -> {
                    BiDeliveryDetailItemEntity dmpReturnOrderItemEntity = new BiDeliveryDetailItemEntity();
                    //商品id
                    dmpReturnOrderItemEntity.setItemId(itemEntity.getFMaterialID());
                    dmpReturnOrderItemEntity.setSaleOrderNo(itemEntity.getFSrcBillNo());
                    dmpReturnOrderItemEntity.setPlatformOrderId(itemEntity.getFSrcBillNo());
                    //平台sku
                    dmpReturnOrderItemEntity.setPlatformSku(itemEntity.getF_ulz_BaseProperty1());
                    //商品sku编号
                    dmpReturnOrderItemEntity.setSkuNo(itemEntity.getFMaterialNumber());
                    //商品名称
                    dmpReturnOrderItemEntity.setItemName(itemEntity.getFMaterialName());
                    //商品成本价
                    dmpReturnOrderItemEntity.setCostPrice(itemEntity.getFCostPrice());
                    //商品售价
                    dmpReturnOrderItemEntity.setSellPrice(new BigDecimal(itemEntity.getFPrice()));
                    //商品数量
                    dmpReturnOrderItemEntity.setQuantity(Double.valueOf(itemEntity.getFRealQty()).intValue());
                    dmpReturnOrderItemEntity.setAmount(itemEntity.getFAllAmount_LC());
                    //商品单位
                    dmpReturnOrderItemEntity.setProductUnit(itemEntity.getFUnitName());
                    //是否是赠品 1. 是 2. 否
                    if (Boolean.valueOf(itemEntity.getFIsFree())) {
                        dmpReturnOrderItemEntity.setIsGift(1);
                    } else {
                        dmpReturnOrderItemEntity.setIsGift(2);
                    }
                    //属性
                    dmpReturnOrderItemEntity.setSpecifics(itemEntity.getFMateriaModel());
                    //订单商品备注
                    dmpReturnOrderItemEntity.setItemRemark(itemEntity.getFEntryNote());
                    //仓库
                    dmpReturnOrderItemEntity.setStockName(itemEntity.getFStockName());
                    //库位
                    dmpReturnOrderItemEntity.setWarehouseLocation(itemEntity.getF_ulz_Text1());
                    orderItemList.add(dmpReturnOrderItemEntity);
                });
        return orderItemList;
    }
}
