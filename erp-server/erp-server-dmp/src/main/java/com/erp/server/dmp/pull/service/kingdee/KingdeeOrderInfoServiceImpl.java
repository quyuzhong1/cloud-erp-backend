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
import com.common.business.dto.JobTaskDTO;
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
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.entity.BiOrderItemSplitEntity;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.kingdee.KingdeeOrderEntity;
import com.erp.model.dmp.kingdee.item.KingdeeOrderItemEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.DmpErrorLogService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.erp.server.dmp.utils.MapCountUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 金蝶云星空订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.SAL_SALEORDER)
public class KingdeeOrderInfoServiceImpl implements IReportSaveService<KingdeeOrderEntity> {
    /**
     * 可用销售订单CODE
     */
    private static final List<String> ORDER_TYPES = new ArrayList<>(Arrays.asList("B2BXSDD", "XSDD01_SYS"));

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Autowired
    private MQProducerService<BiOrderInfoEntity> mqProducerService;
    @Resource
    private CfgSettingService cfgSettingService;

    public static void main(String[] args) {
        KingdeeOrderInfoServiceImpl kingdeeOrderInfoService = new KingdeeOrderInfoServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.SAL_SALEORDER.getTaskName());
        jobTaskDTO.setPlatformApiId("5");
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId("30");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.now().minusDays(5));
        jobTaskDTO.setNextTime(LocalDateTime.now());
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(PlatformApiEnum.SAL_SALEORDER);
        requestDTO.setJobTaskDTO(jobTaskDTO);
