package com.erp.server.dmp.pull.service.kingdee;

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
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderItemService;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.QueryParam;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 金蝶退货销售出库
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.SAL_RETURNSTOCK)
public class KingdeeReturnOrderInfoImpl implements IReportSaveService {
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
                                dmpErrorLogEntity.setCreateTime(new Date());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 金蝶云星空修改mongodb销售出库数据失败，[ 单号 = " + orderEntity.getFBillNo() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(orderEntity, MongoTableNameContant.ORIGINAL_KINGDEE_RETURN_ORDER);
                }
                //存储数据到中台
                analysisReturnOrder(orderEntity);
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
        String st = "";
        String sd = "";
        if (dto.getJobTaskDTO().getLastTime() != null && dto.getJobTaskDTO().getNextTime() != null) {
            LocalDateTime localDateTime = lastTime.minusMonths(5);
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
        queryfilters.add(String.format("FBillTypeID = '%s'", "73383412199a402bb58439509e089077"));
        queryfilters.add(String.format("FOrderNo <> '%s'", ""));
        queryfilters.add(String.format("FDocumentStatus = '%s'", "C"));
        String filterStr = String.join(" and ", queryfilters);
        String fieldKeys = "FBillTypeID,FBillTypeID.FName,FBillNo,FDate,FDocumentStatus,FSaleOrgId,FSaleOrgId.FName,FRetcustId,FRetcustId.FName,FSalesManId,FSalesManId.FName,FCreateDate,FModifyDate,FCancelStatus,FReceiverCountry,FLinkMan,FExchangeRate,FApproveDate,FBussinessType,FOwnerTypeIdHead,FSettleCurrId.FCode,FDelTime,FHeadNote,"
                + "FOrderNo,FAmount,FMustqty,FUnitID.FName,FMaterialId,FMaterialId.FNumber,FMaterialName,FAuxpropId,FMaterialType,FPrice,FStockId,FStocklocId,FStockstatusId,FNote,FSrcBillNo,FSrcBillTypeID,FIsFree,FMaterialModel,FRealQty,FSOBILLTYPEID,FSalUnitQty,FProjectNo,F_ulz_KHSKU";

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
                        throw new RuntimeException(" ===== 金蝶云星空解析销售出库销售出库信息数据失败 ===== " + result);
                    }

