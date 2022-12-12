package com.erp.server.dmp.pull.service.kingdee;

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
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailItemEntity;
import com.erp.model.dmp.kingdee.KingdeeRefundOrderEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpDeliveryDetailInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpDeliveryDetailItemService;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.QueryParam;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 金蝶云星空出库详情
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.SAL_OUTSTOCK)
public class KingdeeDeliveryDetailServiceImpl implements IReportSaveService {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpDeliveryDetailInfoService dmpDeliveryDetailInfoService;

    @Resource
    private DmpDeliveryDetailItemService dmpDeliveryDetailItemService;

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
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 金蝶云星空修改mongodb出库详情失败，[ 单号 = " + outStockEntity.getFBillNo() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(outStockEntity, MongoTableNameContant.ORIGINAL_KINGDEE_DELIVERY_DETAIL);
                }
                //存储数据到中台
                analysisDeliveryDetail(outStockEntity);
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
        try {
            JobTaskDTO jobTask = dto.getJobTaskDTO();
            Integer lastTime = jobTask.getLastTime();
            Integer nextTime = jobTask.getNextTime();
            String st = "";
            String sd = "";
            if (lastTime != 0 && nextTime != 0) {
                Date date = new Date(Long.valueOf(lastTime - (10L*60L)) * 1000L);
                SimpleDateFormat sdf = new SimpleDateFormat(EnumTimePattern.y_m_dhms.toTimePattern());
                st = sdf.format(date);
                sd = sdf.format(new Date(nextTime * 1000L));
                dto.getJobTaskDTO().setLastTime(nextTime);
            } else {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat(EnumTimePattern.y_m_dhms.toTimePattern());
                Calendar cl = Calendar.getInstance();
                cl.setTime(date);
                cl.add(Calendar.DAY_OF_MONTH, -30);
                st = sdf.format(cl.getTime());
                sd = sdf.format(date);
                dto.getJobTaskDTO().setLastTime(Integer.parseInt(String.valueOf(System.currentTimeMillis() / 1000L)));
            }

            //读取配置，初始化SDK
            K3CloudApi client = new K3CloudApi();

            String formId = jobTask.getApiCode();
            LinkedList<String> queryfilters = new LinkedList<>();
            queryfilters.add(String.format("FModifyDate >= '%s'", st));
            queryfilters.add(String.format("FModifyDate <= '%s'", sd));
            queryfilters.add(String.format("FBillTypeID = '%s'", "ad0779a4685a43a08f08d2e42d7bf3e9"));
            String filterStr = String.join(" and ", queryfilters);
            String fieldKeys = "FBillTypeID,FBillTypeID.FName,FBillNo,FSoorDerno,FDate,FSaleOrgId,FSaleOrgId.FName,FCustomerID,FCustomerID.FName,FSaleDeptID.FName,FSalesManID,FSalesManID.FName,FReceiverID.FName,FTransferBizType.FName,F_ulz_BaseProperty2,FLinkPhone,FLinkMan,FBussinessType,FDocumentStatus,FNote,FReceiveAddress,FCreatorId.FName,FCreateDate,FModifierId.FName,FModifyDate,FApproverID.FName,FApproveDate,FCancelStatus,FGYDATE,FLogisticsNos,F_ulz_Text3,FSettleCurrID.FCode,FExchangeRate,"
                    + "FSrcBillNo,F_ulz_BaseProperty1,FCustMatID,FCustMatName,FMaterialID,FMaterialID.FNumber,FMaterialID.FName,FBarcode,FMateriaModel,FMateriaType,FRealQty,FUnitID.FName,FPrice,FIsFree,FArrivalStatus,FArrivalDate,FAmount,FStockStatusID,FStockStatusID.FName,FStockID.FName,F_ulz_Text1,FEntryCostAmount,FEntrynote";

            Boolean dataSign = true;
            //当前页数
            Integer pageIndex = 0;

            //每次最多获取100条
            Integer pageSize = 10000;
            while (dataSign) {
                //请求参数，示例使用的是SDK提供的模板类，还可以使用字符串拼接等方式
                QueryParam param = new QueryParam();
                param.setFormId(formId);
                param.setFieldKeys(fieldKeys);
                if (StringUtils.isNotBlank(st)) {
                    param.setFilterString(filterStr);
                }
                param.setLimit(pageSize);
                //"StartRow\":0,"+// 分页取数开始行索引，从0开始，例如每页10行数据，第2页开始是10，第3页开始是20

                param.setStartRow(pageIndex * pageSize);
                String s = JSONObject.toJSONString(param);

                Map<String, Object> stringObjectMap = null;
                try {
                    List<List<Object>> result = client.executeBillQuery(s);
                    if (!result.isEmpty()) {
                        if (result.size() == 1 && result.get(0).get(0).toString().contains("IsSuccess=false")) {
                            dataSign = false;
                            throw new RuntimeException(" ===== 金蝶云星空解析出库详情信息数据失败 ===== " + result);
                        }

                        for (List<Object> objects : result) {
                            Map<String, String> valMap = KingdeeUtils.keySetValByLinked(fieldKeys, objects);
                            KingdeeDeliveryDetailEntity kingdeeReturnOrderEntity = new KingdeeDeliveryDetailEntity();
                            kingdeeReturnOrderEntity.setFBillTypeID(valMap.get("FBillTypeID"));
                            kingdeeReturnOrderEntity.setFBillTypeName(valMap.get("FBillTypeID.FName"));
                            kingdeeReturnOrderEntity.setFBillNo(valMap.get("FBillNo"));
                            kingdeeReturnOrderEntity.setFSoorDerno(valMap.get("FSoorDerno"));
                            kingdeeReturnOrderEntity.setFDate(valMap.get("FDate"));
                            kingdeeReturnOrderEntity.setFSaleOrgId(valMap.get("FSaleOrgId"));
                            kingdeeReturnOrderEntity.setFSaleOrgName(valMap.get("FSaleOrgId.FName"));
                            kingdeeReturnOrderEntity.setFCustomerID(valMap.get("FCustomerID"));
                            kingdeeReturnOrderEntity.setFCustomerName(valMap.get("FCustomerID.FName"));
                            kingdeeReturnOrderEntity.setFSaleDeptName(valMap.get("FSaleDeptID.FName"));
                            kingdeeReturnOrderEntity.setFSalesManID(valMap.get("FSalesManID"));
                            kingdeeReturnOrderEntity.setFSalesManName(valMap.get("FSalesManID.FName"));
                            kingdeeReturnOrderEntity.setFReceiverName(valMap.get("FReceiverID.FName"));
                            kingdeeReturnOrderEntity.setFTransferBizTypeName(valMap.get("FTransferBizType.FName"));
                            kingdeeReturnOrderEntity.setF_ulz_BaseProperty2(valMap.get("F_ulz_BaseProperty2"));
                            kingdeeReturnOrderEntity.setFLinkPhone(valMap.get("FLinkPhone"));
                            kingdeeReturnOrderEntity.setFLinkMan(valMap.get("FLinkMan"));
                            kingdeeReturnOrderEntity.setFBussinessType(valMap.get("FBussinessType"));
                            kingdeeReturnOrderEntity.setFDocumentStatus(valMap.get("FDocumentStatus"));
                            kingdeeReturnOrderEntity.setFNote(valMap.get("FNote"));
                            kingdeeReturnOrderEntity.setFReceiveAddress(valMap.get("FReceiveAddress"));
                            kingdeeReturnOrderEntity.setFCreatorName(valMap.get("FCreatorId.FName"));
                            kingdeeReturnOrderEntity.setFCreateDate(valMap.get("FCreateDate"));
                            kingdeeReturnOrderEntity.setFModifierName(valMap.get("FModifierId.FName"));
                            kingdeeReturnOrderEntity.setFModifyDate(valMap.get("FModifyDate"));
                            kingdeeReturnOrderEntity.setFApproverName(valMap.get("FApproverID.FName"));
                            kingdeeReturnOrderEntity.setFApproveDate(valMap.get("FApproveDate"));
                            kingdeeReturnOrderEntity.setFCancelStatus(valMap.get("FCancelStatus"));
                            kingdeeReturnOrderEntity.setFGYDATE(valMap.get("FGYDATE"));
                            kingdeeReturnOrderEntity.setFLogisticsNos(valMap.get("FLogisticsNos"));
                            kingdeeReturnOrderEntity.setF_ulz_Text3(valMap.get("F_ulz_Text3"));
                            kingdeeReturnOrderEntity.setFSettleCurrCode(valMap.get("FSettleCurrID.FCode"));
                            kingdeeReturnOrderEntity.setFExchangeRate(valMap.get("FExchangeRate"));

                            //商品信息
                            KingdeeDeliveryDetailItemEntity itemEntity = new KingdeeDeliveryDetailItemEntity();
                            itemEntity.setFBillNo(valMap.get("FBillNo"));
                            itemEntity.setFSrcBillNo(valMap.get("FSrcBillNo"));
                            itemEntity.setF_ulz_BaseProperty1(valMap.get("F_ulz_BaseProperty1"));
                            itemEntity.setFCustMatID(valMap.get("FCustMatID"));
                            itemEntity.setFCustMatName(valMap.get("FCustMatName"));
                            itemEntity.setFMaterialID(valMap.get("FMaterialID"));
                            itemEntity.setFMaterialNumber(valMap.get("FMaterialID.FNumber"));
                            itemEntity.setFMaterialName(valMap.get("FMaterialID.FName"));
                            itemEntity.setFBarcode(valMap.get("FBarcode"));
                            itemEntity.setFMateriaModel(valMap.get("FMateriaModel"));
                            itemEntity.setFMateriaType(valMap.get("FMateriaType"));
                            itemEntity.setFRealQty(valMap.get("FRealQty"));
                            itemEntity.setFUnitName(valMap.get("FUnitID.FName"));
                            itemEntity.setFPrice(valMap.get("FPrice"));
                            itemEntity.setFIsFree(valMap.get("FIsFree"));
                            itemEntity.setFArrivalStatus(valMap.get("FArrivalStatus"));
                            itemEntity.setFArrivalDate(valMap.get("FArrivalDate"));
                            itemEntity.setFAmount(valMap.get("FAmount"));
                            itemEntity.setFStockStatusID(valMap.get("FStockStatusID"));
                            itemEntity.setFStockStatusName(valMap.get("FStockStatusID.FName"));
                            itemEntity.setFStockName(valMap.get("FStockID.FName"));
                            itemEntity.setF_ulz_Text1(valMap.get("F_ulz_Text1"));
                            itemEntity.setFEntryCostAmount(valMap.get("FEntryCostAmount"));
                            itemEntity.setFEntrynote(valMap.get("FEntrynote"));

                            KingdeeDeliveryDetailEntity entity = infoArrayList.stream().filter(a -> a.getFBillNo().equals(valMap.get("FBillNo")) && a.getFSoorDerno().equals(valMap.get("FSoorDerno"))).findFirst().orElse(null);
                            if (entity != null) {
                                entity.getKingdeeOutStockItemEntityList().add(itemEntity);
                            } else {
                                List<KingdeeDeliveryDetailItemEntity> outStockItemEntityList = new ArrayList();
                                outStockItemEntityList.add(itemEntity);
                                kingdeeReturnOrderEntity.setKingdeeOutStockItemEntityList(outStockItemEntityList);
                                infoArrayList.add(kingdeeReturnOrderEntity);
                            }
                        }
                    } else {
                        dataSign = false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("请求接口地址异常 错误信息：" + e.getMessage());
                    DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                    dmpErrorLogEntity.setTaskId(jobTask.getId());
                    dmpErrorLogEntity.setParams("");
                    dmpErrorLogEntity.setErrorMsg(e.getMessage());
                    dmpErrorLogEntity.setReturnMsg(JSONObject.toJSONString(stringObjectMap));
                    dmpErrorLogEntity.setCreateTime(new Date());
                    dmpErrorLogService.add(dmpErrorLogEntity);
                    dataSign = false;
                }
                pageIndex++;
            }
        } catch (Exception e) {
            log.info(" ===== 获取金蝶云星空出库详情数据失败， 错误信息 = { " + e.getMessage() + " }");
            throw new RuntimeException(" ===== 获取金蝶云星空出库详情数据失败， 错误信息 = { " + e.getMessage() + " }");
        }
        return infoArrayList;
    }

