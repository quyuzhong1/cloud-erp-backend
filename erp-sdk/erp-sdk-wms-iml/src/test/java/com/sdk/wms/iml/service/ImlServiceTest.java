package com.sdk.wms.iml.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.utils.FileUtil;
import com.erp.model.wms.enums.OverseasDeliveryModeEnum;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import com.sdk.wms.iml.dto.ImlBaseResp;
import com.sdk.wms.iml.dto.request.*;
import com.sdk.wms.iml.dto.response.ImlInboundResp;
import com.sdk.wms.iml.dto.response.ImlOutboundResp;
import io.seata.common.util.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    public void createOutboundBill() {
        ImlCreateOutboundReq imlCreateOutboundReq = ImlCreateOutboundReq.builder()
                .platformOrderNo("WFHD20251017001")
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
        imlCancelOutboundReq.setOrderNo("OT80565-20251017-000006");
        ImlBaseResp<String> resp = imlService.cancelOutboundBill(imlCancelOutboundReq);
        System.out.println(JSONObject.toJSONString(resp));
    }


    @Test
    public void createInboundBill() {
        ImlCreateInboundReq imlCreateInboundReq = ImlCreateInboundReq.builder()
                .needCustomerAudit("N")
                .platformOrderNo("FHD12456")
                .bizType("TOC")
                .destWarehouseCode("ceshi")
                .customsType("SEPARATE_TAX")
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
                                .boxNo("ceshi11324")
                                .boxWeight(new BigDecimal(1.5))
                                .boxLength(new BigDecimal(1.5))
                                .boxWidth(new BigDecimal(1.5))
                                .boxHeight(new BigDecimal(1.5))
                                .boxDetails(Arrays.asList(
                                        ImlCreateInboundReq.BoxsDTO.BoxDetailsDTO.builder()
                                                .skuBarcode("ceshiB0540-80D")
                                                .quantity(1)
                                                .build()
                                ))
                                .build()
                ))
                .build();
        imlCreateInboundReq.setDirect(
                ImlCreateInboundReq.DirectDTO.builder()
                        .trackingNumber("测试1249")
                        .build()
        );

        ImlBaseResp<ImlInboundResp>  resp = imlService.createInboundBill(imlCreateInboundReq);
        System.out.println(JSONObject.toJSONString(resp));
    }
}