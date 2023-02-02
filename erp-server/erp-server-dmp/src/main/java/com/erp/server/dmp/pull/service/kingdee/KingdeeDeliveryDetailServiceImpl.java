package com.erp.server.dmp.pull.service.kingdee;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.KingdeeOutStockDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.DmpDeliveryDetailItemEntity;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.kingdee.*;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpDeliveryDetailInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpDeliveryDetailItemService;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
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

import static com.erp.server.dmp.pull.service.kingdee.KingdeeOrderInfoServiceImpl.ORG_CODE;

/**
 * 金蝶云星空出库详情
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.SAL_OUTSTOCK)
public class KingdeeDeliveryDetailServiceImpl implements IReportSaveService<KingdeeDeliveryDetailEntity> {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpDeliveryDetailInfoService dmpDeliveryDetailInfoService;

    @Resource
    private DmpDeliveryDetailItemService dmpDeliveryDetailItemService;

    @Resource
    @Qualifier("kingdeeDeliveryDetailServiceImpl")
    private IReportSaveService reportSaveService;

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<KingdeeDeliveryDetailEntity> outStockEntityList = pullDate(dto);
        if (outStockEntityList != null && outStockEntityList.size() > 0) {
            for (KingdeeDeliveryDetailEntity outStockEntity : outStockEntityList) {
                KingdeeOutStockDTO outStockDTO = new KingdeeOutStockDTO();
                outStockDTO.setBillNo(outStockEntity.getFBillNo());
                outStockDTO.setSoorDerno(outStockEntity.getFSoorDerno());
                List<KingdeeRefundOrderEntity> mongoData = mongoService.findMongoData(outStockDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_DELIVERY_DETAIL, KingdeeRefundOrderEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (KingdeeRefundOrderEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(outStockEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(outStockEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(outStockDTO, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_DELIVERY_DETAIL, KingdeeRefundOrderEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 金蝶云星空修改mongodb出库详情失败，[ 单号 = " + outStockEntity.getFBillNo() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 金蝶云星空修改mongodb出库详情失败，[ 单号 = " + outStockEntity.getFBillNo() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(outStockEntity, MongoTableNameContant.ORIGINAL_KINGDEE_DELIVERY_DETAIL);
                }
                //存储数据到中台
                reportSaveService.analysisOrder(outStockEntity);
            }
        }
    }

    /**
     * 请求金蝶云星空出库详情接口
     * @param dto
     * @return
     */
    public List<KingdeeDeliveryDetailEntity> pullDate(RequestDTO dto) {
        List<KingdeeDeliveryDetailEntity> infoArrayList = new ArrayList<>();
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        LinkedList<String> queryFilters = new LinkedList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        queryFilters.add(String.format("FModifyDate >= '%s'", sdf.format(lastTime.minusMinutes(2))));
        queryFilters.add(String.format("FModifyDate <= '%s'", sdf.format(nextTime)));
        queryFilters.add(String.format("FBillTypeID = '%s'", "ad0779a4685a43a08f08d2e42d7bf3e9"));
        queryFilters.add(String.format("FDocumentStatus = '%s'", "C"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FBillTypeID,FBillTypeID.FName,FBillNo,FSoorDerno,FDate,FSaleOrgId,FSaleOrgId.FName," +
                "FCustomerID,FCustomerID.FName,FSaleDeptID.FName,FSalesManID,FSalesManID.FName,FReceiverID.FName," +
                "FTransferBizType.FName,F_ulz_BaseProperty2,FLinkPhone,FLinkMan,FBussinessType,FDocumentStatus," +
                "FNote,FReceiveAddress,FCreatorId.FName,FCreateDate,FModifierId.FName,FModifyDate,FApproverID.FName," +
                "FApproveDate,FCancelStatus,FGYDATE,FLogisticsNos,F_ulz_Text3,FSettleCurrID.FCode,FExchangeRate,"
                + "FSrcBillNo,F_ulz_BaseProperty1,FCustMatID,FCustMatName,FMaterialID,FMaterialID.FNumber,FMaterialID.FName," +
                "FBarcode,FMateriaModel,FMateriaType,FRealQty,FUnitID.FName,FPrice,FIsFree,FArrivalStatus,FArrivalDate," +
                "FAmount,FStockStatusID,FStockStatusID.FName,FStockID.FName,F_ulz_Text1,FEntryCostAmount,FEntrynote";

        Boolean dataSign = true;
        //当前页数
        Integer pageIndex = 0;

        //每次最多获取100条
        Integer pageSize = 10000;
        while (dataSign) {
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName());
            List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            XxlJobHelper.log("获取金蝶发货数据第[{}]页 有{}条记录", pageIndex, pageSize);
            if (result.size() < pageSize){
                dataSign = false;
            }
            if (CollectionUtil.isEmpty(result)) {
                return Collections.emptyList();
            }
            List<KingdeeDeliveryDetailEntity> entityList = result.stream().map(shopEntity ->
                    BeanUtil.toBean(shopEntity, KingdeeDeliveryDetailEntity.class)).collect(Collectors.toList());

            Map<String, List<KingdeeDeliveryDetailItemEntity>> itemMap = result.stream().map(entity ->
                            BeanUtil.toBean(entity, KingdeeDeliveryDetailItemEntity.class))
                    .collect(Collectors.groupingBy(m -> StrUtil.format("{}_{}", m.getFBillNo(), m.getFSoorDerno())));
            entityList.stream().peek(m -> m.setKingdeeOutStockItemEntityList(itemMap.get(StrUtil.format("{}_{}", m.getFBillNo(), m.getFSoorDerno()))))
                    .collect(Collectors.toList());
            infoArrayList.addAll(entityList);
            pageIndex ++;
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
    public void analysisOrder(KingdeeDeliveryDetailEntity kingdeeOutStockEntity) throws Exception {
        // 跳过非唯迹订单
        if (StrUtil.isEmpty(kingdeeOutStockEntity.getFSaleOrgId()) || !ORG_CODE.equals(kingdeeOutStockEntity.getFSaleOrgId())){
            return;
        }
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        DmpDeliveryDetailInfoEntity deliveryDetailInfoEntity = new DmpDeliveryDetailInfoEntity();

        //单据编号
        deliveryDetailInfoEntity.setBillNo(kingdeeOutStockEntity.getFBillNo());

        //订单编号
        deliveryDetailInfoEntity.setOrderNo(kingdeeOutStockEntity.getFSoorDerno());

        //物流单号
        deliveryDetailInfoEntity.setLogisticsNo(kingdeeOutStockEntity.getFLogisticsNos());

        //客户名称
        deliveryDetailInfoEntity.setCustomerName(kingdeeOutStockEntity.getFCustomerName());

        //平台名称
        if (StringUtils.isNotBlank(kingdeeOutStockEntity.getF_ulz_BaseProperty2()) && kingdeeOutStockEntity.getF_ulz_BaseProperty2().equals("null")) {
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
            orderTotalCost = orderTotalCost.add(new BigDecimal(itemEntity.getFAmount()));
            itemTotalCost = itemTotalCost.add(new BigDecimal(itemEntity.getFEntryCostAmount()));
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
            deliveryDetailInfoEntity.setPlatformApproveTime(LocalDateTime.parse(kingdeeOutStockEntity.getFApproveDate(), sdf));
        }

        //平台单据创建时间
        if (StringUtils.isNotBlank(kingdeeOutStockEntity.getFCreateDate()) && !kingdeeOutStockEntity.getFCreateDate().equals("null")) {
            deliveryDetailInfoEntity.setPlatformCreateTime(LocalDateTime.parse(kingdeeOutStockEntity.getFCreateDate(), sdf));
        }

        //平台单据修改时间
        if (StringUtils.isNotBlank(kingdeeOutStockEntity.getFModifyDate()) && !kingdeeOutStockEntity.getFModifyDate().equals("null")) {
            deliveryDetailInfoEntity.setPlatformUpdateTime(LocalDateTime.parse(kingdeeOutStockEntity.getFModifyDate(), sdf));
        }

        //发货时间
        if (StringUtils.isNotBlank(kingdeeOutStockEntity.getFDate()) && !kingdeeOutStockEntity.getFDate().equals("null")) {
            deliveryDetailInfoEntity.setDeliveryDate(LocalDateTime.parse(kingdeeOutStockEntity.getFDate(), sdf));
        }

        //备注
        deliveryDetailInfoEntity.setRemark(kingdeeOutStockEntity.getFNote());

        //平台标识
        deliveryDetailInfoEntity.setPlatformSign("金蝶云星空");

        //企业Id
        deliveryDetailInfoEntity.setCompanyId(kingdeeOutStockEntity.getFSaleOrgId());

        //企业名称
        deliveryDetailInfoEntity.setCompanyName(kingdeeOutStockEntity.getFSaleOrgName());

        //创建时间
        deliveryDetailInfoEntity.setCreateTime(LocalDateTime.now());

        //新增订单信息
        String deliveryDetailId = dmpDeliveryDetailInfoService.checkOrder(deliveryDetailInfoEntity);
        if (StringUtils.isNotBlank(deliveryDetailId)) {
            //新增订单商品信息
            analysisReturnOrderItem(kingdeeOutStockEntity.getKingdeeOutStockItemEntityList(), deliveryDetailId);
        }
    }

    /**
     * 解析出库详情商品数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    public void analysisReturnOrderItem(List<KingdeeDeliveryDetailItemEntity> orderItem, String deliveryDetailId) {
        List<DmpDeliveryDetailItemEntity> orderItemList = new ArrayList<>();
        for (KingdeeDeliveryDetailItemEntity itemEntity : orderItem) {
            DmpDeliveryDetailItemEntity dmpReturnOrderItemEntity = new DmpDeliveryDetailItemEntity();

            //发货详情表id
            dmpReturnOrderItemEntity.setDeliveryDetailId(deliveryDetailId);

            //商品id
            dmpReturnOrderItemEntity.setItemId(itemEntity.getFMaterialID());

            //平台sku
            dmpReturnOrderItemEntity.setPlatformSku(itemEntity.getF_ulz_BaseProperty1());

            //商品sku编号
            dmpReturnOrderItemEntity.setSkuNo(itemEntity.getFMaterialNumber());

            //商品名称
            dmpReturnOrderItemEntity.setItemName(itemEntity.getFMaterialName());

            //商品成本价
            dmpReturnOrderItemEntity.setCostPrice(new BigDecimal(itemEntity.getFEntryCostAmount()));

            //商品售价
            dmpReturnOrderItemEntity.setSellPrice(new BigDecimal(itemEntity.getFPrice()));

            //商品数量
            dmpReturnOrderItemEntity.setQuantity(Double.valueOf(itemEntity.getFRealQty()).intValue());

            dmpReturnOrderItemEntity.setAmount((new BigDecimal(itemEntity.getFAmount())));

            //商品单位
            dmpReturnOrderItemEntity.setProductUnit(itemEntity.getFUnitName());

            //是否是赠品 1. 是 2. 否
            if (Boolean.valueOf(itemEntity.getFIsFree())){
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
        }
        checkOrderItem(orderItemList, deliveryDetailId);
    }

    /**
     * 校验出库详情商品信息在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    public void checkOrderItem(List<DmpDeliveryDetailItemEntity> orderItem, String deliveryDetailId) {
        dmpDeliveryDetailItemService.deleteDeliveryDetailItemByDetailId(deliveryDetailId);
        dmpDeliveryDetailItemService.batchAdd(orderItem);
    }
}
