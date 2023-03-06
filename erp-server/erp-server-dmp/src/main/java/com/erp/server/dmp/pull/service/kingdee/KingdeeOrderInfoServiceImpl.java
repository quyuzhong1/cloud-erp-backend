package com.erp.server.dmp.pull.service.kingdee;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.message.constant.RocketMqTopic;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.RocketMqTagEnum;
import com.erp.model.dmp.kingdee.KingdeeOrderEntity;
import com.erp.model.dmp.kingdee.KingdeeOrderItemEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.common.message.service.mq.MQProducerService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
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
    private static final List<String> ORDER_TYPES = new ArrayList<>(Arrays.asList("B2BXSDD","XSDD01_SYS"));

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Autowired
    private MQProducerService<DmpOrderInfoEntity> mqProducerService;

    public static void main(String[] args) {
        KingdeeOrderInfoServiceImpl kingdeeOrderInfoService = new KingdeeOrderInfoServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.SAL_SALEORDER.getTaskName());
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.now().minusDays(5));
        jobTaskDTO.setNextTime(LocalDateTime.now());
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
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
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
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
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByFIdAndBillNo(entity.getFBillNo(), entity.getFId());
            List<KingdeeOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER, KingdeeOrderEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            KingdeeOrderEntity mongoDatum = mongoData.get(0);
            String id = mongoDatum.get_id();
            mongoDatum.set_id(null);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(id);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER, KingdeeOrderEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("金蝶销售订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<DmpOrderInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_SALE_ORDER_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getPlatformOrderId(), msg.getSalesRecordNumber()));
            if (!SendStatus.SEND_OK .equals(result.getSendStatus())){
                throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());

    }

    /**
     * 请求金蝶云星空订单接口
     * @param dto
     * @return
     */
    public List<KingdeeOrderEntity> pullDate(RequestDTO dto) {
        List<KingdeeOrderEntity> infoArrayList = new ArrayList<>();
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        dto.getJobTaskDTO().setLastTime(nextTime);
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FModifyDate >= '%s'", sdf.format(lastTime.minusMinutes(2))));
        queryFilters.add(String.format("FModifyDate <= '%s'", sdf.format(nextTime)));
//            queryFilters.add(String.format("fCreateDate >= '%s'", "2023-01-05 00:00:00"));
//            queryFilters.add(String.format("fCreateDate <= '%s'", "2023-01-06 00:00:00"));
//            queryFilters.add(StrUtil.format("FBillNo ='{}'", "XSD-20230105-33831"));
        // 移除 订单类型过滤
//            queryFilters.add(String.format("fBillTypeID = '%s'", "eacb50844fc84a10b03d7b841f3a6278"));
        queryFilters.add(String.format("FDocumentStatus = '%s'", "C"));
        String filterStr = String.join(" and ",  queryFilters );
        String fieldKeys = "FID,FBillNo,FDate,FBillTypeId.FName,FBillTypeId.FNumber,FBillTypeId," +
                "FDocumentStatus,FCustId.FName,FSaleDeptId.FName,FSalerId.FName,FReceiveAddress,FLinkMan,FLinkPhone," +
                "FApproverId.FName,FApproveDate,FCloseStatus,FCloseDate,FCancelStatus,FChangerId," +
                "FReceiveId.FName,FNote,FHeadDeliveryWay,FHEADLOCID,FCorrespondOrgId,FSaleGroupId," +
                "FChangeReason,FBusinessType,FReceiveContact,FChargeId,FCreatorId,FCreateDate,FModifierId,FModifierId.FName," +
                "FModifyDate,FSaleOrgId,FSaleOrgId.FName,FVersionNo,FSignStatus,FSOFrom,F_SK_Date,F_SHGJ1,FExchangeRate,FSettleCurrId.FCode";

        Boolean dataSign = true;
        //当前页数
        Integer pageIndex = 0;

        //每次最多获取100条
        Integer pageSize = 10000;
        while (dataSign) {
            //"StartRow\":0,"+// 分页取数开始行索引，从0开始，例如每页10行数据，第2页开始是10，第3页开始是20
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName(), 1);
            List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            log.info("获取金蝶销售订单数据第[{}]页 有{}条记录", pageIndex, pageSize);
            if (result.size() < pageSize){
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
                String itemFieldKeys = "FBillNo,FReturnType,FRowType,FMaterialName,FMaterialGroup,FMaterialId,FMaterialId.FNumber,FMaterialModel,FQty,FPriceUnitQty," +
                        "FUnitID,FAuxPropId,FPrice,FEntryTaxRate,FTaxPrice,FIsFree,FEntryTaxAmount,FMaterialType,FAmount,FBarcode,FMapName,F_ulz_BaseProperty,FMapId," +
                        "FBaseUnitId,FOldQty,FTaxNetPrice,FDiscount,FPriceDiscount,FBranchId,FEntryNote,FSrcType,FSrcBillNo,FMinPlanDeliveryDate,FDeliveryStatus," +
                        "F_ulz_Decimal,F_ulz_CGCB,FSOStockId.FName,FAllAmount";
                List<Map<String, Object>> itemResult = kingdeeApiUtils.queryList(itemFilterStr, itemFieldKeys, pageSize, 1, 10000);
                if (CollectionUtil.isEmpty(itemResult)){
                    log.error("详情数据为空异常 itemFilterStr = {}  itemFieldKeys={} result ={}", itemFilterStr, itemFieldKeys, result);
                    // 保存异常信息到日志表
                    DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity(dto.getJobTaskDTO().getId(), itemFilterStr,JSONObject.toJSONString(itemResult),"详情数据为空异常");
                    dmpErrorLogService.add(dmpErrorLogEntity);
                }
                List<KingdeeOrderItemEntity> itemList = itemResult.stream().map(entity ->
                        BeanUtil.toBean(entity, KingdeeOrderItemEntity.class)).collect(Collectors.toList());
                orderEntity.setOrderItemEntityList(itemList);
            }
            infoArrayList.addAll(entityList);
            pageIndex ++;
        }
        return infoArrayList;
    }

    /**
     * 解析订单数据
     **/
    public DmpOrderInfoEntity initOrderInfoEntity(KingdeeOrderEntity kingdeeOrderEntity) {
        // 跳过非唯迹订单
        if (StrUtil.isEmpty(kingdeeOrderEntity.getFSaleOrgId()) ||
                ApiKingdeeOrganizationEnum.ORGANIZATION_YZS.getCode().equals(kingdeeOrderEntity.getFSaleOrgId()) ||
                ApiKingdeeOrganizationEnum.ORGANIZATION_XX.getCode().equals(kingdeeOrderEntity.getFSaleOrgId())
        ){
            return null;
        }
        // 跳过单据类型
        if (StrUtil.isBlank(kingdeeOrderEntity.getFBillTypeCode()) || !ORDER_TYPES.contains(kingdeeOrderEntity.getFBillTypeCode())){
            return null;
        }
        DmpOrderInfoEntity dmpOrderInfoEntity = new DmpOrderInfoEntity();
        //平台订单id
        dmpOrderInfoEntity.setPlatformOrderId(kingdeeOrderEntity.getFBillNo());
        //订单状态 2.配货中 3.已发货 4.已完成 5.已作废 6.退货 7.退款
        dmpOrderInfoEntity.setOrderStatus(4);
        //买家账号
        dmpOrderInfoEntity.setBuyerUserId("");
        //买家姓名
        dmpOrderInfoEntity.setBuyerName(kingdeeOrderEntity.getFLinkMan());
        //店铺编号
        dmpOrderInfoEntity.setShopNo("B2B");
        //店铺名称
        dmpOrderInfoEntity.setShopName("B2B");
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal orderFee = BigDecimal.ZERO;
        //汇率
        dmpOrderInfoEntity.setCurrencyRate(BigDecimal.ONE);
        if (null != kingdeeOrderEntity.getFExchangeRate() && BigDecimal.ZERO.compareTo(kingdeeOrderEntity.getFExchangeRate()) < 0) {
            dmpOrderInfoEntity.setCurrencyRate(kingdeeOrderEntity.getFExchangeRate());
        }
        List<KingdeeOrderItemEntity> orderItemEntityList = kingdeeOrderEntity.getOrderItemEntityList();
        for (KingdeeOrderItemEntity orderItemEntity : orderItemEntityList) {
            orderFee = orderFee.add(orderItemEntity.getFAllAmount().multiply(dmpOrderInfoEntity.getCurrencyRate()).setScale(4, BigDecimal.ROUND_DOWN));
            totalCost = totalCost.add(orderItemEntity.getF_ulz_Decimal());
            totalPrice = totalPrice.add(new BigDecimal(orderItemEntity.getFAmount()).multiply(dmpOrderInfoEntity.getCurrencyRate()).setScale(4, BigDecimal.ROUND_DOWN));
        }
        //商品总售价 订单金额+ 税费
        dmpOrderInfoEntity.setItemTotal(totalPrice);
        //订单金额
        dmpOrderInfoEntity.setOrderFee(orderFee);
        //订单成本价
        dmpOrderInfoEntity.setOrderCost(totalCost);
        //商品总成本
        dmpOrderInfoEntity.setItemTotalCost(totalCost);
        //待审核订单 1.否 2.是
        dmpOrderInfoEntity.setCanSend(1);
        //是否退货 1.退货 2.非退货
        dmpOrderInfoEntity.setIsReturned(0);
        //是否退款 1.退款 2.非退款
        dmpOrderInfoEntity.setIsRefund(0);
        //订单付款时间
        if (StringUtils.isNotBlank(kingdeeOrderEntity.getF_SK_Date()) && !"null".equals(kingdeeOrderEntity.getF_SK_Date())) {
            dmpOrderInfoEntity.setPaidTime(LocalDateTime.parse(kingdeeOrderEntity.getF_SK_Date()));
        }
        //平台订单时间
        if (StringUtils.isNotBlank(kingdeeOrderEntity.getFCreateDate()) && !"null".equals(kingdeeOrderEntity.getFCreateDate())) {
            dmpOrderInfoEntity.setPlatformCreateTime(LocalDateTime.parse(kingdeeOrderEntity.getFCreateDate()));
        }
        //平台交易号
        dmpOrderInfoEntity.setSalesRecordNumber(kingdeeOrderEntity.getFBillNo());
        //平台的订单状态
        dmpOrderInfoEntity.setPlatformOrderStatus("");
        //订单来源平台
        dmpOrderInfoEntity.setSourcePlatform("B2B");
        //是否合并订单 1.合并订单 2.非合并订单
        dmpOrderInfoEntity.setIsUnion(0);
        //是否拆分订单 1.拆分订单 2.非拆分订单
        dmpOrderInfoEntity.setIsSplit(0);
        //是否重发订单 1.重发订单 2.非重发订单
        dmpOrderInfoEntity.setIsResend(0);
        //缺货订单 0 正在计算是否缺货 1有货 2缺货 3 已补货
        dmpOrderInfoEntity.setHasGoods(0);
        //所属区域
        dmpOrderInfoEntity.setDistrict("");
        //买家城市
        dmpOrderInfoEntity.setCity("");
        //买家省份
        dmpOrderInfoEntity.setProvince("");
        //买家地址1
        dmpOrderInfoEntity.setManStreet(kingdeeOrderEntity.getFReceiveAddress());
        //买家地址2
        dmpOrderInfoEntity.setSecondStreet("");
        //交易关闭时间
        if (StringUtils.isNotBlank(kingdeeOrderEntity.getFCloseDate()) && !"null".equals(kingdeeOrderEntity.getFCloseDate())) {
            dmpOrderInfoEntity.setCloseDate(LocalDateTime.parse(kingdeeOrderEntity.getFCloseDate()));
        }
        //买家电话1
        dmpOrderInfoEntity.setManPhone(kingdeeOrderEntity.getFLinkPhone());
        //买家电话2
        dmpOrderInfoEntity.setSecondPhone("");
        //是否平台发货订单 1.否 2.是
        dmpOrderInfoEntity.setFbaFlag(0);
        //平台备注
        dmpOrderInfoEntity.setSellerMessage(kingdeeOrderEntity.getFNote());
        //币种
        dmpOrderInfoEntity.setCurrencyCode(kingdeeOrderEntity.getFSettleCurrId());
        //运费收入
        dmpOrderInfoEntity.setShippingFee(BigDecimal.ZERO);
        //平台费
        dmpOrderInfoEntity.setPlatformFee(BigDecimal.ZERO);
        //原始运费收入
        dmpOrderInfoEntity.setShippingTotalOrigin(BigDecimal.ZERO);
        //商品原始总售价
        dmpOrderInfoEntity.setItemTotalOrigin(orderFee);
        //补贴金额
        dmpOrderInfoEntity.setSubsidyAmount(BigDecimal.ZERO);
        //国家英文名称
        dmpOrderInfoEntity.setCountryNameEn("");
        //国家中文名称
        dmpOrderInfoEntity.setCountryNameCn(kingdeeOrderEntity.getFSHGJ1());
        //平台标识
        dmpOrderInfoEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
        //企业Id
        dmpOrderInfoEntity.setCompanyId(kingdeeOrderEntity.getFSaleOrgId());
        //企业名称
        dmpOrderInfoEntity.setCompanyName(kingdeeOrderEntity.getFSaleOrgName());
        dmpOrderInfoEntity.setCreateTime(LocalDateTime.now());
        dmpOrderInfoEntity.setItemList(initOrderItem(kingdeeOrderEntity));
        return dmpOrderInfoEntity;
    }

    /**
     * 解析订单商品数据
     **/
    public List<DmpOrderItemEntity> initOrderItem(KingdeeOrderEntity kingdeeOrderEntity) {
        List<KingdeeOrderItemEntity> orderItem = kingdeeOrderEntity.getOrderItemEntityList();
        List<DmpOrderItemEntity> orderItemList = new ArrayList<>();
        HashMap<String, Integer> skuCountMap = new HashMap<>();
        for (KingdeeOrderItemEntity orderItemBean : orderItem) {
            DmpOrderItemEntity dmpOrderItemEntity = new DmpOrderItemEntity();
            //商品id
            dmpOrderItemEntity.setItemId(orderItemBean.getFMaterialId());
            //平台sku
            dmpOrderItemEntity.setPlatformSku(orderItemBean.getFMaterialName());
            //平台原始sku数量
            dmpOrderItemEntity.setPlatformQuantity(orderItemBean.getFOldQty());
            //商品名称
            dmpOrderItemEntity.setItemName(orderItemBean.getFMaterialName());
            //商品图片
            dmpOrderItemEntity.setPictureUrl("");
            //商品成本价
            dmpOrderItemEntity.setCostPrice(orderItemBean.getF_ulz_CGCB());
            //汇率
            if (null == kingdeeOrderEntity.getFExchangeRate()
                    || BigDecimal.ZERO.compareTo(kingdeeOrderEntity.getFExchangeRate()) >= 0) {
                dmpOrderItemEntity.setCurrencyRate(BigDecimal.ONE);
            } else {
                dmpOrderItemEntity.setCurrencyRate(kingdeeOrderEntity.getFExchangeRate());
            }
            //商品原始售价
            dmpOrderItemEntity.setSellPriceOrigin(orderItemBean.getFPrice());
            //商品售价
            dmpOrderItemEntity.setSellPrice(orderItemBean.getFPrice().multiply(dmpOrderItemEntity.getCurrencyRate()).setScale(4, BigDecimal.ROUND_DOWN));
            //商品数量
            dmpOrderItemEntity.setQuantity(orderItemBean.getFQty().intValue());
            //商品单位
            dmpOrderItemEntity.setProductUnit("");
            //是否是赠品 1. 是 2. 否
            if (Boolean.valueOf(orderItemBean.getFIsFree())) {
                dmpOrderItemEntity.setIsGift(1);
            } else {
                dmpOrderItemEntity.setIsGift(2);
            }
            //缺货订单 0.正在计算是否缺货 1.有货 2.缺货 3.已补货
            dmpOrderItemEntity.setHasGoods(0);
            //是否是组合商品 1.组合 2非组合
            dmpOrderItemEntity.setIsCombo(0);
            //订单商品备注
            dmpOrderItemEntity.setItemRemark(orderItemBean.getFEntryNote());
            //商品多属性
            dmpOrderItemEntity.setSpecifics("");
            //商品状态 1：未付款 2：未发货 3：已发货 4：已作废
            switch (orderItemBean.getFDeliveryStatus()) {
                case "C" :
                    dmpOrderItemEntity.setStatus(3);
                    break;
                case "B":
                    dmpOrderItemEntity.setStatus(2);
                case "A":
                    dmpOrderItemEntity.setStatus(2);
                    break;
                default:
                    dmpOrderItemEntity.setStatus(2);
                    break;
            }
            //商品仓库编号
            dmpOrderItemEntity.setStockWarehouseId(orderItemBean.getFSOStockId());
            //商品仓位
            dmpOrderItemEntity.setStockGrid("");
            //sku
            String skuNo = orderItemBean.getFMaterialNumber();
            dmpOrderItemEntity.setSkuNo(skuNo);
            //库存状态：1.自动创建 2.待开发 3.正常 4.清仓 5.停止销售
            dmpOrderItemEntity.setStockStatus(0);
            String erpOrderItemId = orderItemBean.getFBillNo() + "_" + orderItemBean.getFMaterialNumber();
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, skuNo, erpOrderItemId);
            //erp平台商品id
            dmpOrderItemEntity.setErpOrderItemId(erpOrderItemId);
            dmpOrderItemEntity.setAmountAfter(orderItemBean.getFAllAmount());
            orderItemList.add(dmpOrderItemEntity);
        }
        return orderItemList;
    }
}
