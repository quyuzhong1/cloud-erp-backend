package com.sdk.wms.iml.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.utils.Md5Util;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.iml.constants.ImlConstants;
import com.sdk.wms.iml.dto.ImlBaseResp;
import com.sdk.wms.iml.dto.request.*;
import com.sdk.wms.iml.dto.response.*;
import com.sdk.wms.iml.utils.ImlUtils;
import io.seata.common.util.StringUtils;
import jodd.util.StringUtil;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author liuruipeng
 */
@Component
@Validated
public class ImlService {

    public static final String RECEIVING_CODE = "receiving_code";

    private String getPreUrl(){
        if (BusinessCommonConstants.hasProfile("prod")) {
            return "https://open.imlb2c.com/";
        } else {
            return "https://pre-open.imlb2c.cn/";
        }
    }

    private static final String APP_ID = "1929841041771364354";
    private static final String APP_SECRET = "dx-zosnwtgwo3=u=276qgzu+3weguyst";
    private static final String API_URL = "https://pre-open.imlb2c.cn/open-sdk/oms/stock_age_query";
    private static final String REQUEST_TOKEN = "ZOFsMc85N29ly-sA4qKbDXQgJS6QF2A8IzlCWWXH_UgoaGoY6Az8aZuU_uWuQ6s0";

