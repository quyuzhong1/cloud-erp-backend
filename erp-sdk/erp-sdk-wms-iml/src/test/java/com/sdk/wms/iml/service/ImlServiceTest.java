package com.sdk.wms.iml.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.utils.FileUtil;
import com.common.core.utils.Md5Util;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.wms.enums.OverseasDeliveryModeEnum;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import com.sdk.wms.iml.dto.ImlBaseResp;
import com.sdk.wms.iml.dto.request.*;
import com.sdk.wms.iml.dto.response.ImlCalculateFeeResp;
import com.sdk.wms.iml.dto.response.ImlInboundResp;
import com.sdk.wms.iml.dto.response.ImlOutboundResp;
import io.seata.common.util.StringUtils;
import okhttp3.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={BusinessCommonConstants.class,ImlService.class})
public class ImlServiceTest {


    public ImlServiceTest(){
        Map<String,Object> authMap = new HashMap<>();
        //测试
        authMap.put("appId","1929841041771364354");
        authMap.put("appToken","ZOFsMc85N29ly-sA4qKbDXQgJS6QF2A8IzlCWWXH_UgoaGoY6Az8aZuU_uWuQ6s0");
        authMap.put("appSecret","dx-zosnwtgwo3=u=276qgzu+3weguyst");
        //生产
//        authMap.put("appId","1979003219754110977");
//        authMap.put("appToken","fewR7gRJix5l6Xbu7HBPEAtmrXZmYVjHu0DD76oVjNaz0_k6W7D2dRwppWXETUAV");
//        authMap.put("appSecret","7m=j5-+gpydwadwii8y+eawg6-909-ij");
        ThirdWarehouseContext.setAuthMap(authMap);
    }


    private static final String APP_ID = "1979003219754110977";
    private static final String APP_SECRET = "7m=j5-+gpydwadwii8y+eawg6-909-ij";
    private static final String API_URL = "https://open.imlb2c.com";
    private static final String REQUEST_TOKEN = "fewR7gRJix5l6Xbu7HBPEAtmrXZmYVjHu0DD76oVjNaz0_k6W7D2dRwppWXETUAV";

    public static void main(String[] args) throws Exception{
        Map<String,Object> body = new HashMap<>();
        body.put("code","RI2025102800925");
//        body.put("pageSize",50);
        //查询前一天的时间戳的数据
//        long startTime = LocalDateTime.now().minusDays(300).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
//        long endTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
//        body.put("createTimeFrom",startTime);
//        body.put("createTimeTo",endTime);
        String timestamp = String.valueOf(new Date().getTime());
        String appSign = Md5Util.md5(APP_SECRET + timestamp + JSONObject.toJSONString(body));
        Map<String,String> headerMap = new HashMap<>();
        headerMap.put("x-app-id",APP_ID);
        headerMap.put("x-app-sign",appSign);
        headerMap.put("x-request-time",timestamp);
        headerMap.put("x-request-token",REQUEST_TOKEN);
        String bodyStr = OkHttpUtils.doPostJson(API_URL + "/open-sdk/oms/query_refund_order_detail",body, headerMap);
        System.out.println(bodyStr);

//        List<Object> allData = new ArrayList<>();
//        String appId = APP_ID;
//        String appToken = REQUEST_TOKEN;
//        String appSecret = APP_SECRET;
//        String ownerCode = "86526";
////        String extend = indoc.getString("extend_json");
////        JSONObject extendMap = JSON.parseObject(extend);
////
////        JSONObject extendValMap = extendMap.getJSONObject("value");
//        String url = API_URL;
    }

    @Resource
    private ImlService imlService;

    @Test
    public void getProduct() {
        Map<String, Object> param = new HashMap<>();
        ImlBaseResp<String> resp = imlService.getProduct(param);
        System.out.println(JSONObject.toJSONString(resp));
    }

    @Test
    public void getInventory() {
        Map<String, Object> param = new HashMap<>();
        ImlBaseResp<String> resp = imlService.getInventory(param);
        System.out.println(JSONObject.toJSONString(resp));
    }

    @Test
    public void uploadOrderLabel() {
        ImlUploadLabelReq imlUploadLabelReq = new ImlUploadLabelReq();
        imlUploadLabelReq.setOrderNo("OT80565-20251017-000006");
        imlUploadLabelReq.setLabelFilePath("https://erp.ulanzi.cn:8088/group1/M00/B5/C4/rBBkCmjuAwiAFnWUAABwE-xqk3g99.xlsx");
        ImlBaseResp<String> resp = imlService.uploadOrderLabel(imlUploadLabelReq);
        System.out.println(JSONObject.toJSONString(resp));
    }

