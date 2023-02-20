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
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.RocketMqTagEnum;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderItemEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.common.message.service.mq.MQProducerService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.MapCountUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
    private MQProducerService<DmpReturnOrderInfoEntity> mqProducerService;

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<KingdeeReturnOrderEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取金蝶退货订单列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取金蝶退货订单列表数据 entityList.size = {} ", entityList.size());
        List<KingdeeReturnOrderEntity> insertList = new ArrayList<>();
        List<KingdeeReturnOrderEntity> pushToMqList = new ArrayList<>();
        for (KingdeeReturnOrderEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByBillNoAndOrderNo(entity.getFBillNo(), entity.getFOrderNo());
            List<KingdeeReturnOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER, KingdeeReturnOrderEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            KingdeeReturnOrderEntity mongoDatum = mongoData.get(0);
            String id = mongoDatum.get_id();
            mongoDatum.set_id(null);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(id);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER, KingdeeReturnOrderEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("金蝶退货订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<DmpReturnOrderInfoEntity> mabangToMqlist = pushToMqList.parallelStream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        mabangToMqlist.stream().peek(msg ->
                        mqProducerService.asyncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_RETURN_ORDER_TAG.getName(),
                                msg, StrUtil.format("{}_{}", msg.getPlatformOrderId(), msg.getSalesRecordNumber())))
                .collect(Collectors.toList());
    }

    /**
     * 请求金蝶云星空销售出库接口
     * @param dto
     * @return
     */
    public List<KingdeeReturnOrderEntity> pullDate(RequestDTO dto) {
        List<KingdeeReturnOrderEntity> infoArrayList = new ArrayList<>();
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);

        LinkedList<String> queryFilters = new LinkedList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        queryFilters.add(String.format("FModifyDate >= '%s'", sdf.format(lastTime.minusMinutes(2))));
        queryFilters.add(String.format("FModifyDate <= '%s'", sdf.format(nextTime)));
        queryFilters.add(String.format("FBillTypeID = '%s'", "73383412199a402bb58439509e089077"));
