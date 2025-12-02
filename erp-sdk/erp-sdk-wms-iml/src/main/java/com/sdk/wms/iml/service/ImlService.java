package com.sdk.wms.iml.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import okhttp3.*;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    private static final String APP_ID = "1979003219754110977";
    private static final String APP_SECRET = "7m=j5-+gpydwadwii8y+eawg6-909-ij";
    private static final String API_URL = "https://open.imlb2c.com/open-sdk/oms/stock_age_query";
    private static final String REQUEST_TOKEN = "fewR7gRJix5l6Xbu7HBPEAtmrXZmYVjHu0DD76oVjNaz0_k6W7D2dRwppWXETUAV   ";

    public static void main(String[] args) {
//        Map<String,Object> body = new HashMap<>();
//        body.put("platformCustomerCode","86526");
////        body.put("pageSize",50);
//        body.put("pageIndex",1);
//        body.put("pageSize",100);
//        //查询前一天的时间戳的数据
//        long startTime = LocalDateTime.now().minusDays(300).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
//        long endTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
//        String timestamp = String.valueOf(new Date().getTime());
//        String appSign = Md5Util.md5(APP_SECRET + timestamp + JSONObject.toJSONString(body));
//        Map<String,String> headerMap = new HashMap<>();
//        headerMap.put("x-app-id",APP_ID);
//        headerMap.put("x-app-sign",appSign);
//        headerMap.put("x-request-time",timestamp);
//        headerMap.put("x-request-token",REQUEST_TOKEN);
//        System.out.println(JSONObject.toJSONString(body));
//        System.out.println(JSONObject.toJSONString(headerMap));
//        String bodyStr = OkHttpUtils.doPostJson(API_URL,body, headerMap);
//        System.out.println(bodyStr);

        try {
            boolean isNext = true;
            int page = 1;
            while (isNext){
                // 准备请求数据
                String timestamp = String.valueOf(new Date().getTime());
                Map<String,Object> bodyMap = new HashMap<>();
                bodyMap.put("pageIndex",page);
                bodyMap.put("pageSize",100);
                bodyMap.put("platformCustomerCode","86526");
                String postJson = JSONObject.toJSONString(bodyMap);

                // 计算签名
                String appSign = Md5Util.md5(APP_SECRET + timestamp + postJson);

                // 创建请求体
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        postJson
                );

                // 创建请求
                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .addHeader("x-app-id", APP_ID)
                        .addHeader("x-app-sign", appSign)
                        .addHeader("x-request-time", timestamp)
                        .addHeader("x-request-token", REQUEST_TOKEN)
                        .build();

                // 发送请求
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)  // 连接超时
                        .readTimeout(60, TimeUnit.SECONDS)     // 读取超时
                        .writeTimeout(30, TimeUnit.SECONDS)    // 写入超时
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONObject responseJson = JSON.parseObject(responseBody);
                        if (responseJson.getInteger("code") != 0) {
                            throw new RuntimeException("API返回错误: " + responseJson);
                        }
                        isNext = responseJson.getJSONObject("data").getBooleanValue("hasNext");
                        JSONArray dataList = responseJson.getJSONObject("data").getJSONArray("list");
                        if (dataList != null) {
                            //过滤掉bizType 为TOB的数据
                            List<Object> filteredList = dataList.stream()
                                    .filter(obj -> {
                                        if (obj instanceof JSONObject) {
                                            JSONObject jsonObj = (JSONObject) obj;
                                            String bizType = jsonObj.getString("bizType");
                                            return !"TOB".equals(bizType);
                                        }
                                        return true; // 如果不是JSONObject，保留原数据
                                    })
                                    .collect(Collectors.toList());
                            dataList = new JSONArray(filteredList);
                        }
                        page = page +1;
                    } else {
                        throw new RuntimeException("请求失败: " + response.code() + " - " + response.message() + "-" +response.body().string() );
                    }
                }
            }
        }catch (Exception e){
            throw new RuntimeException("请求异常: " + e.getMessage(), e);
        }
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