    @Test
    public void uploadFile() {
        ImlUploadFileReq imlUploadLabelReq = ImlUploadFileReq.builder()
                .platformCustomerCode("81571")
                .fileNumber("rBBkCmjuAwiAFnWUAABwE-xqk3g99")
                .fileName("rBBkCmjuAwiAFnWUAABwE-xqk3g99")
                .filePath("https://erp.ulanzi.cn:8088/group1/M00/B5/C4/rBBkCmjuAwiAFnWUAABwE-xqk3g99.xlsx")
                .type("Wildberries")
                .orderList( Arrays.asList(
                        ImlUploadFileReq.OrderListDTO.builder()
                                .orderNo("OT80565-20251017-000006")
                                .build()
                ))
                .build();
        ImlBaseResp<String> resp = imlService.uploadFile(imlUploadLabelReq);
        System.out.println(JSONObject.toJSONString(resp));
    }


    @Test
    public void calculateFee() {
        ImlCalculateFeeReq imlCalculateFeeReq = ImlCalculateFeeReq.builder()
                .orderType("OUTBOUND")
                .bizType("TOC")
                .transportProductCode("IML-RU")
                .orderBoxes( Arrays.asList(
                        ImlCalculateFeeReq.OrderBoxesDTO.builder()
                                .length(new BigDecimal(10))
                                .width(new BigDecimal(10))
                                .height(new BigDecimal(10))
                                .weight(new BigDecimal(1))
                                .build()
                ))
                .skus( Arrays.asList(
                        ImlCalculateFeeReq.SkusDTO.builder()
                                .skuBarcode("ceshiB0540-80D")
                                .build()
                ))
                .address( ImlCalculateFeeReq.AddressDTO.builder()
                        .country("RU")
                        .postcode("123456")
                        .build()
                )
                .build();
        ImlBaseResp<ImlCalculateFeeResp> resp = imlService.calculateFee(imlCalculateFeeReq);
        System.out.println(JSONObject.toJSONString(resp));
    }


    @Test
    public void createOutboundBill() {
        ImlCreateOutboundReq imlCreateOutboundReq = ImlCreateOutboundReq.builder()
                .platformOrderNo("WFHD202510290101")
                .ecPlatformOrderNo("asn520254")
                .logisticsCode("IML-RU")
                .bizType("TOC")
                .warehouseCode("ceshi")
                .trackNumber("123456")
                .buyerCountry("RU")
                .buyerProvince("state")
                .buyerCity("for")
                .buyerAddress("address")
                .buyerName("mark")
                .buyerPhone("123456")
                .buyerEmail("123")
                .buyerPostcode("123456")
                .detailList(Arrays.asList(
                        ImlCreateOutboundReq.DetailListDTO.builder()
                                .skuBarcode("ceshiB0540-80D")
                                .skuCount(1)
                                .build()
                ))
                .build();

        ImlBaseResp<ImlOutboundResp>  resp = imlService.createOutboundBill(imlCreateOutboundReq);
        System.out.println(JSONObject.toJSONString(resp));
    }

    @Test
    public void cancelOutboundBill() {
        ImlCancelOutboundReq imlCancelOutboundReq = new ImlCancelOutboundReq();
        imlCancelOutboundReq.setOrderNo("OT80565-20251029-000001");
        ImlBaseResp<String> resp = imlService.cancelOutboundBill(imlCancelOutboundReq);
        System.out.println(JSONObject.toJSONString(resp));
    }


    @Test
    public void createInboundBill() {
        ImlCreateInboundReq imlCreateInboundReq = ImlCreateInboundReq.builder()
                .needCustomerAudit("N")
                .platformOrderNo("FHD251028000001")
                .bizType("TOC")
                .destWarehouseCode("RUS2")
//                .customsType("SEPARATE_TAX")
                .inboundType("DIRECT")
                .expectedDate(LocalDateTimeUtil.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .attachments(Arrays.asList(
                        ImlCreateInboundReq.AttachmentsDTO.builder()
                                .fileName("测试")
                                .fileType("xlxs")
                                .fileData("ceshi")
                                .build()
                ))
                .boxs(Arrays.asList(
                        ImlCreateInboundReq.BoxsDTO.builder()
//                                .boxNo("ceshi11211111324")
                                .boxWeight(new BigDecimal(1.5))
                                .boxLength(new BigDecimal(1.5))
                                .boxWidth(new BigDecimal(1.5))
                                .boxHeight(new BigDecimal(1.5))
                                .boxDetails(Arrays.asList(
                                        ImlCreateInboundReq.BoxsDTO.BoxDetailsDTO.builder()
                                                .skuBarcode("cola333")
                                                .quantity(1)
                                                .build()
                                ))
                                .build()
                ))
                .build();
        imlCreateInboundReq.setDirect(
                ImlCreateInboundReq.DirectDTO.builder()
                        .trackingNumber("354345")
                        .build()
        );

        ImlBaseResp<ImlInboundResp>  resp = imlService.createInboundBill(imlCreateInboundReq);
        System.out.println(JSONObject.toJSONString(resp));
    }



    @Test
    public void cancelInboundBill() {
        ImlBaseResp<String> resp = imlService.cancelInboundBill("IN80565-20251028-000003");
        System.out.println(JSONObject.toJSONString(resp));
    }
}