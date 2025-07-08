package com.erp.server.dmp.pull.service.kingdee;

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
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.BiReturnOrderInfoEntity;
import com.erp.model.dmp.entity.BiReturnOrderItemEntity;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.model.dmp.kingdee.item.KingdeeReturnOrderItemEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.erp.server.dmp.utils.MapCountUtils;
import com.xxl.job.core.util.GsonTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 金蝶退货销售出库
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.SAL_RETURNSTOCK)
public class KingdeeReturnOrderInfoImpl implements IReportSaveService<KingdeeReturnOrderEntity> {
    @Resource
    private MongoService mongoService;

    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private CfgSettingService cfgSettingService;

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<KingdeeReturnOrderEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取金蝶退货订单列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取金蝶退货订单列表数据 entityList.size = {} ", entityList.size());
        List<KingdeeReturnOrderEntity> insertList = new ArrayList<>();
        List<KingdeeReturnOrderEntity> pushToMqList = new ArrayList<>();
        for (KingdeeReturnOrderEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByBillNoAndOrderNo(entity.getFBillNo(), null);
            List<KingdeeReturnOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_RETURN_ORDER, KingdeeReturnOrderEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            KingdeeReturnOrderEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            //过滤无效数据
            if (!mongoDatum.getIsValid()) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(GsonTool.toJson(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_RETURN_ORDER, KingdeeReturnOrderEntity.class);
        }

        //同步到OMS销售退货单
        pushToMqList.forEach(req -> {
            if (StringUtils.isNotBlank(req.getFDate()) && !req.getFDate().equals("null")) {
                if (LocalDateTime.parse(req.getFDate()).toLocalDate().compareTo(LocalDate.parse("2023-07-06")) > 0) {
                    mqProducerService.asyncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_REFUND_ORDER_TO_TASK_TAG.getName(), req, req.getFBillNo());
                }
            }

        });

        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_KINGDEE_RETURN_ORDER);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("金蝶退货订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        //过滤oms 推送的订单数据
        KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils();
        if (kingdeeApiUtils.notNeedPushMQ(dto.getJobTaskDTO().getLastTime())){
            pushToMqList = pushToMqList.stream().filter(e -> !CommonConstants.SYSTEM.equals(e.getFULZDataSources())).collect(Collectors.toList());
        }
        // 构造订单结构
        List<BiReturnOrderInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        List<BiReturnOrderInfoEntity> biReturnOrderInfoEntityList = entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_RETURN_ORDER_TAG.getName(),
                    msg,msg.getPlatformOrderId());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("金蝶退货订单发送数据为：{}" , JSON.toJSONString(biReturnOrderInfoEntityList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<KingdeeReturnOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_KINGDEE_RETURN_ORDER, KingdeeReturnOrderEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (KingdeeReturnOrderEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(KingdeeReturnOrderEntity mongoDatum) {
        BiReturnOrderInfoEntity returnOrderInfo = initOrderInfoEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
        if(null == returnOrderInfo){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_RETURN_ORDER, KingdeeReturnOrderEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_RETURN_ORDER, KingdeeReturnOrderEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_RETURN_ORDER_TAG.getName(),
                returnOrderInfo,returnOrderInfo.getReturnCode());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求金蝶云星空销售出库接口
     * @param dto
     * @return
     */
    public List<KingdeeReturnOrderEntity> pullDate(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();

        LinkedList<String> queryFilters = new LinkedList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        // 退货单拉全类型的单据
//        queryFilters.add(StrUtil.format("FBillTypeID in ('{}','{}')", "73383412199a402bb58439509e089077","559351ce1d0252"));
//        queryFilters.add(String.format("FOrderNo <> '%s'", ""));
        queryFilters.add(StrUtil.format("FDocumentStatus in ({})", "'C'"));
        queryFilters.add(StrUtil.format(" FISGENFORIOS = {}", "0"));
        queryFilters.add(StrUtil.format(" (FApproveDate >= '{}' and FApproveDate < '{}')",sdf.format(lastTime.minusMinutes(8)),sdf.format(nextTime)));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FID," +
                "FBillTypeID," +
                "FBillTypeID.FName," +
                "FBillTypeID.FNumber," +
                "FBillNo," +
                "FDate," +
                "FDocumentStatus," +
                "FSaleOrgId," +
                "FSaleOrgId.FName," +
                "FRetcustId.FName," +
                "FRetcustId.FNumber," +
                "FSalesManId," +
                "FSalesManId.FName," +
                "FCreateDate," +
                "FModifyDate," +
                "FCancelStatus," +
                "FReceiverCountry," +
                "FLinkMan," +
                "FExchangeRate," +
                "FApproveDate," +
                "FBussinessType," +
                "FOwnerTypeIdHead," +
                "FSettleCurrId.FCode," +
                "FDelTime," +
                "FHeadNote," +
                "FReturnReason.FDataValue," +
                "FSaledeptid.FNumber," +
                "FSaledeptid.FName," +
                "FOrderNo," +
                "FAmount," +
                "FMustqty," +
                "FUnitID.FName," +
                "FEntity_FEntryId," +
                "FMaterialId," +
                "FMaterialId.FNumber," +
                "FMaterialName," +
                "FAuxpropId," +
                "FMaterialType," +
                "FPrice," +
                "FStockId," +
                "FStockId.FNumber," +
                "FStockId.FName," +
                "FStockLocId.FF100014.FNumber," +
                "FStockstatusId," +
                "FNote," +
                "FSrcBillNo," +
                "FSrcBillTypeID," +
                "FIsFree," +
                "FMaterialModel," +
                "FRealQty," +
                "FSOBILLTYPEID," +
                "FSalUnitQty," +
                "FProjectNo," +
                "F_ulz_KHSKU," +
                "FAllAmount," +
                "FReturnType," +
                "FSOEntryId," +
                "F_ULZ_data_sources,FISGENFORIOS," +
                "FETHIRDBILLNO";

        Boolean dataSign = true;
        //当前页数
        Integer pageIndex = 0;
        //每次最多获取100条
        Integer pageSize = 10000;
        List<Map<String, Object>> resultAll = new ArrayList<>();
        while (dataSign) {
            //"StartRow\":0,"+// 分页取数开始行索引，从0开始，例如每页10行数据，第2页开始是10，第3页开始是20
//            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName());
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName());
            List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            log.info("获取金蝶退货数据第[{}]页 有{}条记录", pageIndex, pageSize);
            if (result.size() < pageSize){
                dataSign = false;
            }
            if (CollectionUtil.isEmpty(result)) {
                break;
            }
            resultAll.addAll(result);
            pageIndex++;
        }
        List<KingdeeReturnOrderEntity> entityList = resultAll.stream().map(entity ->
                JSONObject.parseObject(JSONObject.toJSONString(entity), KingdeeReturnOrderEntity.class)).distinct()
                .collect(Collectors.toList());

        Map<String, List<KingdeeReturnOrderItemEntity>> itemMap = resultAll.stream().map(entity ->
                        JSONObject.parseObject(JSONObject.toJSONString(entity), KingdeeReturnOrderItemEntity.class))
                .collect(Collectors.groupingBy(KingdeeReturnOrderItemEntity::getFBillNo));
        List<KingdeeReturnOrderEntity> kingdeeReturnOrderEntityList = entityList.stream().peek(m -> m.setItemEntityList(itemMap.get( m.getFBillNo())))
                .distinct()
                .collect(Collectors.toList());
        log.debug("金蝶退货订单原始数据为：{}" , JSON.toJSONString(kingdeeReturnOrderEntityList));

        return entityList;
    }

    /**
     * 解析订单数据
     **/
    private BiReturnOrderInfoEntity initOrderInfoEntity(KingdeeReturnOrderEntity returnOrderEntity) {
        if (StrUtil.isEmpty(returnOrderEntity.getFSaleOrgId()) ||
                ApiKingdeeOrganizationEnum.ORGANIZATION_YZS.getCode().equals(returnOrderEntity.getFSaleOrgId()) ||
                ApiKingdeeOrganizationEnum.ORGANIZATION_XX.getCode().equals(returnOrderEntity.getFSaleOrgId())
        ){
            return null;
        }
        BiReturnOrderInfoEntity biReturnOrderInfoEntity = new BiReturnOrderInfoEntity();
        //平台订单编号
        biReturnOrderInfoEntity.setPlatformOrderId(returnOrderEntity.getFBillNo());
        biReturnOrderInfoEntity.setReturnCode(returnOrderEntity.getFBillNo());
        biReturnOrderInfoEntity.setPlatformReturnCode(returnOrderEntity.getFBillNo());
        //店铺编号
        biReturnOrderInfoEntity.setShopNo("B2B");
        //店铺名称
        biReturnOrderInfoEntity.setShopName("B2B");
        //付款时间
        biReturnOrderInfoEntity.setPaidTime(null);
        //发货时间
        if (!"null".equals(returnOrderEntity.getFDelTime()) && StrUtil.isNotEmpty(returnOrderEntity.getFDelTime())){
            biReturnOrderInfoEntity.setExpressTime(LocalDateTime.parse(returnOrderEntity.getFDelTime()));
        }
        Integer status = 2;
        if (Objects.equals(returnOrderEntity.getFCancelStatus(), "C")) {
            status = 5;
        }
        if (Objects.equals(returnOrderEntity.getFDocumentStatus(), "C")) {
            status = 4;
        }
        //状态：1待处理 2已退款 3已重发 4已完成 5已作废
        biReturnOrderInfoEntity.setStatus(status);
        //平台交易号
        biReturnOrderInfoEntity.setSalesRecordNumber(returnOrderEntity.getFBillNo());
        //汇率
        biReturnOrderInfoEntity.setCurrencyRate(BigDecimal.ONE);
        if (null != returnOrderEntity.getFExchangeRate() && BigDecimal.ZERO.compareTo(returnOrderEntity.getFExchangeRate()) < 0) {
            biReturnOrderInfoEntity.setCurrencyRate(returnOrderEntity.getFExchangeRate());
        }
        List<KingdeeReturnOrderItemEntity> itemEntityList = returnOrderEntity.getItemEntityList();
        BigDecimal amount = BigDecimal.ZERO;
        for (KingdeeReturnOrderItemEntity kingdeeReturnOrderItemEntity : itemEntityList) {
            amount = amount.add(kingdeeReturnOrderItemEntity.getFAllAmount().multiply(biReturnOrderInfoEntity.getCurrencyRate()));
        }
        //订单金额
        biReturnOrderInfoEntity.setOrderFee(amount);
        //订单重量
        biReturnOrderInfoEntity.setOrderWeight(BigDecimal.ZERO);
        biReturnOrderInfoEntity.setPlatformName("B2B");
        //国家英文名称
        biReturnOrderInfoEntity.setCountryNameEn("");
        //国家中文名称
        biReturnOrderInfoEntity.setCountryNameCn(returnOrderEntity.getFReceiverCountry());
        //买家账号
        biReturnOrderInfoEntity.setBuyerUserId("");
        //买家姓名
        biReturnOrderInfoEntity.setBuyerName(returnOrderEntity.getFRetcustName());
        //登记人编号
        biReturnOrderInfoEntity.setEmployeeId(returnOrderEntity.getFSaleOrgId());
        //登记人名称
        biReturnOrderInfoEntity.setEmployeeName(returnOrderEntity.getFSalesManName());
        //备注
        biReturnOrderInfoEntity.setRemark(returnOrderEntity.getFHeadNote());
        //退货信息创建时间
        if (!"null".equals(returnOrderEntity.getFCreateDate()) && StrUtil.isNotEmpty(returnOrderEntity.getFCreateDate())){
            biReturnOrderInfoEntity.setReturnCreateTime(LocalDateTime.parse(returnOrderEntity.getFCreateDate()));
        }
        //退款时间
        if (!"null".equals(returnOrderEntity.getFApproveDate()) && StrUtil.isNotEmpty(returnOrderEntity.getFApproveDate())){
            biReturnOrderInfoEntity.setRefundTime(LocalDateTime.parse(returnOrderEntity.getFApproveDate()));
        }
        //币种
        biReturnOrderInfoEntity.setCurrencyCode(returnOrderEntity.getFSettleCurrCode());
        //平台标识
        biReturnOrderInfoEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
        //企业Id
        biReturnOrderInfoEntity.setCompanyId(returnOrderEntity.getFSaleOrgId());
        //企业名称
        biReturnOrderInfoEntity.setCompanyName(returnOrderEntity.getFSaleOrgName());
        biReturnOrderInfoEntity.setIsDeleted(Boolean.FALSE);
        biReturnOrderInfoEntity.setCreateTime(LocalDateTime.now());
        biReturnOrderInfoEntity.setItemList(initOrderItem(returnOrderEntity));
        return biReturnOrderInfoEntity;
    }

    /**
     * 解析退货订单商品数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    public List<BiReturnOrderItemEntity> initOrderItem(KingdeeReturnOrderEntity returnOrderEntity) {
        List<BiReturnOrderItemEntity> orderItemList = new ArrayList<>();
        HashMap<String, Integer> skuCountMap = new HashMap<>();
        returnOrderEntity.getItemEntityList().stream().forEach(orderItemBean ->{
            BiReturnOrderItemEntity biReturnOrderItemEntity = new BiReturnOrderItemEntity();
            //sku编号
            String skuNo = orderItemBean.getFMaterialNumber();
            biReturnOrderItemEntity.setSkuNo(skuNo);
            //商品名称
            biReturnOrderItemEntity.setItemName(orderItemBean.getFMaterialName());
            if (StringUtils.isNotBlank(orderItemBean.getFRealQty())) {
                //买家购买数量
                biReturnOrderItemEntity.setQuantity(Double.valueOf(orderItemBean.getFRealQty()).intValue());
            }
            //商品单位
            biReturnOrderItemEntity.setProductUnit(orderItemBean.getFUnitName());
            //商品图片地址
            biReturnOrderItemEntity.setPictureUrl("");
            //售价
            biReturnOrderItemEntity.setSellPrice(new BigDecimal(orderItemBean.getFPrice()));
            //物品属性
            biReturnOrderItemEntity.setSpecifics(orderItemBean.getFMaterialModel());
            //状态 1待处理 2验货入库 3自然耗损
            biReturnOrderItemEntity.setStatus(2);
            //erp平台商品id
            String erpOrderItemId = orderItemBean.getFOrderNo() + "_" + returnOrderEntity.getFBillNo() + "_" + skuNo;
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, skuNo, erpOrderItemId);
            biReturnOrderItemEntity.setErpOrderItemId(erpOrderItemId);
            biReturnOrderItemEntity.setIsDeleted(Boolean.FALSE);
            biReturnOrderItemEntity.setAmountAfter(orderItemBean.getFAllAmount());
            orderItemList.add(biReturnOrderItemEntity);
        });
        return orderItemList;
    }
}