    public static void main(String[] args) {
        Map<String,Object> body = new HashMap<>();
        body.put("platformCustomerCode","80565");
//        body.put("pageSize",50);
        //查询前一天的时间戳的数据
        long startTime = LocalDateTime.now().minusDays(300).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String timestamp = String.valueOf(new Date().getTime());
        String appSign = Md5Util.md5(APP_SECRET + timestamp + JSONObject.toJSONString(body));
        Map<String,String> headerMap = new HashMap<>();
        headerMap.put("x-app-id",APP_ID);
        headerMap.put("x-app-sign",appSign);
        headerMap.put("x-request-time",timestamp);
        headerMap.put("x-request-token",REQUEST_TOKEN);
        System.out.println(JSONObject.toJSONString(body));
        System.out.println(JSONObject.toJSONString(headerMap));
        String bodyStr = OkHttpUtils.doPostJson(API_URL,body, headerMap);
        System.out.println(bodyStr);
    }

//    public static void main(String[] args) {
//        ImlCreateInboundReq imlCreateInboundReq = ImlCreateInboundReq.builder()
//                .needCustomerAudit("N")
//                .platformOrderNo("TESTFHD20251015001")
//                .bizType("TOC")
//                .destWarehouseCode("ceshi")
//                .customsType("SEPARATE_TAX")
//                .inboundType("DIRECT")
//                .logisticsCode("IML-RU")
//                .expectedDate(new Date().getTime())
//                .direct( ImlCreateInboundReq.DirectDTO.builder()
//                        .trackingNumber("123456987")
//                        .build())
//                .boxs(Arrays.asList(
//                        ImlCreateInboundReq.BoxsDTO.builder()
//                                .boxNo("BOX001")
//                                .boxLength(new BigDecimal("1.1"))
//                                .boxWidth(new BigDecimal("1.1"))
//                                .boxHeight(new BigDecimal("1.1"))
//                                .boxWeight(new BigDecimal("1.1"))
//                                .boxDetails(Arrays.asList(
//                                        ImlCreateInboundReq.BoxsDTO.BoxDetailsDTO.builder()
//                                                .skuCode("HXPENG-TESTTEST")
//                                                .quantity(1)
//                                                .build()
//                                ))
//                                .build()
//                ))
//                .attachments(Arrays.asList(
//                        ImlCreateInboundReq.AttachmentsDTO.builder()
//                                .fileName("test.pdf")
//                                .fileType("pdf")
//                                .fileData("OTHER")
//                                .build()
//                ))
//                .build();
//        String timestamp = String.valueOf(new Date().getTime());
//        String appSign = Md5Util.md5(APP_SECRET + timestamp + JSONObject.toJSONString(imlCreateInboundReq));
//        Map<String,String> headerMap = new HashMap<>();
//        headerMap.put("x-app-id",APP_ID);
//        headerMap.put("x-app-sign",appSign);
//        headerMap.put("x-request-time",timestamp);
//        headerMap.put("x-request-token",REQUEST_TOKEN);
//        String bodyStr = OkHttpUtils.doPostJson(API_URL,JSONObject.toJSONString(imlCreateInboundReq), headerMap);
//        System.out.println(bodyStr);
//    }
    /**
     * 获取仓库列表
     */
    public ImlBaseResp<String> getProduct(Map<String,Object> body){
        Map<String, String> headerMap = ImlUtils.buildHearderMap(JSONObject.toJSONString(new HashMap<>()));
        String path = "open-sdk/mms/new_query_sku_list";
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(body), headerMap);
        return ImlUtils.parseToImlResp(bodyStr, String.class);
    }
    /**
     * 获取仓库列表
     */
    public ImlBaseResp<String> getInventory(Map<String,Object> body){
        Map<String, String> headerMap = ImlUtils.buildHearderMap(JSONObject.toJSONString(new HashMap<>()));
        String path = "open-sdk/oms/new_stock_total_query";
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(body), headerMap);
        return ImlUtils.parseToImlResp(bodyStr, String.class);
    }
    /**
     * 获取仓库列表
     */
    public ImlBaseResp<String> getWarehouse(){
        Map<String, String> headerMap = ImlUtils.buildHearderMap(JSONObject.toJSONString(new HashMap<>()));
        String path = "open-sdk/oms/query_warehouse";
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,new HashMap<>(), headerMap);
        return ImlUtils.parseToImlResp(bodyStr, String.class);
    }

    /**
     * 获取物流产品
     */
    public ImlBaseResp<List<ImlLogisticChannelResp>> getShippingMethod(){
        Map<String, String> headerMap = ImlUtils.buildHearderMap(JSONObject.toJSONString(new HashMap<>()));
        String path = "open-sdk/fms/product_query";
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,new HashMap<>(), headerMap);
        return ImlUtils.parseToImlResp(bodyStr, new TypeReference<ImlBaseResp<List<ImlLogisticChannelResp>>>() {});
    }

    /**
     * 创建入库单
     */
    public ImlBaseResp<ImlInboundResp> createInboundBill(@Valid ImlCreateInboundReq imlGetReceiptReq){

        String path = "open-sdk/oms/create_inbound_order";
        Map<String, String> headerMap = ImlUtils.buildHearderMap(JSONObject.toJSONString(imlGetReceiptReq));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(imlGetReceiptReq), headerMap);
        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(imlGetReceiptReq));
        ImlBaseResp<ImlInboundResp> respDto = ImlUtils.parseToImlResp(bodyStr, ImlInboundResp.class);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return respDto;
    }

    /**
     * 编辑入库单
     */
    public ImlBaseResp<ImlInboundResp> editInboundBill(@Valid ImlCreateInboundReq imlGetReceiptReq){
        String path = "open-sdk/oms/edit_inbound_order";
        Map<String, String> headerMap = ImlUtils.buildHearderMap(JSONObject.toJSONString(imlGetReceiptReq));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(imlGetReceiptReq), headerMap);
        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(imlGetReceiptReq));
        ImlBaseResp<ImlInboundResp> respDto = ImlUtils.parseToImlResp(bodyStr, ImlInboundResp.class);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return respDto;
    }
    /**
     * 取消入库单
     */
    public ImlBaseResp<String> cancelInboundBill(@Valid @NotEmpty(message = "入库单号不能为空") String receivingCode){
        String path = "open-sdk/oms/cancel_inbound_order";
        Map<String,Object> bodyMap = new HashMap<>();
        bodyMap.put("orderNo",receivingCode);
        Map<String, String> headerMap = ImlUtils.buildHearderMap(JSONObject.toJSONString(bodyMap));
        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(bodyMap));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(bodyMap), headerMap);
        ImlBaseResp<String> respDto = ImlUtils.parseToImlResp(bodyStr, String.class);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return respDto;
    }


    /**
     * 创建出库单
     */
    public ImlBaseResp<ImlOutboundResp> createOutboundBill(@Valid ImlCreateOutboundReq imlCreateOutboundReq){
        String path = "open-sdk/oms/create_outbound_order";
        Map<String, String> headerMap = ImlUtils.buildHearderMap(JSONObject.toJSONString(imlCreateOutboundReq));
        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(imlCreateOutboundReq));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(imlCreateOutboundReq), headerMap);
        ImlBaseResp<ImlOutboundResp> respDto = ImlUtils.parseToImlResp(bodyStr, ImlOutboundResp.class);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return respDto;
    }

    /**
     * 取消出库单
     */
    public ImlBaseResp<String> cancelOutboundBill(@Valid ImlCancelOutboundReq imlCancelOutboundReq){
        String path = "open-sdk/oms/cancel_outbound_order";
        Map<String, String> headerMap = ImlUtils.buildHearderMap(JSONObject.toJSONString(imlCancelOutboundReq));
        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(imlCancelOutboundReq));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(imlCancelOutboundReq), headerMap);
        ImlBaseResp<String> respDto = ImlUtils.parseToImlResp(bodyStr, String.class);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return respDto;
    }


    /**
     * 上传面单
     */
    public ImlBaseResp<String> uploadOrderLabel(ImlUploadLabelReq imlCreateOutboundReq){
        String path = "open-sdk/oms/upload_label_info";
        Map<String, String> headerMap = ImlUtils.buildHearderMap(JSONObject.toJSONString(imlCreateOutboundReq));
        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(imlCreateOutboundReq));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(imlCreateOutboundReq), headerMap);
        ImlBaseResp<String> respDto = ImlUtils.parseToImlResp(bodyStr, String.class);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return respDto;
    }
    /**
     * 上传交接文件
     */
    public ImlBaseResp<String> uploadFile(ImlUploadFileReq imlCreateOutboundReq){
        String path = "open-sdk/oms/upload_delivery_receipt_file";
        Map<String, String> headerMap = ImlUtils.buildHearderMap(JSONObject.toJSONString(imlCreateOutboundReq));
        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(imlCreateOutboundReq));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(imlCreateOutboundReq), headerMap);
        ImlBaseResp<String> respDto = ImlUtils.parseToImlResp(bodyStr, String.class);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return respDto;
    }

    /**
     * 运费试算
     */
    public ImlBaseResp<ImlCalculateFeeResp> calculateFee(ImlCalculateFeeReq imlReq){
        String path = "open-sdk/fms/check_fee";
        Map<String, String> headerMap = ImlUtils.buildHearderMap(JSONObject.toJSONString(imlReq));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(imlReq), headerMap);
        ImlBaseResp<ImlCalculateFeeResp> respDto = ImlUtils.parseToImlResp(bodyStr, ImlCalculateFeeResp.class);
        return respDto;
    }
}