                    for (List<Object> objects : result) {
                        Map<String, String> valMap = KingdeeUtils.keySetValByLinked(fieldKeys, objects);
                        KingdeeReturnOrderEntity kingdeeReturnOrderEntity = new KingdeeReturnOrderEntity();
                        kingdeeReturnOrderEntity.setFBillTypeID(valMap.get("FBillTypeID"));
                        kingdeeReturnOrderEntity.setFBillTypeName(valMap.get("FBillTypeID.FName"));
                        kingdeeReturnOrderEntity.setFBillNo(valMap.get("FBillNo"));
                        kingdeeReturnOrderEntity.setFOrderNo(valMap.get("FOrderNo"));
                        kingdeeReturnOrderEntity.setFDate(valMap.get("FDate"));
                        kingdeeReturnOrderEntity.setFDocumentStatus(valMap.get("FDocumentStatus"));
                        kingdeeReturnOrderEntity.setFSaleOrgId(valMap.get("FSaleOrgId"));
                        kingdeeReturnOrderEntity.setFSaleOrgName(valMap.get("FSaleOrgId"));
                        kingdeeReturnOrderEntity.setFRetcustId(valMap.get("FName"));
                        kingdeeReturnOrderEntity.setFRetcustName(valMap.get("FRetcustId.FName"));
                        kingdeeReturnOrderEntity.setFSalesManId(valMap.get("FSalesManId"));
                        kingdeeReturnOrderEntity.setFSalesManName(valMap.get("FSalesManId.FName"));
                        kingdeeReturnOrderEntity.setFCreateDate(valMap.get("FCreateDate"));
                        kingdeeReturnOrderEntity.setFModifyDate(valMap.get("FModifyDate"));
                        kingdeeReturnOrderEntity.setFCancelStatus(valMap.get("FCancelStatus"));
                        kingdeeReturnOrderEntity.setFReceiverCountry(valMap.get("FReceiverCountry"));
                        kingdeeReturnOrderEntity.setFLinkMan(valMap.get("FLinkMan"));
                        kingdeeReturnOrderEntity.setFExchangeRate(valMap.get("FExchangeRate"));
                        kingdeeReturnOrderEntity.setFApproveDate(valMap.get("FApproveDate"));
                        kingdeeReturnOrderEntity.setFBussinessType(valMap.get("FBussinessType"));
                        kingdeeReturnOrderEntity.setFOwnerTypeIdHead(valMap.get("FOwnerTypeIdHead"));
                        kingdeeReturnOrderEntity.setFSettleCurrCode(valMap.get("FSettleCurrId.FCode"));
                        kingdeeReturnOrderEntity.setFDelTime(valMap.get("FDelTime"));
                        kingdeeReturnOrderEntity.setFHeadNote(valMap.get("FHeadNote"));

                        //商品信息
                        KingdeeReturnOrderItemEntity orderItemEntity = new KingdeeReturnOrderItemEntity();
                        orderItemEntity.setFBillNo(valMap.get("FBillNo"));
                        orderItemEntity.setFAmount(valMap.get("FAmount"));
                        orderItemEntity.setFMustqty(valMap.get("FMustqty"));
                        orderItemEntity.setFUnitName(valMap.get("FUnitID.FName"));
                        orderItemEntity.setFMaterialId(valMap.get("FMaterialId"));
                        orderItemEntity.setFMaterialNumber(valMap.get("FMaterialId.FNumber"));
                        orderItemEntity.setFMaterialName(valMap.get("FMaterialName"));
                        orderItemEntity.setFAuxpropId(valMap.get("FAuxpropId"));
                        orderItemEntity.setFMaterialType(valMap.get("FMaterialType"));
                        orderItemEntity.setFPrice(valMap.get("FPrice"));
                        orderItemEntity.setFStockId(valMap.get("FStockId"));
                        orderItemEntity.setFStocklocId(valMap.get("FStocklocId"));
                        orderItemEntity.setFStockstatusId(valMap.get("FStockstatusId"));
                        orderItemEntity.setFNote(valMap.get("FNote"));
                        orderItemEntity.setFSrcBillNo(valMap.get("FSrcBillNo"));
                        orderItemEntity.setFSrcBillTypeID(valMap.get("FSrcBillTypeID"));
                        orderItemEntity.setFIsFree(valMap.get("FIsFree"));
                        orderItemEntity.setFMaterialModel(valMap.get("FMaterialModel"));
                        orderItemEntity.setFRealQty(valMap.get("FRealQty"));
                        orderItemEntity.setFSOBILLTYPEID(valMap.get("FSOBILLTYPEID"));
                        orderItemEntity.setFSalUnitQty(valMap.get("FSalUnitQty"));
                        orderItemEntity.setFProjectNo(valMap.get("FProjectNo"));
                        orderItemEntity.setF_ulz_KHSKU(valMap.get("F_ulz_KHSKU"));

                        KingdeeReturnOrderEntity entity = infoArrayList.stream().filter(a -> a.getFBillNo().equals(valMap.get("FBillNo")) && a.getFOrderNo().equals(valMap.get("FOrderNo"))).findFirst().orElse(null);
                        if (entity != null) {
                            entity.getItemEntityList().add(orderItemEntity);
                        } else {
                            List<KingdeeReturnOrderItemEntity> orderItemEntityList = new ArrayList();
                            orderItemEntityList.add(orderItemEntity);
                            kingdeeReturnOrderEntity.setItemEntityList(orderItemEntityList);
                            infoArrayList.add(kingdeeReturnOrderEntity);
                        }
                    }
                } else {
                    dataSign = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                log.info("请求接口地址异常 错误信息：" + e.getMessage());
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
                    dmpErrorLogEntity.setCreateTime(new Date());
                    dmpErrorLogService.add(dmpErrorLogEntity);
                }
                dataSign = false;
            }
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
    @Transactional
    public void analysisReturnOrder(KingdeeReturnOrderEntity returnOrderEntity) throws Exception {
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
        if (returnOrderEntity.getFCancelStatus().equals("C")) {
            status = 5;
        }

        if (returnOrderEntity.getFDocumentStatus().equals("C")) {
            status = 4;
        }


        //状态：1待处理 2已退款 3已重发 4已完成 5已作废
        dmpReturnOrderInfoEntity.setStatus(status);

        //平台交易号
        dmpReturnOrderInfoEntity.setSalesRecordNumber("");

        List<KingdeeReturnOrderItemEntity> itemEntityList = returnOrderEntity.getItemEntityList();
        BigDecimal amount = BigDecimal.ZERO;
        for (KingdeeReturnOrderItemEntity kingdeeReturnOrderItemEntity : itemEntityList) {
            amount = amount.add(BigDecimal.valueOf(Double.valueOf(kingdeeReturnOrderItemEntity.getFAmount())));
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

        //汇率
        dmpReturnOrderInfoEntity.setCurrencyRate(BigDecimal.valueOf(Double.valueOf(returnOrderEntity.getFExchangeRate())));

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
            dmpReturnOrderItemEntity.setSellPrice(BigDecimal.valueOf(Double.valueOf(orderItemBean.getFPrice())));

            //物品属性
            dmpReturnOrderItemEntity.setSpecifics(orderItemBean.getFMaterialModel());

            //状态 1待处理 2验货入库 3自然耗损
            dmpReturnOrderItemEntity.setStatus(2);

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
