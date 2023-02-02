package com.erp.server.dmp.pull.service.kingdee;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderItemEntity;
import com.erp.model.dmp.kingdee.KingdeeShopEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderItemService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.QueryParam;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
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
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpReturnOrderInfoService dmpReturnOrderInfoService;

    @Resource
    private DmpReturnOrderItemService dmpReturnOrderItemService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    @Qualifier("kingdeeReturnOrderInfoImpl")
    private IReportSaveService reportSaveService;

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<KingdeeReturnOrderEntity> orderEntities = pullDate(dto);
        if (orderEntities != null && orderEntities.size() > 0) {
            for (KingdeeReturnOrderEntity orderEntity : orderEntities) {
                OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
                orderMongoDTO.setBillNo(orderEntity.getFBillNo());
                orderMongoDTO.setOrderNo(orderEntity.getFOrderNo());
                List<KingdeeReturnOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_RETURN_ORDER, KingdeeReturnOrderEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (KingdeeReturnOrderEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(orderEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(orderEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_RETURN_ORDER, KingdeeReturnOrderEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 金蝶云星空修改mongodb销售出库数据失败，[ 单号 = " + orderEntity.getFBillNo() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 金蝶云星空修改mongodb销售出库数据失败，[ 单号 = " + orderEntity.getFBillNo() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(orderEntity, MongoTableNameContant.ORIGINAL_KINGDEE_RETURN_ORDER);
                }
                //存储数据到中台
                reportSaveService.analysisOrder(orderEntity);
            }
        }
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
        queryFilters.add(String.format("FOrderNo <> '%s'", ""));
        queryFilters.add(String.format("FDocumentStatus = '%s'", "C"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FBillTypeID,FBillTypeID.FName,FBillTypeID.FNumber,FBillNo,FDate,FDocumentStatus,FSaleOrgId,FSaleOrgId.FName,FRetcustId," +
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
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName());
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
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void analysisOrder(KingdeeReturnOrderEntity returnOrderEntity) throws Exception {
        if (StrUtil.isEmpty(returnOrderEntity.getFSaleOrgId()) || !"1".equals(returnOrderEntity.getFSaleOrgId())){
            return;
        }
        DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity = new DmpReturnOrderInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

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
        if (StringUtils.isNotBlank(returnOrderEntity.getFDelTime()) && !returnOrderEntity.getFDelTime().equals("null")) {
            dmpReturnOrderInfoEntity.setExpressTime(sdf.parse(returnOrderEntity.getFDelTime()));
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
        dmpReturnOrderInfoEntity.setSalesRecordNumber("");
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

        /**
         * 平台名称
         */
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
        if (StringUtils.isNotBlank(returnOrderEntity.getFCreateDate()) && !returnOrderEntity.getFCreateDate().equals("null")) {
            dmpReturnOrderInfoEntity.setReturnCreateTime(sdf.parse(returnOrderEntity.getFCreateDate()));
        }

        //退款时间
        if (StringUtils.isNotBlank(returnOrderEntity.getFApproveDate()) && !returnOrderEntity.getFApproveDate().equals("null")) {
            dmpReturnOrderInfoEntity.setRefundTime(sdf.parse(returnOrderEntity.getFApproveDate()));
        }

        //币种
        dmpReturnOrderInfoEntity.setCurrencyCode(returnOrderEntity.getFSettleCurrCode());

        //平台标识
        dmpReturnOrderInfoEntity.setPlatformSign("金蝶云星空");

        //企业Id
        dmpReturnOrderInfoEntity.setCompanyId(returnOrderEntity.getFSaleOrgId());

        //企业名称
        dmpReturnOrderInfoEntity.setCompanyName(returnOrderEntity.getFSaleOrgName());

        dmpReturnOrderInfoEntity.setCreateTime(LocalDateTime.now());

        //新增订单信息
        String orderInfoId = dmpReturnOrderInfoService.checkOrder(dmpReturnOrderInfoEntity);
        if (StringUtils.isNotBlank(orderInfoId)) {
            //新增订单商品信息
            analysisReturnOrderItem(returnOrderEntity.getItemEntityList(), orderInfoId);
        }
    }

    /**
     * 解析退货订单商品数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    public void analysisReturnOrderItem(List<KingdeeReturnOrderItemEntity> orderItem, String orderId) {
        List<DmpReturnOrderItemEntity> orderItemList = new ArrayList<>();
        for (KingdeeReturnOrderItemEntity orderItemBean : orderItem) {
            DmpReturnOrderItemEntity dmpReturnOrderItemEntity = new DmpReturnOrderItemEntity();
            //退货订单表id
            dmpReturnOrderItemEntity.setReturnOrderId(orderId);

            //sku编号
            dmpReturnOrderItemEntity.setSkuNo(orderItemBean.getFMaterialNumber());

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

            dmpReturnOrderItemEntity.setAmountAfter(orderItemBean.getFAllAmount());

            orderItemList.add(dmpReturnOrderItemEntity);
        }
        checkOrderItem(orderItemList, orderId);
    }

    /**
     * 校验退货订单商品信息在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    public void checkOrderItem(List<DmpReturnOrderItemEntity> orderItem, String returnOrderId) {
        dmpReturnOrderItemService.deleteOrderByReturnOrderId(returnOrderId);
        dmpReturnOrderItemService.batchAdd(orderItem);
    }
}
