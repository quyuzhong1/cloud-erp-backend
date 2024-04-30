package com.erp.server.tms.service.logistics;

import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.LogisticsAddressTypeEnum;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.request.OrderRequest;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.server.tms.ErpServerTmsApplication;
import com.erp.server.tms.convert.LogisticsAddressConverter;
import com.erp.server.tms.service.LogisticsAddressService;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.model.address.SellerResponse;
import com.erp.tms.aliexpress.model.order.request.Address;
import com.erp.tms.aliexpress.model.query.request.QueryLogisticsRequest;
import com.erp.tms.aliexpress.model.query.response.LogisticsServiceResponse;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import com.erp.tms.aliexpress.util.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
 * @description: TODO
 * @date 2023年11月16日
 * @version: 1.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class AliExpressLogisticsHandlerImplTest {
    String TOP_USER_KEY = "2671706312";
    String CLIENT = "ISV-数大臣";
    @Resource
    private AliExpressLogisticsHandlerImpl aliExpressLogisticsHandler;
    @Resource
    private AliExpressShipperService aliExpressShipperService;

    @Resource
    private AliExpressOrderService aliExpressOrderService;

    @Resource
    private LogisticsAddressService logisticsAddressService;
    private Map<String, String> authMap = new HashMap<>();

    public AliExpressLogisticsHandlerImplTest(){
        String CLIENT_CODE = "502978";
        String CHECK_WORD = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
        String token = "50000201913g5RZqpecEaQ6pT179453ddTkJkRLXEqUDEXFxOEwPXvtsX3DHKlWZJx01";
        authMap.put("clientId",CLIENT_CODE);
        authMap.put("clientSecret",CHECK_WORD);
        authMap.put("token",token);
        authMap.put("url","https://api-sg.aliexpress.com");
    }

    public Map<String, String> getLogisticsAuthConfig(){
        Map<String, String> logisticsAuthConfig = aliExpressLogisticsHandler.getLogisticsAuthConfig("");
        return logisticsAuthConfig;
    }
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(){
        List<Map<String, String>> logisticsAuthConfigByPlatform = aliExpressLogisticsHandler.getLogisticsAuthConfigByPlatform(LogisticsPlatformEnum.SHOPEE.getCode());
        return logisticsAuthConfigByPlatform;
    }

    @Test
    public void getChannel() {
        ApiResult<List<LogisticsSaleChannelEntity>> channel = aliExpressLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build());
        System.out.println(channel);
    }

    @Test
    public void createOrder(){
        //发货人
        SenderInfo senderInfo = new SenderInfo();
        senderInfo.setId("440174196461");
        senderInfo.setAddressFirst("5-8#lift 9th floor buliding 205 xinyang street xintaiyang Industrial Park lin village");
        senderInfo.setContact("contact");
        senderInfo.setCityName("Dongguan");
        senderInfo.setCompanyName("4PX");
        senderInfo.setName("chenxuli");
        senderInfo.setProvinceName("Guangdong Province");
        senderInfo.setTelNumber("17191087538");
        senderInfo.setEmail("dhphoto@aliyun.com");
        senderInfo.setCountry("CN");
        senderInfo.setZipCode("523000");
        //上门揽收
        SenderInfo pickUp = new SenderInfo();
        pickUp.setId("440174520062");
        pickUp.setAddressFirst("5-8#lift 9th floor buliding 205 xinyang street xintaiyang Industrial Park lin village");
        pickUp.setContact("contact");
        pickUp.setCityName("Dongguan");
        pickUp.setCompanyName("4PX");
        pickUp.setName("chenxuli");
        pickUp.setProvinceName("Guangdong Province");
        pickUp.setTelNumber("17191087538");
        pickUp.setEmail("dhphoto@aliyun.com");
        pickUp.setCountry("CN");
        pickUp.setZipCode("523000");
        //退货
        SenderInfo returnInfo = new SenderInfo();
        returnInfo.setId("440173332446");
        returnInfo.setAddressFirst("5-8#lift 9th floor buliding 205 xinyang street xintaiyang Industrial Park lin village");
        returnInfo.setContact("contact");
        returnInfo.setCityName("Dongguan");
        returnInfo.setCompanyName("4PX");
        returnInfo.setName("chenxuli");
        returnInfo.setProvinceName("Guangdong Province");
        returnInfo.setTelNumber("17191087538");
        returnInfo.setEmail("dhphoto@aliyun.com");
        returnInfo.setCountry("CN");
        returnInfo.setZipCode("523000");

        LogisticsProductVO logisticsProductVO = new LogisticsProductVO();
        logisticsProductVO.setId("1005005951455393");
        logisticsProductVO.setSkuId("1005005616949322");
        logisticsProductVO.setEnglishUsage("materi");
        logisticsProductVO.setDeclareChineseName("物流");
        logisticsProductVO.setDeclareEnglishName("mta");
        logisticsProductVO.setDestDeclarePrice(new BigDecimal("197.07"));
        logisticsProductVO.setWeight(1);
        logisticsProductVO.setQuantity(2);
        logisticsProductVO.setSourceCountry("CN");
        logisticsProductVO.setIsElectric(false);
        logisticsProductVO.setDeclarePrice(BigDecimal.valueOf(2));
        logisticsProductVO.setDestDeclarePrice(BigDecimal.valueOf(2));
        logisticsProductVO.setChildOrderId(8184516086176025L);
        logisticsProductVO.setScItemCode("");
//        logisticsProductVO.setScItemId(40414943126L);
//        logisticsProductVO.setScItemName("");
//        logisticsProductVO.setSkuCode("L083GBB1");
//        logisticsProductVO.setSkuName("A018GBB1");

        LogisticsSaleChannelEntity logisticsSaleChannel = new LogisticsSaleChannelEntity();
        logisticsSaleChannel.setCode("CAINIAO_STANDARD_FPXQZ");
        logisticsSaleChannel.setShipmentMethod("Express-Post");
        logisticsSaleChannel.setPlatformChannelId("11169435");
        logisticsSaleChannel.setSupplierName("CAINIAONNRM");
        LogisticsChannelEntity logisticsChannel = new LogisticsChannelEntity();
//        logisticsChannel.setCode("S832");
        logisticsChannel.setId("1724614809662599171");
        //DDU/DDP
        logisticsChannel.setTaxModel("DDU");
        final LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
                .authMap(authMap)
                .orderSource("ERP")
                .pickupType("DOOR_PICKUP")
//                .pickupType("SELF_SEND")
//                .pickupType("SELF_POST")
//                .facility("can")
                .deliveryNo("8184516086166025")
//                .deliveryNo("1102175972276889")
                .receiverInfoVO(ReceiverInfoVO.builder()
                        .streetAddress("Calle Alcatraz 244, Fraccionamiento Vistas de Tesistán, 45200 Zapopan, J")
                        .email("965656546@qq.con")
                        .city("Uskudar")
                        .name("tr1011044319")
                        .companyName("tr1011044319")
                        .contact("zhang san")
                        .country("US")
                        .zipCode("CV56DY")
                        .province("Istanbul")
                        .telNumber("1234567890")
                        .build())
                .senderInfo(senderInfo)
                .pickUpInfo(pickUp)
                .returnInfo(returnInfo)
                .parceInfoVO(ParceInfoVO.builder()
                        .currency("CNY")
                        .height(1)
                        .hasBattery(true)
                        .totalPrice(new BigDecimal("394.14"))
                        .totalQuantity(1)
                        .totalWeight(1)
                        .length(1)
                        .totalWeight(1)
                        .width(1)
                        .build())
                .logisticsProductVOList(Arrays.asList(
                        logisticsProductVO
                ))
                .logisticsChannelEntity(logisticsChannel)
                .logisticsSaleChannel(logisticsSaleChannel)
                .topUserKey(TOP_USER_KEY)
                .build();
        ApiResult<LogisticsOrderResponseVO> order = aliExpressLogisticsHandler.createOrder(logisticsOrderVO);
        System.out.println(order);
    }

    @Test
    public void queryOrderList(){
        LogisticsQueryBaseVO logisticsQueryVOList = new LogisticsQueryBaseVO();
        logisticsQueryVOList.setDeliveryNo("3028833906081879");
        logisticsQueryVOList.setAuthMap(authMap);
        ApiResult<List<LogisticsOrderResponseVO>> listApiResult = aliExpressLogisticsHandler.queryOrderList(Collections.singletonList(logisticsQueryVOList));
        System.out.println("结果输出");
        System.out.println(JSONObject.toJSONString(listApiResult));
    }

    /**
     * 获取面签
     * @throws IOException
     */
    @Test
    public void getLabelList() throws IOException {
        LogisticsGetLabelVO logisticsQueryVO2 = new LogisticsGetLabelVO();
        logisticsQueryVO2.setDeliveryNo("3028833906081879");
        logisticsQueryVO2.setTransportNo("241166290576312");
//        logisticsQueryVO2.setTransportNo("PQ936A0792035050134690Z ");
        logisticsQueryVO2.setAuthMap(authMap);
        logisticsQueryVO2.setLabelType("1");
        ApiResult<List<LogisticsPrintLabelResponse>> labelList = aliExpressLogisticsHandler.getLabelList(Collections.singletonList(logisticsQueryVO2));
        System.out.println(labelList);
    }
    @Test
    public void authorization() {
        ApiResult apiResult = aliExpressLogisticsHandler.authorization(authMap);
        System.out.println(apiResult);
    }

    /**
     * 订单可发仓库列表
     * @throws ApiException
     */
    @Test
    public void getLogisticsService() throws ApiException {
        QueryLogisticsRequest queryLogisticsRequest =  QueryLogisticsRequest.builder()
                .order_id(8183524352516601L)
                .goods_weight("1")
                .goods_height(1L)
                .goods_width(1L)
                .goods_length(1L)
//                .order_id(1102175972276889L)
                .build();
        QueryLogisticsRequest queryLogisticsRequest1 =  QueryLogisticsRequest.builder()
                .order_id(8183524352506601L)
                .goods_weight("1")
                .goods_height(1L)
                .goods_width(1L)
                .goods_length(1L)
                .sub_order_list(Collections.singletonList(queryLogisticsRequest))
                .build();
        IopResponse logisticsService = aliExpressShipperService.getLogisticsService(authMap, queryLogisticsRequest1);
        System.out.println(logisticsService);
    }

    /**
     * 订单列表
     * @throws com.erp.oms.aliexpress.util.ApiException
     */
    @Test
    public void getOrderList() throws com.erp.oms.aliexpress.util.ApiException {
        String apiName = AliexpressConstants.LIST_ORDER;
        OrderRequest orderRequest = OrderRequest.builder().
                clientId(authMap.get("clientId")).
                clientSecret(authMap.get("clientSecret")).
                startTime("2024-02-18 00:00:00").
                endTime("2024-02-20 00:00:00").
                baseUrl(authMap.get("url")).
                apiName(apiName).
                currentPage(1).
                token(authMap.get("token")).build();
        List<AliExpressOrder > orderList = new ArrayList<>();
        aliExpressOrderService.listOrder(orderRequest, orderList);
        System.out.println("订单列表");
        System.out.println(JSONObject.toJSONString(orderList));
    }

    /**
     * 卖家信息
     */
    @Test
    public void getSellerInfo() throws ApiException {
        IopResponse sellerInfo = aliExpressShipperService.getSellerInfo(authMap);
        System.out.println(sellerInfo);
    }

    /**
     * 卖家地址信息
     */
    @Test
    public void getLogisticsAddress() throws ApiException {
        IopResponse sellerInfo = aliExpressShipperService.getLogisticsAddress(authMap);
        SellerResponse responseMsg = JSONObject.parseObject(sellerInfo.getBody(), SellerResponse.class);
        List<Address> senders = responseMsg.getSenders();
        List<Address> pickups = responseMsg.getPickups();
        List<Address> refunds = responseMsg.getRefunds();
        if (CollectionUtils.isNotEmpty(senders)){
            List<LogisticsAddressEntity> addressEntities = LogisticsAddressConverter.INSTANCE.sellerAddressToLogisticsAddress(senders);
            addressEntities.forEach(sender ->{
                sender.setType(LogisticsAddressTypeEnum.DELIVER);
            });
            logisticsAddressService.saveBatch(addressEntities);
        }
        if (CollectionUtils.isNotEmpty(pickups)){
            List<LogisticsAddressEntity> addressEntities = LogisticsAddressConverter.INSTANCE.sellerAddressToLogisticsAddress(pickups);
            addressEntities.forEach(sender ->{
                sender.setType(LogisticsAddressTypeEnum.COLLECT);
            });
            logisticsAddressService.saveBatch(addressEntities);
        }
        if (CollectionUtils.isNotEmpty(refunds)){
            List<LogisticsAddressEntity> addressEntities = LogisticsAddressConverter.INSTANCE.sellerAddressToLogisticsAddress(refunds);
            addressEntities.forEach(sender ->{
                sender.setType(LogisticsAddressTypeEnum.REFUND);
            });
            logisticsAddressService.saveBatch(addressEntities);
        }
        System.out.println(responseMsg);
    }
}