//        List<KingdeeOrderEntity> kingdeeOrderEntities = kingdeeOrderInfoService.pullDate(requestDTO);
        try {
            kingdeeOrderInfoService.pullDataSave(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        System.out.println(kingdeeOrderEntities);
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<KingdeeOrderEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取金蝶销售订单列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取金蝶销售订单列表数据 entityList.size = {} ", entityList.size());
        List<KingdeeOrderEntity> insertList = new ArrayList<>();
        List<KingdeeOrderEntity> pushToMqList = new ArrayList<>();
        for (KingdeeOrderEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByFIdAndBillNo(entity.getFBillNo());
            List<KingdeeOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER, KingdeeOrderEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if (CollectionUtil.isEmpty(mongoData)) {
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            KingdeeOrderEntity mongoDatum = mongoData.get(0);
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
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER, KingdeeOrderEntity.class);
        }
        if (CollectionUtil.isNotEmpty(insertList)) {
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER);
        }

        if (CollectionUtil.isEmpty(pushToMqList)) {
            log.warn("金蝶销售订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        //判断是否需要推送MQ
        KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils();
        if (kingdeeApiUtils.notNeedPushMQ(dto.getJobTaskDTO().getLastTime())){
            pushToMqList = pushToMqList.stream().filter(e -> !CommonConstants.B2BXSDD.equals(e.getFBillTypeCode())).collect(Collectors.toList());
        }
        // 构造订单结构
        List<BiOrderInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        List<BiOrderInfoEntity> biOrderInfoEntityList = entityToMqlist.stream().peek(msg -> {
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_SALE_ORDER_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getPlatformOrderId(), msg.getSalesRecordNumber()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("金蝶订单发送数据为：{}" , JSON.toJSONString(biOrderInfoEntityList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<KingdeeOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER, KingdeeOrderEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (KingdeeOrderEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(KingdeeOrderEntity mongoDatum) {
        BiOrderInfoEntity orderInfo = initOrderInfoEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
        if (null == orderInfo) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER, KingdeeOrderEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER, KingdeeOrderEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_SALE_ORDER_TAG.getName(),
                orderInfo, StrUtil.format("{}_{}", orderInfo.getPlatformOrderId(), orderInfo.getSalesRecordNumber()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求金蝶云星空订单接口
     *
     * @param dto
     * @return
     */
    public List<KingdeeOrderEntity> pullDate(RequestDTO dto) {
        List<KingdeeOrderEntity> infoArrayList = new ArrayList<>();
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        LinkedList<String> queryFilters = new LinkedList<>();
//            queryFilters.add(StrUtil.format("FBillNo ='{}'", "XSD-20230105-33831"));
        // 移除 订单类型过滤
//            queryFilters.add(String.format("fBillTypeID = '%s'", "eacb50844fc84a10b03d7b841f3a6278"));
        queryFilters.add(StrUtil.format("FDocumentStatus = '{}'", "C"));
        queryFilters.add(StrUtil.format(" (FApproveDate >= '{}' and FApproveDate < '{}')",sdf.format(lastTime.minusMinutes(8)),sdf.format(nextTime)));
        String filterStr = String.join(" and ",  queryFilters );

        String fieldKeys = "FID,FBillNo,FDate,FBillTypeId.FName,FBillTypeId.FNumber,FBillTypeId," +
                "FDocumentStatus,FCustId.FName,FCustId.FNumber,FSaleDeptId.FName,FSalerId.FName,FSalerId.FNumber,FReceiveAddress,FLinkMan,FLinkPhone," +
                "FApproverId.FName,FApproveDate,FCloseStatus,FCloseDate,FCancelStatus,FChangerId," +
                "FReceiveId.FName,FNote,FHeadDeliveryWay,FHEADLOCID,FCorrespondOrgId,FSaleGroupId," +
                "FChangeReason,FBusinessType,FReceiveContact,FChargeId,FCreatorId,FCreateDate,FModifierId,FModifierId.FName," +
                "FModifyDate,FSaleOrgId,FSaleOrgId.FName,FVersionNo,FSignStatus,FSOFrom,F_SK_Date,F_SHGJ1.FNumber,F_SHGJ1,FExchangeRate,FSettleCurrId.FCode," +
                "FDeliveryDate";

        Boolean dataSign = true;
        //当前页数
        Integer pageIndex = 0;

        //每次最多获取100条
        Integer pageSize = 10000;
        while (dataSign) {
            //"StartRow\":0,"+// 分页取数开始行索引，从0开始，例如每页10行数据，第2页开始是10，第3页开始是20
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName());
            List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            log.info("获取金蝶销售订单数据第[{}]页 有{}条记录", pageIndex, pageSize);
            if (result.size() < pageSize) {
                dataSign = false;
            }
            if (CollectionUtil.isEmpty(result)) {
                return Collections.emptyList();
            }
            List<KingdeeOrderEntity> entityList = result.stream().map(entity ->
                    BeanUtil.toBean(entity, KingdeeOrderEntity.class)).collect(Collectors.toList());
            for (KingdeeOrderEntity orderEntity : entityList) {
                LinkedList<String> itemFilters = new LinkedList<>();
                itemFilters.add(String.format("FBillNo = '%s'", orderEntity.getFBillNo()));
                itemFilters.add(String.format("FID = '%s'", orderEntity.getFId()));
                String itemFilterStr = String.join(" and ", itemFilters);
                String itemFieldKeys = "FSaleOrderEntry_FEntryID,FBillNo,FReturnType,FRowType,FMaterialName,FMaterialGroup,FMaterialId,FMaterialId.FNumber,FMaterialModel,FQty,FPriceUnitQty," +
                        "FUnitID,FAuxPropId,FPrice,FEntryTaxRate,FTaxPrice,FIsFree,FEntryTaxAmount,FMaterialType,FAmount,FBarcode,FMapName,F_ulz_BaseProperty,FMapId," +
                        "FBaseUnitId,FOldQty,FTaxNetPrice,FDiscount,FPriceDiscount,FBranchId,FEntryNote,FSrcType,FSrcBillNo,FMinPlanDeliveryDate,FDeliveryStatus," +
                        "F_ulz_Decimal,F_ulz_CGCB,FSOStockId.FNumber,FSOStockId.FName,FAllAmount";
                List<Map<String, Object>> itemResult = kingdeeApiUtils.queryList(itemFilterStr, itemFieldKeys, pageSize, 1, 10000);
                if (CollectionUtil.isEmpty(itemResult)) {
                    log.error("详情数据为空异常 itemFilterStr = {}  itemFieldKeys={} result ={}", itemFilterStr, itemFieldKeys, result);
                    // 保存异常信息到日志表
                    DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity(dto.getJobTaskDTO().getId(), itemFilterStr, JSONObject.toJSONString(itemResult), "详情数据为空异常");
                    dmpErrorLogService.add(dmpErrorLogEntity);
                }
                List<KingdeeOrderItemEntity> itemList = itemResult.stream().map(entity ->
                        BeanUtil.toBean(entity, KingdeeOrderItemEntity.class)).collect(Collectors.toList());
                orderEntity.setOrderItemEntityList(itemList);
            }
            infoArrayList.addAll(entityList);
            pageIndex++;
        }
        return infoArrayList;
    }

    /**
     * 解析订单数据
     **/
    public BiOrderInfoEntity initOrderInfoEntity(KingdeeOrderEntity kingdeeOrderEntity) {
        // 跳过非唯迹订单
        if (StrUtil.isEmpty(kingdeeOrderEntity.getFSaleOrgId()) ||
                ApiKingdeeOrganizationEnum.ORGANIZATION_YZS.getCode().equals(kingdeeOrderEntity.getFSaleOrgId()) ||
                ApiKingdeeOrganizationEnum.ORGANIZATION_XX.getCode().equals(kingdeeOrderEntity.getFSaleOrgId())
        ) {
            return null;
        }
        // 跳过单据类型
        if (StrUtil.isBlank(kingdeeOrderEntity.getFBillTypeCode()) || !ORDER_TYPES.contains(kingdeeOrderEntity.getFBillTypeCode())) {
            return null;
        }
        BiOrderInfoEntity biOrderInfoEntity = new BiOrderInfoEntity();
        //平台订单id
        biOrderInfoEntity.setPlatformOrderId(kingdeeOrderEntity.getFBillNo());
        //订单状态 2.配货中 3.已发货 4.已完成 5.已作废 6.退货 7.退款
        biOrderInfoEntity.setOrderStatus(4);
        //买家账号
        biOrderInfoEntity.setBuyerUserId("");
        //买家姓名
        biOrderInfoEntity.setBuyerName(kingdeeOrderEntity.getFLinkMan());
        //店铺编号
        biOrderInfoEntity.setShopNo(kingdeeOrderEntity.getCustomerCode());
        //店铺名称
        biOrderInfoEntity.setShopName(kingdeeOrderEntity.getFCustId());

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal orderFee = BigDecimal.ZERO;
        //汇率
        biOrderInfoEntity.setCurrencyRate(BigDecimal.ONE);
        if (null != kingdeeOrderEntity.getFExchangeRate() && BigDecimal.ZERO.compareTo(kingdeeOrderEntity.getFExchangeRate()) < 0) {
            biOrderInfoEntity.setCurrencyRate(kingdeeOrderEntity.getFExchangeRate());
        }
        List<KingdeeOrderItemEntity> orderItemEntityList = kingdeeOrderEntity.getOrderItemEntityList();
        for (KingdeeOrderItemEntity orderItemEntity : orderItemEntityList) {
            if (Objects.nonNull(orderItemEntity.getFAllAmount())) {
                orderFee = orderFee.add(orderItemEntity.getFAllAmount().multiply(biOrderInfoEntity.getCurrencyRate()).setScale(4, BigDecimal.ROUND_DOWN));
            }
            totalCost = totalCost.add(orderItemEntity.getF_ulz_Decimal());
            totalPrice = totalPrice.add(new BigDecimal(orderItemEntity.getFAmount()).multiply(biOrderInfoEntity.getCurrencyRate()).setScale(4, BigDecimal.ROUND_DOWN));
        }
        //商品总售价 订单金额+ 税费
        biOrderInfoEntity.setItemTotal(totalPrice);
        //订单金额
        biOrderInfoEntity.setOrderFee(orderFee);
        //订单成本价
        biOrderInfoEntity.setOrderCost(totalCost);
        //商品总成本
        biOrderInfoEntity.setItemTotalCost(totalCost);
        //待审核订单 1.否 2.是
        biOrderInfoEntity.setCanSend(1);
        //是否退货 1.退货 2.非退货
        biOrderInfoEntity.setIsReturned(0);
        //是否退款 1.退款 2.非退款
        biOrderInfoEntity.setIsRefund(0);
        //订单付款时间
        if (StringUtils.isNotBlank(kingdeeOrderEntity.getF_SK_Date()) && !"null".equals(kingdeeOrderEntity.getF_SK_Date())) {
            biOrderInfoEntity.setPaidTime(LocalDateTime.parse(kingdeeOrderEntity.getF_SK_Date()));
        }
        //发货时间
        if (StringUtils.isNotBlank(kingdeeOrderEntity.getFDeliveryDate()) && !"null".equals(kingdeeOrderEntity.getFDeliveryDate())) {
            biOrderInfoEntity.setDeliveryTime(LocalDateTime.parse(kingdeeOrderEntity.getFDeliveryDate()));
        }
        //平台订单时间
        if (StringUtils.isNotBlank(kingdeeOrderEntity.getFCreateDate()) && !"null".equals(kingdeeOrderEntity.getFCreateDate())) {
            biOrderInfoEntity.setPlatformCreateTime(LocalDateTime.parse(kingdeeOrderEntity.getFCreateDate()));
        }
        //平台交易号
        biOrderInfoEntity.setSalesRecordNumber(kingdeeOrderEntity.getFBillNo());
        //平台的订单状态
        biOrderInfoEntity.setPlatformOrderStatus("");
        //订单来源平台
        biOrderInfoEntity.setSourcePlatform("B2B");
        //是否合并订单 1.合并订单 2.非合并订单
        biOrderInfoEntity.setIsUnion(0);
        //是否拆分订单 1.拆分订单 2.非拆分订单
        biOrderInfoEntity.setIsSplit(0);
        //是否重发订单 1.重发订单 2.非重发订单
        biOrderInfoEntity.setIsResend(0);
        //缺货订单 0 正在计算是否缺货 1有货 2缺货 3 已补货
        biOrderInfoEntity.setHasGoods(0);
        //所属区域
        biOrderInfoEntity.setDistrict("");
        //买家城市
        biOrderInfoEntity.setCity("");
        //买家省份
        biOrderInfoEntity.setProvince("");
        //买家地址1
        biOrderInfoEntity.setManStreet(kingdeeOrderEntity.getFReceiveAddress());
        //买家地址2
        biOrderInfoEntity.setSecondStreet("");
        //交易关闭时间
        if (StringUtils.isNotBlank(kingdeeOrderEntity.getFCloseDate()) && !"null".equals(kingdeeOrderEntity.getFCloseDate())) {
            biOrderInfoEntity.setCloseDate(LocalDateTime.parse(kingdeeOrderEntity.getFCloseDate()));
        }
        //买家电话1
        biOrderInfoEntity.setManPhone(kingdeeOrderEntity.getFLinkPhone());
        //买家电话2
        biOrderInfoEntity.setSecondPhone("");
        //是否平台发货订单 1.否 2.是
        biOrderInfoEntity.setFbaFlag(0);
        //平台备注
        biOrderInfoEntity.setSellerMessage(kingdeeOrderEntity.getFNote());
        //币种
        biOrderInfoEntity.setCurrencyCode(kingdeeOrderEntity.getFSettleCurrId());
        //运费收入
        biOrderInfoEntity.setShippingFee(BigDecimal.ZERO);
        //平台费
        biOrderInfoEntity.setPlatformFee(BigDecimal.ZERO);
        //原始运费收入
        biOrderInfoEntity.setShippingTotalOrigin(BigDecimal.ZERO);
        //商品原始总售价
        biOrderInfoEntity.setItemTotalOrigin(orderFee);
        //补贴金额
        biOrderInfoEntity.setSubsidyAmount(BigDecimal.ZERO);
        //国家英文名称
        biOrderInfoEntity.setCountryNameEn(kingdeeOrderEntity.getCountryCode());
        //国家中文名称
        biOrderInfoEntity.setCountryNameCn(kingdeeOrderEntity.getFSHGJ1());
        //平台标识
        biOrderInfoEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
        //企业Id
        biOrderInfoEntity.setCompanyId(kingdeeOrderEntity.getFSaleOrgId());
        //企业名称
        biOrderInfoEntity.setCompanyName(kingdeeOrderEntity.getFSaleOrgName());
        biOrderInfoEntity.setCreateTime(LocalDateTime.now());
        biOrderInfoEntity.setItemList(initOrderItem(kingdeeOrderEntity));
        return biOrderInfoEntity;
    }

    /**
     * 解析订单商品数据
     **/
    public List<BiOrderItemSplitEntity> initOrderItem(KingdeeOrderEntity kingdeeOrderEntity) {
        List<KingdeeOrderItemEntity> orderItem = kingdeeOrderEntity.getOrderItemEntityList();
        List<BiOrderItemSplitEntity> orderItemList = new ArrayList<>();
        HashMap<String, Integer> skuCountMap = new HashMap<>();
        for (KingdeeOrderItemEntity orderItemBean : orderItem) {
            BiOrderItemSplitEntity biOrderItemSplitEntity = new BiOrderItemSplitEntity();
            //商品id
            biOrderItemSplitEntity.setItemId(orderItemBean.getFMaterialId());
            //平台sku
            biOrderItemSplitEntity.setPlatformSku(orderItemBean.getFMaterialName());
            //平台原始sku数量
            biOrderItemSplitEntity.setPlatformQuantity(orderItemBean.getFOldQty());
            //商品名称
            biOrderItemSplitEntity.setItemName(orderItemBean.getFMaterialName());
            //商品图片
            biOrderItemSplitEntity.setPictureUrl("");
            //商品成本价
            biOrderItemSplitEntity.setCostPrice(orderItemBean.getF_ulz_CGCB());
            //汇率
            if (null == kingdeeOrderEntity.getFExchangeRate()
                    || BigDecimal.ZERO.compareTo(kingdeeOrderEntity.getFExchangeRate()) >= 0) {
                biOrderItemSplitEntity.setCurrencyRate(BigDecimal.ONE);
            } else {
                biOrderItemSplitEntity.setCurrencyRate(kingdeeOrderEntity.getFExchangeRate());
            }
            //商品原始售价
            biOrderItemSplitEntity.setSellPriceOrigin(orderItemBean.getFPrice());
            //商品售价
            biOrderItemSplitEntity.setSellPrice(orderItemBean.getFPrice().multiply(biOrderItemSplitEntity.getCurrencyRate()).setScale(4, BigDecimal.ROUND_DOWN));
            //商品数量
            biOrderItemSplitEntity.setQuantity(orderItemBean.getFQty().intValue());
            //商品单位
            biOrderItemSplitEntity.setProductUnit("");
            //是否是赠品 1. 是 2. 否
            if (Boolean.valueOf(orderItemBean.getFIsFree())) {
                biOrderItemSplitEntity.setIsGift(1);
            } else {
                biOrderItemSplitEntity.setIsGift(2);
            }
            //缺货订单 0.正在计算是否缺货 1.有货 2.缺货 3.已补货
            biOrderItemSplitEntity.setHasGoods(0);
            //是否是组合商品 1.组合 2非组合
            biOrderItemSplitEntity.setIsCombo(0);
            //订单商品备注
            biOrderItemSplitEntity.setItemRemark(orderItemBean.getFEntryNote());
            //商品多属性
            biOrderItemSplitEntity.setSpecifics("");
            //商品状态 1：未付款 2：未发货 3：已发货 4：已作废
            switch (orderItemBean.getFDeliveryStatus()) {
                case "C":
                    biOrderItemSplitEntity.setStatus(String.valueOf(3));
                    break;
                case "B":
                    biOrderItemSplitEntity.setStatus(String.valueOf(2));
                case "A":
                    biOrderItemSplitEntity.setStatus(String.valueOf(2));
                    break;
                default:
                    biOrderItemSplitEntity.setStatus(String.valueOf(2));
                    break;
            }
            //商品仓库编号
            biOrderItemSplitEntity.setStockWarehouseId(orderItemBean.getFSOStockId());
            //商品仓位
            biOrderItemSplitEntity.setStockGrid("");
            //sku
            String skuNo = orderItemBean.getFMaterialNumber();
            biOrderItemSplitEntity.setSkuNo(skuNo);
            //库存状态：1.自动创建 2.待开发 3.正常 4.清仓 5.停止销售
            biOrderItemSplitEntity.setStockStatus(0);
            String erpOrderItemId = orderItemBean.getFBillNo()+ "_" + orderItemBean.getFEntryID() + "_" + orderItemBean.getFMaterialNumber();
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, skuNo, erpOrderItemId);
            //erp平台商品id
            biOrderItemSplitEntity.setErpOrderItemId(erpOrderItemId);
            biOrderItemSplitEntity.setAmountAfter(orderItemBean.getFAllAmount());
            orderItemList.add(biOrderItemSplitEntity);
        }
        return orderItemList;
    }
}