    /**
     * 解析订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    @Transactional
    public void analysisDeliveryDetail(KingdeeDeliveryDetailEntity kingdeeOutStockEntity) throws Exception {
        DmpDeliveryDetailInfoEntity deliveryDetailInfoEntity = new DmpDeliveryDetailInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

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
            orderTotalCost = orderTotalCost.add(BigDecimal.valueOf(Double.valueOf(itemEntity.getFAmount())));
            itemTotalCost = itemTotalCost.add(BigDecimal.valueOf(Double.valueOf(itemEntity.getFEntryCostAmount())));
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
        deliveryDetailInfoEntity.setCurrencyRate(BigDecimal.valueOf(Double.valueOf(kingdeeOutStockEntity.getFExchangeRate())));

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
            deliveryDetailInfoEntity.setPlatformApproveTime(sdf.parse(kingdeeOutStockEntity.getFApproveDate()));
        }

        //平台单据创建时间
        if (StringUtils.isNotBlank(kingdeeOutStockEntity.getFCreateDate()) && !kingdeeOutStockEntity.getFCreateDate().equals("null")) {
            deliveryDetailInfoEntity.setPlatformCreateTime(sdf.parse(kingdeeOutStockEntity.getFCreateDate()));
        }

        //平台单据修改时间
        if (StringUtils.isNotBlank(kingdeeOutStockEntity.getFModifyDate()) && !kingdeeOutStockEntity.getFModifyDate().equals("null")) {
            deliveryDetailInfoEntity.setPlatformUpdateTime(sdf.parse(kingdeeOutStockEntity.getFModifyDate()));
        }

        //发货时间 //TODO 暂无
        deliveryDetailInfoEntity.setDeliveryDate(null);

        //备注
        deliveryDetailInfoEntity.setRemark(kingdeeOutStockEntity.getFNote());

        //平台标识
        deliveryDetailInfoEntity.setPlatformSign("金蝶云星空");

        //企业Id
        deliveryDetailInfoEntity.setCompanyId(kingdeeOutStockEntity.getFSaleOrgId());

        //企业名称
        deliveryDetailInfoEntity.setCompanyName(kingdeeOutStockEntity.getFSaleOrgName());

        //创建时间
        deliveryDetailInfoEntity.setCreateTime(new Date());

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
    @Transactional
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
            dmpReturnOrderItemEntity.setCostPrice(BigDecimal.valueOf(Double.valueOf(itemEntity.getFEntryCostAmount())));

            //商品售价
            dmpReturnOrderItemEntity.setSellPrice(BigDecimal.valueOf(Double.valueOf(itemEntity.getFPrice())));

            //商品数量
            dmpReturnOrderItemEntity.setQuantity(Double.valueOf(itemEntity.getFRealQty()).intValue());

            dmpReturnOrderItemEntity.setAmount((BigDecimal.valueOf(Double.valueOf(itemEntity.getFAmount()))));

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
            dmpReturnOrderItemEntity.setItemRemark(itemEntity.getFEntrynote());

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
