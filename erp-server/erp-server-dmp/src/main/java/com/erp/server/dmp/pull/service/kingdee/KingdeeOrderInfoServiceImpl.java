package com.erp.server.dmp.pull.service.kingdee;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.kingdee.KingdeeOrderEntity;
import com.erp.model.dmp.kingdee.KingdeeOrderItemEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpOrderItemService;
import com.erp.server.dmp.pull.service.dmp.DmpShopInfoService;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.QueryParam;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 金蝶云星空订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.SAL_SALEORDER)
public class KingdeeOrderInfoServiceImpl implements IReportSaveService {

    /**
     * 唯迹集团组织ID
     */
    private static final String ORG_CODE = "1";
    /**
     * 可用销售订单CODE
     */
    private static final List<String> ORDER_TYPES = new ArrayList<>(Arrays.asList("B2BXSDD","XSDD01_SYS"));

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpOrderItemService dmpOrderItemService;

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public static void main(String[] args) {
        KingdeeOrderInfoServiceImpl kingdeeOrderInfoService = new KingdeeOrderInfoServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.SAL_SALEORDER.taskName);
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(PlatformApiEnum.SAL_SALEORDER);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<KingdeeOrderEntity> kingdeeOrderEntities = kingdeeOrderInfoService.pullDate(requestDTO);
        System.out.println(kingdeeOrderEntities);
    }

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<KingdeeOrderEntity> orderEntities = pullDate(dto);

        if (orderEntities != null && orderEntities.size() > 0) {
            for (KingdeeOrderEntity orderEntity : orderEntities) {
                OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
                orderMongoDTO.setBillNo(orderEntity.getFBillNo());
                List<KingdeeOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER, KingdeeOrderEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (KingdeeOrderEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(orderEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(orderEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER, KingdeeOrderEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 金蝶云星空修改mongodb订单数据失败，[ 订单号 = " + orderEntity.getFBillNo() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 金蝶云星空修改mongodb订单数据失败，[ 订单号 = " + orderEntity.getFBillNo() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(orderEntity, MongoTableNameContant.ORIGINAL_KINGDEE_ORDER);
                }
                //存储数据到中台
                analysisOrder(orderEntity);
            }
        }
    }

    /**
     * 请求金蝶云星空订单接口
     * @param dto
     * @return
     */
    public List<KingdeeOrderEntity> pullDate(RequestDTO dto) {
        List<KingdeeOrderEntity> infoArrayList = new ArrayList<>();
        try {
            LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
            LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
            String st = "";
            String sd = "";
            if (dto.getJobTaskDTO().getLastTime() != null && dto.getJobTaskDTO().getNextTime() != null) {
                LocalDateTime localDateTime = lastTime.minusMinutes(5);
                DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
                st = sdf.format(localDateTime);
                sd = sdf.format(nextTime);
                dto.getJobTaskDTO().setLastTime(nextTime);
            } else {
                LocalDateTime date = LocalDateTime.now();
                DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
                LocalDateTime localDateTime = date.minusDays(30);
                st = sdf.format(localDateTime);
                sd = sdf.format(date);
                dto.getJobTaskDTO().setLastTime(date);
            }

            //读取配置，初始化SDK
            K3CloudApi client = new K3CloudApi();

            String formId = dto.getJobTaskDTO().getApiCode();
            LinkedList<String> queryfilters = new LinkedList<>();
            queryfilters.add(String.format("FModifyDate >= '%s'", st));
            queryfilters.add(String.format("FModifyDate <= '%s'", sd));
//            queryfilters.add(String.format("fCreateDate >= '%s'", "2023-01-05 00:00:00"));
//            queryfilters.add(String.format("fCreateDate <= '%s'", "2023-01-06 00:00:00"));
//            queryfilters.add(StrUtil.format("FBillNo ='{}'", "XSD-20230105-33831"));
            // 移除 订单类型过滤
//            queryfilters.add(String.format("fBillTypeID = '%s'", "eacb50844fc84a10b03d7b841f3a6278"));
            queryfilters.add(String.format("FDocumentStatus = '%s'", "C"));
            String filterStr = String.join(" and ",  queryfilters );
            String fieldKeys = "FID,FBillNo,FDate,FBillTypeId.FName,FBillTypeId.FNumber,FBillTypeId,FDocumentStatus,FCustId.FName,FSaleDeptId.FName,FSalerId.FName,FReceiveAddress,FLinkMan,FLinkPhone,FApproverId.FName,FApproveDate,FCloseStatus,FCloseDate,FCancelStatus,FChangerId,FReceiveId.FName,FNote,FHeadDeliveryWay,FHEADLOCID,FCorrespondOrgId,FSaleGroupId,FChangeReason,FBusinessType,FReceiveContact,FChargeId,FCreatorId,FCreateDate,FModifierId,FModifyDate,FSaleOrgId,FSaleOrgId.FName,FVersionNo,FSignStatus,FSOFrom,F_SK_Date,F_SHGJ1,FExchangeRate,FSettleCurrId.FCode";

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
                param.setFilterString(filterStr);
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
                            throw new RuntimeException(" ===== 金蝶云星空解析采购订单数据失败 ===== " + result);
                        }

                        for (List<Object> objects : result) {
                            Map<String, String> stringStringMap = KingdeeUtils.keySetValByLinked(fieldKeys, objects);
                            KingdeeOrderEntity orderEntity = new KingdeeOrderEntity();
                            orderEntity.setFID(stringStringMap.get("FID"));
                            orderEntity.setFBillNo(stringStringMap.get("FBillNo"));
                            orderEntity.setFDate(stringStringMap.get("FDate"));
                            orderEntity.setFBillTypeID(stringStringMap.get("FBillTypeId.FName"));
                            orderEntity.setFBillTypeCode(stringStringMap.get("FBillTypeId.FNumber"));
                            orderEntity.setFDocumentStatus(stringStringMap.get("FDocumentStatus"));
                            orderEntity.setFCustId(stringStringMap.get("FCustId.FName"));
                            orderEntity.setFSaleDeptId(stringStringMap.get("FSaleDeptId.FName"));
                            orderEntity.setFSalerId(stringStringMap.get("FSalerId.FName"));
                            orderEntity.setFReceiveAddress(stringStringMap.get("FReceiveAddress"));
                            orderEntity.setFLinkMan(stringStringMap.get("FLinkMan"));
                            orderEntity.setFLinkPhone(stringStringMap.get("FLinkPhone"));
                            orderEntity.setFApproverId(stringStringMap.get("FApproverId.FName"));
                            orderEntity.setFApproveDate(stringStringMap.get("FApproveDate"));
                            orderEntity.setFCloseStatus(stringStringMap.get("FCloseStatus"));
                            orderEntity.setFCloseDate(stringStringMap.get("FCloseDate"));
                            orderEntity.setFCancelStatus(stringStringMap.get("FCancelStatus"));
                            orderEntity.setFChangerId(stringStringMap.get("FChangerId"));
                            orderEntity.setFReceiveId(stringStringMap.get("FReceiveId.FName"));
                            orderEntity.setFNote(stringStringMap.get("FNote"));
                            orderEntity.setFHeadDeliveryWay(stringStringMap.get("FHeadDeliveryWay"));
                            orderEntity.setFHEADLOCID(stringStringMap.get("FHEADLOCID"));
                            orderEntity.setFCorrespondOrgId(stringStringMap.get("FCorrespondOrgId"));
                            orderEntity.setFSaleGroupId(stringStringMap.get("FSaleGroupId"));
                            orderEntity.setFChangeReason(stringStringMap.get("FChangeReason"));
                            orderEntity.setFBusinessType(stringStringMap.get("FBusinessType"));
                            orderEntity.setFReceiveContact(stringStringMap.get("FReceiveContact"));
                            orderEntity.setFChargeId(stringStringMap.get("FChargeId"));
                            orderEntity.setFCreatorId(stringStringMap.get("FCreatorId"));
                            orderEntity.setFCreateDate(stringStringMap.get("FCreateDate"));
                            orderEntity.setFModifierId(stringStringMap.get("FModifierId"));
                            orderEntity.setFModifyDate(stringStringMap.get("FModifyDate"));
                            orderEntity.setFSaleOrgId(stringStringMap.get("FSaleOrgId"));
                            orderEntity.setFSaleOrgName(stringStringMap.get("FSaleOrgId.FName"));
                            orderEntity.setFVersionNo(stringStringMap.get("FVersionNo"));
                            orderEntity.setFSignStatus(stringStringMap.get("FSignStatus"));
                            orderEntity.setFSOFrom(stringStringMap.get("FSOFrom"));
                            orderEntity.setF_SK_Date(stringStringMap.get("F_SK_Date"));
                            orderEntity.setFSHGJ1(stringStringMap.get("F_SHGJ1"));
                            orderEntity.setFExchangeRate(new BigDecimal(stringStringMap.get("FExchangeRate")));
                            orderEntity.setFSettleCurrId(stringStringMap.get("FSettleCurrId.FCode"));

                            LinkedList<String> queryfilterst = new LinkedList<>();
                            queryfilterst.add(String.format("FBillNo = '%s'", orderEntity.getFBillNo()));
                            queryfilterst.add(String.format("FID = '%s'", orderEntity.getFID()));
                            String filterStrt = String.join(" and ", queryfilterst);
                            String fieldKeyst = "FBillNo,FReturnType,FRowType,FMaterialName,FMaterialGroup,FMaterialId,FMaterialId.FNumber,FMaterialModel,FQty,FPriceUnitQty," +
                                    "FUnitID,FAuxPropId,FPrice,FEntryTaxRate,FTaxPrice,FIsFree,FEntryTaxAmount,FMaterialType,FAmount,FBarcode,FMapName,F_ulz_BaseProperty,FMapId," +
                                    "FBaseUnitId,FOldQty,FTaxNetPrice,FDiscount,FPriceDiscount,FBranchId,FEntryNote,FSrcType,FSrcBillNo,FMinPlanDeliveryDate,FDeliveryStatus," +
                                    "F_ulz_Decimal,F_ulz_CGCB,FSOStockId.FName,FAllAmount";
                            param.setFormId(dto.getJobTaskDTO().getApiCode());
                            param.setFieldKeys(fieldKeyst);
                            param.setFilterString(filterStrt);
                            param.setLimit(10000);
                            param.setStartRow(pageIndex);
                            param.setTopRowCount(10000);
                            List<KingdeeOrderItemEntity> orderItemEntityList = new ArrayList<>();
                            List<List<Object>> resultTwo = client.executeBillQuery(JSONObject.toJSONString(param));
                            if (!resultTwo.isEmpty()) {
                                if (resultTwo.size() == 1 && resultTwo.get(0).get(0).toString().contains("IsSuccess=false")) {
                                    dataSign = false;
                                    throw new RuntimeException(" ===== 金蝶云星空解析采购订单数据失败 ===== ");
                                }

                                for (List<Object> objectList : resultTwo) {
                                    Map<String, String> mapItem = KingdeeUtils.keySetValByLinked(fieldKeyst, objectList);
                                    KingdeeOrderItemEntity orderItemEntity = new KingdeeOrderItemEntity();
                                    orderItemEntity.setFBillNo(mapItem.get("FBillNo"));
                                    orderItemEntity.setFReturnType(mapItem.get("FReturnType"));
                                    orderItemEntity.setFRowType(mapItem.get("FRowType"));
                                    orderItemEntity.setFMaterialName(mapItem.get("FMaterialName"));
                                    orderItemEntity.setFMaterialGroup(mapItem.get("FMaterialGroup"));
                                    orderItemEntity.setFMaterialId(mapItem.get("FMaterialId"));
                                    orderItemEntity.setFMaterialNumber(mapItem.get("FMaterialId.FNumber"));
                                    orderItemEntity.setFMaterialModel(mapItem.get("FMaterialModel"));
                                    orderItemEntity.setFQty(new BigDecimal(mapItem.get("FQty")));
                                    orderItemEntity.setFPriceUnitQty(mapItem.get("FPriceUnitQty"));
                                    orderItemEntity.setFUnitID(mapItem.get("FUnitID"));
                                    orderItemEntity.setFAuxPropId(mapItem.get("FAuxPropId"));
                                    orderItemEntity.setFPrice(new BigDecimal(mapItem.get("FPrice")));
                                    orderItemEntity.setFEntryTaxRate(mapItem.get("FEntryTaxRate"));
                                    orderItemEntity.setFTaxPrice(mapItem.get("FTaxPrice"));
                                    orderItemEntity.setFIsFree(mapItem.get("FIsFree"));
                                    orderItemEntity.setFEntryTaxAmount(mapItem.get("FEntryTaxAmount"));
                                    orderItemEntity.setFMaterialType(mapItem.get("FMaterialType"));
                                    orderItemEntity.setFAmount(mapItem.get("FAmount"));
                                    orderItemEntity.setFBarcode(mapItem.get("FBarcode"));
                                    orderItemEntity.setFMapName(mapItem.get("FMapName"));
                                    orderItemEntity.setF_ulz_BaseProperty(mapItem.get("F_ulz_BaseProperty"));
                                    orderItemEntity.setFMapId(mapItem.get("FMapId"));
                                    orderItemEntity.setFBaseUnitId(mapItem.get("FBaseUnitId"));
                                    orderItemEntity.setFOldQty(new BigDecimal(mapItem.get("FOldQty")).intValue());
                                    orderItemEntity.setFTaxNetPrice(mapItem.get("FTaxNetPrice"));
                                    orderItemEntity.setFDiscount(mapItem.get("FDiscount"));
                                    orderItemEntity.setFPriceDiscount(mapItem.get("FPriceDiscount"));
                                    orderItemEntity.setFBranchId(mapItem.get("FBranchId"));
                                    orderItemEntity.setFEntryNote(mapItem.get("FEntryNote"));
                                    orderItemEntity.setFSrcType(mapItem.get("FSrcType"));
                                    orderItemEntity.setFSrcBillNo(mapItem.get("FSrcBillNo"));
                                    orderItemEntity.setFMinPlanDeliveryDate(mapItem.get("FMinPlanDeliveryDate"));
                                    orderItemEntity.setFDeliveryStatus(mapItem.get("FDeliveryStatus"));
                                    orderItemEntity.setF_ulz_Decimal(new BigDecimal(mapItem.get("F_ulz_Decimal")));
                                    orderItemEntity.setF_ulz_CGCB(new BigDecimal(mapItem.get("F_ulz_CGCB")));
                                    orderItemEntity.setFSOStockId(mapItem.get("FSOStockId.FName"));
                                    orderItemEntity.setFAllAmount(new BigDecimal(mapItem.get("FAllAmount")));
                                   orderItemEntityList.add(orderItemEntity);
                                }
                            }
                            orderEntity.setOrderItemEntityList(orderItemEntityList);
                            infoArrayList.add(orderEntity);
                        }
                    } else {
                        dataSign = false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("请求接口地址异常 错误信息：", e);
                    Integer errorCount = dto.getJobTaskDTO().getErrorCount();
                    if (errorCount < 3) {
                        dto.getJobTaskDTO().setErrorCount(errorCount + 1);
                        redisTemplate.boundListOps(dto.getJobTaskDTO().getTaskName()).leftPush(JSONObject.toJSONString(dto.getJobTaskDTO()));
                    } else {
                        DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                        dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                        dmpErrorLogEntity.setParams("");
                        dmpErrorLogEntity.setErrorMsg(e.getMessage());
                        dmpErrorLogEntity.setReturnMsg(JSONObject.toJSONString(stringObjectMap));
                        dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
                        dmpErrorLogService.add(dmpErrorLogEntity);
                    }
                    dataSign = false;
                }
                pageIndex++;
            }
        } catch (Exception e) {
            log.error(" ===== 获取金蝶云星空订单列表数据失败， 错误信息 =", e);
            throw new RuntimeException(" ===== 获取金蝶云星空订单列表数据失败， 错误信息 = ", e);
        }
        return infoArrayList;
    }

    /**
     * 解析订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    public void analysisOrder(KingdeeOrderEntity kingdeeOrderEntity) throws Exception {
        // 跳过非唯迹订单
        if (StrUtil.isEmpty(kingdeeOrderEntity.getFSaleOrgName()) || !ORG_CODE.equals(kingdeeOrderEntity.getFSaleOrgId())){
            return;
        }
        // 跳过非销售订单
        if (StrUtil.isBlank(kingdeeOrderEntity.getFBillTypeCode()) || !ORDER_TYPES.contains(kingdeeOrderEntity.getFBillTypeCode())){
            return;
        }
        DmpOrderInfoEntity dmpOrderInfoEntity = new DmpOrderInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        //平台订单id
        dmpOrderInfoEntity.setPlatformOrderId(kingdeeOrderEntity.getFBillNo());

        //订单状态 2.配货中 3.已发货 4.已完成 5.已作废 6.退货 7.退款
        dmpOrderInfoEntity.setOrderState(4);

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
        List<KingdeeOrderItemEntity> orderItemEntityList = kingdeeOrderEntity.getOrderItemEntityList();
        for (KingdeeOrderItemEntity orderItemEntity : orderItemEntityList) {
            orderFee = orderFee.add(orderItemEntity.getFAllAmount());
            totalCost = totalCost.add(orderItemEntity.getF_ulz_Decimal());
            totalPrice = totalPrice.add(new BigDecimal(orderItemEntity.getFAmount()));
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
        dmpOrderInfoEntity.setCanSend(null);

        //是否退货 1.退货 2.非退货
        dmpOrderInfoEntity.setIsReturned(null);

        //是否退款 1.退款 2.非退款
        dmpOrderInfoEntity.setIsRefund(null);

        //订单付款时间
        if (StringUtils.isNotBlank(kingdeeOrderEntity.getF_SK_Date()) && !kingdeeOrderEntity.getF_SK_Date().equals("null")) {
            dmpOrderInfoEntity.setPaidTime(sdf.parse(kingdeeOrderEntity.getF_SK_Date()));
        }

        //平台订单时间
        if (StringUtils.isNotBlank(kingdeeOrderEntity.getFCreateDate()) && !kingdeeOrderEntity.getFCreateDate().equals("null")) {
            dmpOrderInfoEntity.setPlatformCreateTime(sdf.parse(kingdeeOrderEntity.getFCreateDate()));
        }

        //平台交易号
        dmpOrderInfoEntity.setSalesRecordNumber(kingdeeOrderEntity.getFBillNo());

        //平台的订单状态
        dmpOrderInfoEntity.setPlatformOrderStatus("");

        //订单来源平台
        dmpOrderInfoEntity.setSourcePlatform("B2B");

        //是否合并订单 1.合并订单 2.非合并订单
        dmpOrderInfoEntity.setIsUnion(null);

        //是否拆分订单 1.拆分订单 2.非拆分订单
        dmpOrderInfoEntity.setIsSplit(null);

        //是否重发订单 1.重发订单 2.非重发订单
        dmpOrderInfoEntity.setIsResend(null);

        //缺货订单 0 正在计算是否缺货 1有货 2缺货 3 已补货
        dmpOrderInfoEntity.setHasGoods(null);

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
        if (StringUtils.isNotBlank(kingdeeOrderEntity.getFCloseDate()) && !kingdeeOrderEntity.getFCloseDate().equals("null")) {
            dmpOrderInfoEntity.setCloseDate(sdf.parse(kingdeeOrderEntity.getFCloseDate()));
        }
        //买家电话1
        dmpOrderInfoEntity.setManPhone(kingdeeOrderEntity.getFLinkPhone());

        //买家电话2
        dmpOrderInfoEntity.setSecondPhone("");

        //是否平台发货订单 1.否 2.是
        dmpOrderInfoEntity.setFbaFlag(null);

        //平台备注
        dmpOrderInfoEntity.setSellerMessage(kingdeeOrderEntity.getFNote());

        //币种
        dmpOrderInfoEntity.setCurrencyCode(kingdeeOrderEntity.getFSettleCurrId());

        //汇率
        if (kingdeeOrderEntity.getFExchangeRate() != null
                && kingdeeOrderEntity.getFExchangeRate().compareTo(BigDecimal.ZERO) <= 0
                && kingdeeOrderEntity.getFSettleCurrId().equalsIgnoreCase("CNY")) {
            dmpOrderInfoEntity.setCurrencyRate(BigDecimal.ONE);
        } else {
            dmpOrderInfoEntity.setCurrencyRate(kingdeeOrderEntity.getFExchangeRate());
        }

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
        dmpOrderInfoEntity.setPlatformSign("金蝶云星空");

        //企业Id
        dmpOrderInfoEntity.setCompanyId(kingdeeOrderEntity.getFSaleOrgId());

        //企业名称
        dmpOrderInfoEntity.setCompanyName(kingdeeOrderEntity.getFSaleOrgName());

        dmpOrderInfoEntity.setCreateTime(LocalDateTime.now());

        //新增订单信息
        String orderInfoId = dmpOrderInfoService.checkOrder(dmpOrderInfoEntity);
        if (StringUtils.isNotBlank(orderInfoId)) {
            //新增订单商品信息
            analysisOrderItem(kingdeeOrderEntity, orderInfoId);
        }
    }

    /**
     * 解析订单商品数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    public void analysisOrderItem(KingdeeOrderEntity kingdeeOrderEntity, String orderId) {
        List<KingdeeOrderItemEntity> orderItem = kingdeeOrderEntity.getOrderItemEntityList();
        List<DmpOrderItemEntity> orderItemList = new ArrayList<>();
        for (KingdeeOrderItemEntity orderItemBean : orderItem) {
            DmpOrderItemEntity dmpOrderItemEntity = new DmpOrderItemEntity();

            //订单表id
            dmpOrderItemEntity.setOrderId(orderId);

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

            //商品原始售价
            dmpOrderItemEntity.setSellPriceOrigin(orderItemBean.getFPrice());

            //商品售价
            dmpOrderItemEntity.setSellPrice(orderItemBean.getFPrice());

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
            dmpOrderItemEntity.setIsCombo(null);

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
            dmpOrderItemEntity.setSkuNo(orderItemBean.getFMaterialId());

            //库存状态：1.自动创建 2.待开发 3.正常 4.清仓 5.停止销售
            dmpOrderItemEntity.setStockStatus(null);

            //erp平台商品id
            dmpOrderItemEntity.setErpOrderItemId(orderItemBean.getFBillNo() + "_" + orderItemBean.getFMaterialNumber());

            //汇率
            if (kingdeeOrderEntity.getFExchangeRate() != null
                    && kingdeeOrderEntity.getFExchangeRate().compareTo(BigDecimal.ZERO) <= 0
                    && kingdeeOrderEntity.getFSettleCurrId().equalsIgnoreCase("CNY")) {
                dmpOrderItemEntity.setCurrencyRate(BigDecimal.ONE);
            } else {
                dmpOrderItemEntity.setCurrencyRate(kingdeeOrderEntity.getFExchangeRate());
            }
            dmpOrderItemEntity.setAmountAfter(orderItemBean.getFAllAmount());
            orderItemList.add(dmpOrderItemEntity);
        }
        dmpOrderItemService.checkOrderItem(orderItemList);
    }
}
