package com.erp.server.tms.service.logistics;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.server.tms.ErpServerTmsApplication;
import com.sdk.tms.shopee.model.base.BaseRequest;
import com.sdk.tms.shopee.model.base.BaseResponse;
import com.sdk.tms.shopee.model.logistics.request.Dropoff;
import com.sdk.tms.shopee.model.logistics.request.ShipOrderRequest;
import com.sdk.tms.shopee.model.logistics.request.ShippingOrderRequest;
import com.sdk.tms.shopee.model.logistics.request.TrackRequest;
import com.sdk.tms.shopee.model.logistics.response.ShipDetailResponse;
import com.sdk.tms.shopee.model.logistics.response.ShipDropInfo;
import com.sdk.tms.shopee.model.logistics.response.ShippingDocumentParameterResponse;
import com.sdk.tms.shopee.model.logistics.response.TrackResponse;
import com.sdk.tms.shopee.service.ShopeeLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author zdy
 * @ClassName UBILogisticsHandlerImplTest
 * @date 2023年11月16日
 * @version: 1.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class ShopeeLogisticsHandlerImplTest {
    @Resource
    private ShopeeLogisticsHandlerImpl shopeeLogisticsHandler;
    @Resource
    private ShopeeLogisticsService shopeeLogisticsService;

    private Map<String, String> authMap = new HashMap<>();

    public ShopeeLogisticsHandlerImplTest(){
        authMap.put("id", "1111");
        authMap.put("logisticsPlatform", "Shopee");
        authMap.put("partnerKey", "5975757847654870727869546f436e696f4b454d466a74586f46696555466348");
        authMap.put("partnerId", "1070627");
//        authMap.put("shopId", "1843907937611972609");
        authMap.put("shopId", "94349");
        authMap.put("token", "74686351676b4b4c657a4b78586d6241");
        authMap.put("host", "https://partner.test-stable.shopeemobile.com");
    }

    public Map<String, String> getLogisticsAuthConfig(){
        Map<String, String> logisticsAuthConfig = shopeeLogisticsHandler.getLogisticsAuthConfigByShopId("");
        return logisticsAuthConfig;
    }
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(){
        List<Map<String, String>> logisticsAuthConfigByPlatform = shopeeLogisticsHandler.getLogisticsAuthConfigByPlatform(LogisticsPlatformEnum.SHOPEE.getCode());
        return logisticsAuthConfigByPlatform;
    }

    @Test
    public void getChannel() {
        ApiResult<List<LogisticsSaleChannelEntity>> channel = shopeeLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build());
        System.out.println(channel);
    }

    @Test
    public void createOrder(){
        SenderInfo senderInfo = new SenderInfo();
        senderInfo.setAddressFirst("address");
        senderInfo.setContact("contact");
        senderInfo.setCityName("newyork");
        senderInfo.setCompanyName("componeny");
        senderInfo.setName("name");
        senderInfo.setProvinceName("shenzhen");
        senderInfo.setTelNumber("12345678");
        senderInfo.setEmail("321546");
        senderInfo.setCountry("CN");
        senderInfo.setZipCode("515800");
        LogisticsProductVO logisticsProductVO = new LogisticsProductVO();
        logisticsProductVO.setId("12121232");
        logisticsProductVO.setSkuId("123456");
        logisticsProductVO.setEnglishUsage("materi");
        logisticsProductVO.setDeclareChineseName("物流");
        logisticsProductVO.setDeclareEnglishName("mta");
        logisticsProductVO.setDestDeclarePrice(new BigDecimal("12"));
        logisticsProductVO.setWeight(1999);
        logisticsProductVO.setQuantity(10);
        logisticsProductVO.setSourceCountry("CN");
        LogisticsChannelEntity logisticsChannel = new LogisticsChannelEntity();
        logisticsChannel.setCode("UBI.CA2US.CAPOST");
        //DDU/DDP
        logisticsChannel.setTaxModel("DDU");

        LogisticsSaleChannelEntity logisticsSaleChannel = new LogisticsSaleChannelEntity();
        logisticsSaleChannel.setCode("UBI.CA2US.CAPOST");
        logisticsSaleChannel.setShipmentMethod("Express-Post");
        logisticsSaleChannel.setPlatformChannelId("1725040739275055105");

        final LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
                .authMap(authMap)
//                .channelCode("")
//                .channelId("1725040739275055105")
                .orderSource("ERP")
//                .facility("can")
                .deliveryNo("wj12345167721")
                .receiverInfoVO(ReceiverInfoVO.builder()
                        .addressFirst("address")
                        .email("123@q.con")
                        .city("shenz")
                        .name("mark")
                        .companyName("componey")
                        .contact("mark")
                        .country("US")
                        .zipCode("11510")
                        .province("state")
                        .telNumber("123456789")
                        .build())
                .senderInfo(senderInfo)
                .parceInfoVO(ParceInfoVO.builder()
                        .currency("USD")
                        .height(1)
                        .hasBattery(true)
                        .totalPrice(new BigDecimal("120"))
                        .totalQuantity(10)
                        .totalWeight(1999)
                        .length(1)
                        .totalWeight(123)
                        .width(123)
                        .build())
                .logisticsProductVOList(Arrays.asList(
                        logisticsProductVO
                ))
                .logisticsChannelEntity(logisticsChannel)
                .build();
        ApiResult<LogisticsOrderResponseVO> order = shopeeLogisticsHandler.createOrder(logisticsOrderVO);
        System.out.println(order);
    }

    @Test
    public void queryOrderList(){
        LogisticsQueryBaseVO logisticsQueryVOList = new LogisticsQueryBaseVO();
        logisticsQueryVOList.setDeliveryNo("wj12345167721");
        logisticsQueryVOList.setAuthMap(authMap);
        ApiResult<List<LogisticsOrderResponseVO>> listApiResult = shopeeLogisticsHandler.queryOrderList(Collections.singletonList(logisticsQueryVOList));
        System.out.println(listApiResult);
    }

    @Test
    public void getLabelList() throws IOException {
        LogisticsGetLabelVO logisticsQueryVO2 = new LogisticsGetLabelVO();
        logisticsQueryVO2.setDeliveryNo("wj12345167721");
        logisticsQueryVO2.setAuthMap(authMap);
        ApiResult<List<LogisticsPrintLabelResponse>> labelList = shopeeLogisticsHandler.getLabelList(Collections.singletonList(logisticsQueryVO2));
        System.out.println(labelList);
    }

    @Test
    public void interceptOrder(){
        LogisticsInterceptOrderVO logisticsQueryVOList2 = new LogisticsInterceptOrderVO();
        logisticsQueryVOList2.setDeliveryNo("wj12345167721");
        logisticsQueryVOList2.setAuthMap(authMap);
        ApiResult<List<InterceptResponseVO>> listApiResult = shopeeLogisticsHandler.interceptOrder(Collections.singletonList(logisticsQueryVOList2));
        System.out.println(listApiResult);
    }

    @Test
    public void cancelOrder(){
        LogisticsCancelOrderVO logisticsQueryVOList = new LogisticsCancelOrderVO();
        logisticsQueryVOList.setDeliveryNo("wj12345167721");
        logisticsQueryVOList.setTransportNo("lBK4IuWt-IlRQrfmJhnniA");
        logisticsQueryVOList.setTrackNo("LM000002721CA");
        logisticsQueryVOList.setAuthMap(authMap);
        ApiResult<List<CancelResponseVO>> listApiResult = shopeeLogisticsHandler.cancelOrder(Collections.singletonList(logisticsQueryVOList));
        System.out.println(listApiResult);
    }

    @Test
    public void confirmOrder(){
        LogisticsQueryBaseVO logisticsQueryVO = new LogisticsCancelOrderVO();
        logisticsQueryVO.setDeliveryNo("wj12345167721");
        logisticsQueryVO.setTransportNo("lBK4IuWt-IlRQrfmJhnniA");
        logisticsQueryVO.setTrackNo("LM000002721CA");
        logisticsQueryVO.setAuthMap(authMap);
        ApiResult<List<ConfirmResponseVO>> listApiResult = shopeeLogisticsHandler.confirmOrder(Collections.singletonList(logisticsQueryVO));
        System.out.println(listApiResult);
    }

    @Test
    public void authorization() {
        ApiResult<Object>ApiResult<Object>= shopeeLogisticsHandler.authorization(authMap);
        System.out.println(apiResult);
    }
    @Test
    public void getShippingParameter() {
        BaseRequest baseRequest = BaseRequest.builder()
                .partnerKey(authMap.get("partnerKey"))
                .partnerId(Long.valueOf(authMap.get("partnerId")))
                .shopId(Long.valueOf(authMap.get("shopId")))
                .accessToken(authMap.get("token"))
                .host(authMap.get("host"))
                .build();
        String orderSn = "241014HEJQ5KGY";
        String packageNumber = "OFG182599777214119";
        ShipDetailResponse shippingParameter = shopeeLogisticsService.getShippingParameter(baseRequest, orderSn, packageNumber);
        System.out.println(shippingParameter);
    }
    @Test
    public void shippingOrder() {
        BaseRequest baseRequest = BaseRequest.builder()
                .partnerKey(authMap.get("partnerKey"))
                .partnerId(Long.valueOf(authMap.get("partnerId")))
                .shopId(Long.valueOf(authMap.get("shopId")))
                .accessToken(authMap.get("token"))
                .host(authMap.get("host"))
                .build();
//        String orderSn = "2410119RC3WKGU";
//        String packageNumber = "OFG182340319214041";
        String orderSn = "241015KMF3U41R";
        String packageNumber = "OFG182674818215655";
        ShipDetailResponse shippingParameter = shopeeLogisticsService.getShippingParameter(baseRequest, orderSn, packageNumber);
        ShipDropInfo dropoff = shippingParameter.getDropoff();
        ShipOrderRequest shipOrderRequest = null;
        if (CollectionUtil.isEmpty(dropoff.getBranchInfoList())){
            shipOrderRequest = ShipOrderRequest.builder()
                    .orderSn(orderSn)
                    .dropoff(Dropoff.builder().build())
                    .build();
        }else {
            String logisticsChannelName = "";
            String logisticsNo = "";
            Dropoff dropoff1 = Dropoff.builder()
                    .branchId(dropoff.getBranchInfoList().get(0).getBranchId())
                    .senderRealName(logisticsChannelName)
                    .slug(dropoff.getSlugInfoList().get(0).getSlug())
                    .trackingNumber(logisticsNo)
                    .build();
            shipOrderRequest = ShipOrderRequest.builder()
                    .orderSn(orderSn)
                    .dropoff(dropoff1)
                    .packageNumber(packageNumber)
                    .build();
        }
        BaseResponse baseResponse = shopeeLogisticsService.shippingOrder(baseRequest, shipOrderRequest);
        //{"error":"logistics.ship_order_not_ready_to_ship","message":"The order is not ready to ship.","request_id":"317a860d24923e8906ac682e6cd4f300:010002cde045a22a:0000002cf04e45dc"}
        System.out.println(baseResponse);
    }
    @Test
    public void getShippingDocumentParameter() {
        BaseRequest baseRequest = BaseRequest.builder()
                .partnerKey(authMap.get("partnerKey"))
                .partnerId(Long.valueOf(authMap.get("partnerId")))
                .shopId(Long.valueOf(authMap.get("shopId")))
                .accessToken(authMap.get("token"))
                .host(authMap.get("host"))
                .build();
        String orderSn = "241016P6BFN5KQ";
        String packageNumber = "OFG182599777214119";
//        String orderSn = "2410119RC3WKGU";
//        String packageNumber = "OFG182340319214041";
//        String orderSn = "2409068X5S22U7";//527508280
//        String packageNumber = "OFG179307286219949";
        List<ShippingOrderRequest > orderRequestList = new ArrayList<>();
        ShippingOrderRequest shippingOrderRequest = ShippingOrderRequest.builder()
                .orderSn(orderSn)
//                .packageNumber(packageNumber)
                .build();
        orderRequestList.add(shippingOrderRequest);
        List<ShippingDocumentParameterResponse> shippingDocumentParameter = shopeeLogisticsService.getShippingDocumentParameter(baseRequest, orderRequestList);
        System.out.println(shippingDocumentParameter);
        //{"error":"","message":"","response":{"result_list":[{"order_sn":"2410119RC3WKGU","package_number":"OFG182340319214041","suggest_shipping_document_type":"THERMAL_AIR_WAYBILL","selectable_shipping_document_type":["THERMAL_AIR_WAYBILL"]}]},"warning":null,"request_id":"317a860d2492453f041c9641451e0f00:010002b15c916242:00000024262ea989"}
    }

    @Test
    public void getTrackNumber() {
//        String orderSn = "2410119RC3WKGU";
//        String packageNumber = "OFG182340319214041";
//        String orderSn = "2409068X5S22U7";//527508280
//        String packageNumber = "OFG179307286219949";
        String orderSn = "241016P6BFN5KQ";//180939511
        String packageNumber = "OFG182599777214119";
        BaseRequest baseRequest = BaseRequest.builder()
                .partnerKey(authMap.get("partnerKey"))
                .partnerId(Long.valueOf(authMap.get("partnerId")))
                .shopId(Long.valueOf(authMap.get("shopId")))
                .accessToken(authMap.get("token"))
                .host(authMap.get("host"))
                .build();
        TrackResponse trackNumber = shopeeLogisticsService.getTrackNumber(baseRequest, orderSn);

        System.out.println(trackNumber);
    }

    @Test
    public void createShippingDocument() {
        BaseRequest baseRequest = BaseRequest.builder()
                .partnerKey(authMap.get("partnerKey"))
                .partnerId(Long.valueOf(authMap.get("partnerId")))
                .shopId(Long.valueOf(authMap.get("shopId")))
                .accessToken(authMap.get("token"))
                .host(authMap.get("host"))
                .build();
        String orderSn = "241014HEJQ5KGY";//180939511
        String trackingNumber = "180939511";
        String packageNumber = "OFG182599777214119";
//        String orderSn = "2410119RC3WKGU";
//        String packageNumber = "OFG182340319214041";
//        String orderSn = "2409068X5S22U7";//527508280
//        String packageNumber = "OFG179307286219949";
        List<ShippingOrderRequest > orderRequestList = new ArrayList<>();
        ShippingOrderRequest shippingOrderRequest = ShippingOrderRequest.builder()
                .orderSn(orderSn)
                .packageNumber(packageNumber)
                .trackingNumber(trackingNumber)
                .build();
        orderRequestList.add(shippingOrderRequest);
        List<ShippingDocumentParameterResponse> shippingDocumentParameter = shopeeLogisticsService.createShippingDocument(baseRequest, orderRequestList);
        //{"error":"common.batch_api_all_failed","message":"Failed, please check result_list for more details.","response":{"result_list":[{"order_sn":"2410119RC3WKGU","package_number":"OFG182340319214041","fail_error":"logistics.package_can_not_print","fail_message":"The package can not print now. Detail: The document is not yet ready for printing. Please try again later."}]},"request_id":"317a860d2492530ace56bc0bf8213100:010002a7e68b73ae:000000c649ec5e22"}
        System.out.println(shippingDocumentParameter);
    }

    @Test
    public void getShippingDocumentResult() {
        BaseRequest baseRequest = BaseRequest.builder()
                .partnerKey(authMap.get("partnerKey"))
                .partnerId(Long.valueOf(authMap.get("partnerId")))
                .shopId(Long.valueOf(authMap.get("shopId")))
                .accessToken(authMap.get("token"))
                .host(authMap.get("host"))
                .build();
        String orderSn = "241014HEJQ5KGY";//180939511
        String trackingNumber = "180939511";
        String packageNumber = "OFG182599777214119";
        List<ShippingOrderRequest > orderRequestList = new ArrayList<>();
        ShippingOrderRequest shippingOrderRequest = ShippingOrderRequest.builder()
                .orderSn(orderSn)
                .trackingNumber(trackingNumber)
//                .orderSn("2410119RC3WKGU")
                .packageNumber(packageNumber)
//                .packageNumber("OFG182340319214041")
                .build();
        orderRequestList.add(shippingOrderRequest);
        List<ShippingDocumentParameterResponse> shippingDocumentParameter = shopeeLogisticsService.getShippingDocumentResult(baseRequest, orderRequestList);

        System.out.println(shippingDocumentParameter);
    }

    @Test
    public void downloadShippingDocument() {
        BaseRequest baseRequest = BaseRequest.builder()
                .partnerKey(authMap.get("partnerKey"))
                .partnerId(Long.valueOf(authMap.get("partnerId")))
                .shopId(Long.valueOf(authMap.get("shopId")))
                .accessToken(authMap.get("token"))
                .host(authMap.get("host"))
                .build();
        String orderSn = "241016P6BFN5KQ";//180939511
        String trackingNumber = "180939511";
        String packageNumber = "OFG182599777214119";
        List<ShippingOrderRequest > orderRequestList = new ArrayList<>();
        ShippingOrderRequest shippingOrderRequest = ShippingOrderRequest.builder()
                .orderSn(orderSn)
//                .orderSn("2410119RC3WKGU")
//                .packageNumber("OFG179307286219949")
//                .packageNumber(packageNumber)
                .build();
        orderRequestList.add(shippingOrderRequest);
        String shippingDocumentType = "NORMAL_AIR_WAYBILL";
        String bytes = shopeeLogisticsService.downloadShippingDocument(baseRequest, orderRequestList, shippingDocumentType);

        System.out.println(bytes);
    }
}