//        queryFilters.add(String.format("FOrderNo <> '%s'", ""));
        queryFilters.add(String.format("FDocumentStatus = '%s'", "C"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FID,FBillTypeID,FBillTypeID.FName,FBillTypeID.FNumber,FBillNo,FDate,FDocumentStatus,FSaleOrgId,FSaleOrgId.FName,FRetcustId," +
                "FRetcustId.FName,FSalesManId,FSalesManId.FName,FCreateDate,FModifyDate,FCancelStatus,FReceiverCountry,FLinkMan,FExchangeRate," +
                "FApproveDate,FBussinessType,FOwnerTypeIdHead,FSettleCurrId.FCode,FDelTime,FHeadNote,"
                + "FOrderNo,FAmount,FMustqty,FUnitID.FName,FMaterialId,FMaterialId.FNumber,FMaterialName,FAuxpropId,FMaterialType,FPrice,FStockId," +
                "FStocklocId,FStockstatusId,FNote,FSrcBillNo,FSrcBillTypeID,FIsFree,FMaterialModel,FRealQty,FSOBILLTYPEID,FSalUnitQty,FProjectNo,F_ulz_KHSKU,FAllAmount";

        Boolean dataSign = true;
        //当前页数
        Integer pageIndex = 0;
        //每次最多获取100条
        Integer pageSize = 10000;
        while (dataSign) {
            //"StartRow\":0,"+// 分页取数开始行索引，从0开始，例如每页10行数据，第2页开始是10，第3页开始是20
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName(), 1);
            List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            log.info("获取金蝶退货数据第[{}]页 有{}条记录", pageIndex, pageSize);
            if (result.size() < pageSize){
                dataSign = false;
            }
            if (CollectionUtil.isEmpty(result)) {
                return Collections.emptyList();
            }
            List<KingdeeReturnOrderEntity> entityList = result.stream().map(entity ->
                    BeanUtil.toBean(entity, KingdeeReturnOrderEntity.class)).distinct()
                    .collect(Collectors.toList());

            Map<String, List<KingdeeReturnOrderItemEntity>> itemMap = result.stream().map(entity ->
                            BeanUtil.toBean(entity, KingdeeReturnOrderItemEntity.class))
                    .collect(Collectors.groupingBy(m -> StrUtil.format("{}_{}", m.getFBillNo(), m.getFOrderNo())));
            entityList.stream().peek(m -> m.setItemEntityList(itemMap.get(StrUtil.format("{}_{}", m.getFBillNo(), m.getFOrderNo()))))
                    .collect(Collectors.toList());

            infoArrayList.addAll(entityList);
            pageIndex++;
        }
        return infoArrayList;
    }

    /**
     * 解析订单数据
     **/
    private DmpReturnOrderInfoEntity initOrderInfoEntity(KingdeeReturnOrderEntity returnOrderEntity) {
        if (StrUtil.isEmpty(returnOrderEntity.getFSaleOrgId()) ||
                ApiKingdeeOrganizationEnum.ORGANIZATION_YZS.getCode().equals(returnOrderEntity.getFSaleOrgId()) ||
                ApiKingdeeOrganizationEnum.ORGANIZATION_XX.getCode().equals(returnOrderEntity.getFSaleOrgId())
        ){
            return null;
        }
        if (StrUtil.isBlank(returnOrderEntity.getFOrderNo())){
            return null;
        }
        DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity = new DmpReturnOrderInfoEntity();
        //平台订单编号
        dmpReturnOrderInfoEntity.setPlatformOrderId(returnOrderEntity.getFOrderNo());
        //退货单号
        dmpReturnOrderInfoEntity.setReturnOrderId(returnOrderEntity.getFBillNo());
        //店铺编号
        dmpReturnOrderInfoEntity.setShopNo("B2B");
        //店铺名称
        dmpReturnOrderInfoEntity.setShopName("B2B");
        //付款时间
        dmpReturnOrderInfoEntity.setPaidTime(null);
        //发货时间
        if (!"null".equals(returnOrderEntity.getFDelTime()) && StrUtil.isNotEmpty(returnOrderEntity.getFDelTime())){
            dmpReturnOrderInfoEntity.setExpressTime(LocalDateTime.parse(returnOrderEntity.getFDelTime()));
        }
        Integer status = 2;
        if (Objects.equals(returnOrderEntity.getFCancelStatus(), "C")) {
            status = 5;
        }
        if (Objects.equals(returnOrderEntity.getFDocumentStatus(), "C")) {
            status = 4;
        }
        //状态：1待处理 2已退款 3已重发 4已完成 5已作废
        dmpReturnOrderInfoEntity.setStatus(status);
        //平台交易号
        dmpReturnOrderInfoEntity.setSalesRecordNumber(returnOrderEntity.getFBillNo());
        //汇率
        dmpReturnOrderInfoEntity.setCurrencyRate(BigDecimal.ONE);
        if (null != returnOrderEntity.getFExchangeRate() && BigDecimal.ZERO.compareTo(returnOrderEntity.getFExchangeRate()) < 0) {
            dmpReturnOrderInfoEntity.setCurrencyRate(returnOrderEntity.getFExchangeRate());
        }
        List<KingdeeReturnOrderItemEntity> itemEntityList = returnOrderEntity.getItemEntityList();
        BigDecimal amount = BigDecimal.ZERO;
        for (KingdeeReturnOrderItemEntity kingdeeReturnOrderItemEntity : itemEntityList) {
            amount = amount.add(kingdeeReturnOrderItemEntity.getFAllAmount().multiply(dmpReturnOrderInfoEntity.getCurrencyRate()));
        }
        //订单金额
        dmpReturnOrderInfoEntity.setOrderFee(amount);
        //订单重量
        dmpReturnOrderInfoEntity.setOrderWeight(BigDecimal.ZERO);
        dmpReturnOrderInfoEntity.setPlatformName("B2B");
        //国家英文名称
        dmpReturnOrderInfoEntity.setCountryNameEn("");
        //国家中文名称
        dmpReturnOrderInfoEntity.setCountryNameCn(returnOrderEntity.getFReceiverCountry());
        //买家账号
        dmpReturnOrderInfoEntity.setBuyerUserId("");
        //买家姓名
        dmpReturnOrderInfoEntity.setBuyerName(returnOrderEntity.getFRetcustName());
        //登记人编号
        dmpReturnOrderInfoEntity.setEmployeeId(returnOrderEntity.getFSaleOrgId());
        //登记人名称
        dmpReturnOrderInfoEntity.setEmployeeName(returnOrderEntity.getFSalesManName());
        //备注
        dmpReturnOrderInfoEntity.setRemark(returnOrderEntity.getFHeadNote());
        //退货信息创建时间
        if (!"null".equals(returnOrderEntity.getFCreateDate()) && StrUtil.isNotEmpty(returnOrderEntity.getFCreateDate())){
            dmpReturnOrderInfoEntity.setReturnCreateTime(LocalDateTime.parse(returnOrderEntity.getFCreateDate()));
        }
        //退款时间
        if (!"null".equals(returnOrderEntity.getFApproveDate()) && StrUtil.isNotEmpty(returnOrderEntity.getFApproveDate())){
            dmpReturnOrderInfoEntity.setRefundTime(LocalDateTime.parse(returnOrderEntity.getFApproveDate()));
        }
        //币种
        dmpReturnOrderInfoEntity.setCurrencyCode(returnOrderEntity.getFSettleCurrCode());
        //平台标识
        dmpReturnOrderInfoEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
        //企业Id
        dmpReturnOrderInfoEntity.setCompanyId(returnOrderEntity.getFSaleOrgId());
        //企业名称
        dmpReturnOrderInfoEntity.setCompanyName(returnOrderEntity.getFSaleOrgName());
        dmpReturnOrderInfoEntity.setIsDeleted(Boolean.FALSE);
        dmpReturnOrderInfoEntity.setCreateTime(LocalDateTime.now());
        dmpReturnOrderInfoEntity.setItemList(initOrderItem(returnOrderEntity));
        return dmpReturnOrderInfoEntity;
    }

    /**
     * 解析退货订单商品数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    public List<DmpReturnOrderItemEntity> initOrderItem(KingdeeReturnOrderEntity returnOrderEntity) {
        List<DmpReturnOrderItemEntity> orderItemList = new ArrayList<>();
        HashMap<String, Integer> skuCountMap = new HashMap<>();
        returnOrderEntity.getItemEntityList().stream().forEach(orderItemBean ->{
            DmpReturnOrderItemEntity dmpReturnOrderItemEntity = new DmpReturnOrderItemEntity();
            //sku编号
            String skuNo = orderItemBean.getFMaterialNumber();
            dmpReturnOrderItemEntity.setSkuNo(skuNo);
            //商品名称
            dmpReturnOrderItemEntity.setItemName(orderItemBean.getFMaterialName());
            //买家购买数量
            dmpReturnOrderItemEntity.setQuantity(Double.valueOf(orderItemBean.getFSalUnitQty()).intValue());
            //商品单位
            dmpReturnOrderItemEntity.setProductUnit(orderItemBean.getFUnitName());
            //商品图片地址
            dmpReturnOrderItemEntity.setPictureUrl("");
            //售价
            dmpReturnOrderItemEntity.setSellPrice(new BigDecimal(orderItemBean.getFPrice()));
            //物品属性
            dmpReturnOrderItemEntity.setSpecifics(orderItemBean.getFMaterialModel());
            //状态 1待处理 2验货入库 3自然耗损
            dmpReturnOrderItemEntity.setStatus(2);
            //erp平台商品id
            String erpOrderItemId = returnOrderEntity.getFOrderNo() + "_" + returnOrderEntity.getFBillNo() + "_" + skuNo;
            erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, skuNo, erpOrderItemId);
            dmpReturnOrderItemEntity.setErpOrderItemId(erpOrderItemId);
            dmpReturnOrderItemEntity.setIsDeleted(Boolean.FALSE);
            dmpReturnOrderItemEntity.setAmountAfter(orderItemBean.getFAllAmount());
            orderItemList.add(dmpReturnOrderItemEntity);
        });
        return orderItemList;
    }
}
