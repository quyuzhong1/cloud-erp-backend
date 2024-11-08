package com.erp.server.tms.service.handover;

import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.exception.ServiceException;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.request.OrderRequest;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.server.tms.ErpServerTmsApplication;
import com.erp.server.tms.service.logistics.AliExpressLogisticsHandlerImpl;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.constants.PathConstants;
import com.erp.tms.aliexpress.model.handover.*;
import com.erp.tms.aliexpress.model.handover.request.*;
import com.erp.tms.aliexpress.model.handover.response.*;
import com.erp.tms.aliexpress.model.order.request.QueryOrderRequest;
import com.erp.tms.aliexpress.model.order.response.BaseResult;
import com.erp.tms.aliexpress.service.AliExpressHandoverService;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
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
public class AliExpressOrderHandlerImplTest {
    String TOP_USER_KEY = "2671706312";
    String CLIENT = "ISV-数大臣";

    @Resource
    private AliExpressLogisticsHandlerImpl aliExpressLogisticsHandler;
    @Resource
    private AliExpressHandoverService aliExpressHandoverService;
    @Resource
    private AliExpressOrderService aliExpressOrderService;
    @Resource
    private AliExpressShipperService aliExpressShipperService;
    private Map<String, String> authMap = new HashMap<>();

    public AliExpressOrderHandlerImplTest(){
        //test
//        String CLIENT_CODE = "502978";
//        String CHECK_WORD = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
//        String token = "50000700312cJ4nYbrzErAqH159364aboXoPdAcJwjMuEzrGXEAudlrVcjGsjo4bIr30";
        //prod
        String CLIENT_CODE = "503630";
        String CHECK_WORD = "PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ";
        String token = "50000500839eewdp7jko0vtD80GwgipeyTiagAS9MvvfgdtLzm910eedab8AhgxYjWqN";
        authMap.put("clientId",CLIENT_CODE);
        authMap.put("clientSecret",CHECK_WORD);
        authMap.put("token",token);
        authMap.put("url","https://api-sg.aliexpress.com");
    }

    public Map<String, String> getLogisticsAuthConfig(){
        Map<String, String> logisticsAuthConfig = aliExpressLogisticsHandler.getLogisticsAuthConfigByShopId("");
        return logisticsAuthConfig;
    }
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(){
        List<Map<String, String>> logisticsAuthConfigByPlatform = aliExpressLogisticsHandler.getLogisticsAuthConfigByPlatform(LogisticsPlatformEnum.SHOPEE.getCode());
        return logisticsAuthConfigByPlatform;
    }

    /**
     * 订单列表
     * @throws com.erp.oms.aliexpress.util.ApiException
     */
    @Test
    public void getOrderList() throws ApiException {
        String apiName = AliexpressConstants.LIST_ORDER;
        OrderRequest orderRequest = OrderRequest.builder().
                clientId(authMap.get("clientId")).
                clientSecret(authMap.get("clientSecret")).
                startTime("2024-01-30 00:00:00").
                endTime("2024-02-02 00:00:00").
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
     * 菜鸟国际出口 提交订单
     * @throws com.erp.tms.aliexpress.util.ApiException
     */
    @Test
    public void commit() throws com.erp.tms.aliexpress.util.ApiException {
        SellerParcelOrder sellerParcelOrder = SellerParcelOrder.builder()
                .sellerId(TOP_USER_KEY)
                .orderCodeList(Collections.singletonList("3030096741101976"))
                .userNick("cn1072056312gdtae")
                .build();

        AddressBase addressBase = AddressBase.builder()
                .city("Rinchoa")
                .country("PT")
                .detailAddress("Praca do R********")
                .province("Rio de Mouro")
                .district("Rio")
                .zipCode("2635-457")
                .street("mouro")
                .build();
        AddressInfo addressInfo = AddressInfo.builder()
                .address(addressBase)
                .email("123@qq.com")
                .name("dddd")
                .mobile("92****596")
                .phone("17654546969")
                .addressId("440174520062")
                .build();
        UserInfo userInfo = UserInfo.builder().topUserKey(TOP_USER_KEY).build();
        Features features = Features.builder()
                .palletQuantity(1)
                .containerType("1")
                .gmtReadyToShip(System.currentTimeMillis())
                .prePackage("true")
                .build();
        CommitRequest commitRequest = CommitRequest.builder()
                .sellerParcelOrderList(Collections.singletonList(sellerParcelOrder))
                .skipInvalidParcel(true)
                .remark("xxx")
//                .returnInfo(addressInfo)
                .pickInfo(addressInfo)
                .orderCodeList(Collections.singletonList("3030096741091976"))
                .weight(new BigDecimal(1000))
                .handoverOrderId("")
                .userInfo(userInfo)
                .weightUnit("kg")
                .type("cainiao_pickup")
                .client(CLIENT)
                .locale("zh_CN")
                .features(features)
                .appointmentType("bigbag")
                .domesticTrackingNo("SF22324322")
                .domesticLogisticsCompany("SF")
                .domesticLogisticsCompanyId("505")
                .build();
        IopResponse response = aliExpressHandoverService.commit(authMap, commitRequest);
        BaseResult baseResult = JSONObject.parseObject(response.getBody(),BaseResult.class);
        if (Objects.nonNull(baseResult.getErrorResponse())){
            System.out.println(JSONObject.toJSONString(baseResult.getErrorResponse()));
        }
        HandoverCommitResponse handoverCommitResponse = JSONObject.parseObject(baseResult.getData(), HandoverCommitResponse.class);
        System.out.println("结果输出");
        System.out.println(JSONObject.toJSONString(handoverCommitResponse));
    }

    @Test
    public void testCommit(){
        String body = "{\"result\":{\"data\":{\"handover_order_id\":10071881812,\"handover_content_code\":\"LP00631998284401\",\"handover_content_id\":10084152400},\"success\":true},\"request_id\":\"213bd16f17084991518705952\"}";
        BaseResult baseResult = JSONObject.parseObject(body, BaseResult.class);
        if (Objects.nonNull(baseResult.getErrorResponse())) {
            throw new ServiceException(baseResult.getErrorResponse().getSubMsg());
        }
        HandoverCommitResult handoverCommitResult = JSONObject.parseObject(baseResult.getResult(), HandoverCommitResult.class);
        System.out.println("结果输出");
        System.out.println(JSONObject.toJSONString(handoverCommitResult));
    }
    /**
     * 返回直接解决方案的指定物流服务的可用资源列表
     */
    @Test
    public void queryService() throws com.erp.tms.aliexpress.util.ApiException {
        ServiceRequest serviceRequest = ServiceRequest.builder()
                .tradeOrderId("3028833906081879")
                .intlTrackingNo("PQ936A0792035050134690Z")
                .outOrderCode("LP00622895695059")
                .reason("批准")
                .build();
        IopResponse response = aliExpressHandoverService.queryService(authMap, serviceRequest);
        System.out.println("结果输出");
        BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
        System.out.println(JSONObject.toJSONString(baseResponse));
        BaseResult baseResult = JSONObject.parseObject(baseResponse.getResponse(),BaseResult.class);
        System.out.println(JSONObject.toJSONString(baseResult));
    }

    /**
     * 订单明细查询
     */
    @Test
    public void queryOrder() throws com.erp.tms.aliexpress.util.ApiException {
        QueryOrderRequest queryOrderRequest = QueryOrderRequest.builder()
                .trade_order_id("3040480645907417")
                .current_page(1)
                .page_size(20)
                .build();
        BaseResult baseResult = aliExpressShipperService.queryLogisticsOrder(authMap, queryOrderRequest);
        System.out.println("结果输出");
        System.out.println(JSONObject.toJSONString(baseResult));
    }

    /**
     * 大包详情
     * @throws com.erp.tms.aliexpress.util.ApiException
     */
    @Test
    public void queryContent() throws com.erp.tms.aliexpress.util.ApiException {
                HandoverQueryRequest handoverQueryRequest = HandoverQueryRequest.builder()
                .client(PathConstants.CLIENT)
                .locale("zh_CN")
                .orderCode("LP00632655786123")
                .userInfo(UserInfo.builder().topUserKey(PathConstants.TOP_USER_KEY).build())
                .build();
        IopResponse response = aliExpressHandoverService.queryContent(authMap, handoverQueryRequest);
        BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
        BaseResult baseResult = JSONObject.parseObject(baseResponse.getResult(), BaseResult.class);
        if (baseResult.getSuccess() && StringUtils.isNotEmpty(baseResult.getData())){
            HandoverQueryResponse queryResponse = JSONObject.parseObject(baseResult.getData(), HandoverQueryResponse.class);
            System.out.println(queryResponse);
        }
        System.out.println(baseResult);
    }

    /**
     * 批次追加大包
     * @throws com.erp.tms.aliexpress.util.ApiException
     */
    @Test
    public void subbagAdd() throws com.erp.tms.aliexpress.util.ApiException {
        SubbagRequest subbagRequest = SubbagRequest.builder()
                .userInfo(UserInfo.builder().topUserKey(PathConstants.TOP_USER_KEY).build())
                .addSubbagQuantity(1)
                .orderCode("8183868002476390")
                .locale("zh_CN")
                .build();
        IopResponse response = aliExpressHandoverService.subbagAdd(authMap, subbagRequest);
//        BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
        BaseResult baseResult = JSONObject.parseObject(response.getBody(), BaseResult.class);
        System.out.println(baseResult);
    }

    /**
     * 提供给ISV通过该接口查询小包信息
     * @throws com.erp.tms.aliexpress.util.ApiException
     */
    @Test
    public void queryParcel() throws com.erp.tms.aliexpress.util.ApiException {
        HandoverQueryRequest handoverQueryRequest = HandoverQueryRequest.builder()
                .client(PathConstants.CLIENT)
                .locale("zh_CN")
                .orderCode("8183868002476390")
//                .trackingNumber("LP00629346935157")
                .userInfo(UserInfo.builder().topUserKey(PathConstants.TOP_USER_KEY).build())
                .build();
        IopResponse response = aliExpressHandoverService.queryParcel(authMap, handoverQueryRequest);
        BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
        BaseResult baseResult = JSONObject.parseObject(baseResponse.getResult(), BaseResult.class);
        System.out.println(baseResult);
    }

    /**
     * 提供给ISV通过该接口修改交接单
     * @throws com.erp.tms.aliexpress.util.ApiException
     */
    @Test
    public void update() throws com.erp.tms.aliexpress.util.ApiException {
        AddressBase addressBase = AddressBase.builder()
                .city("Rinchoa")
                .country("PT")
                .detailAddress("Praca do R********")
                .province("Rio de Mouro")
                .district("Rio")
                .zipCode("2635-457")
                .street("mouro")
                .build();
        AddressInfo addressInfo = AddressInfo.builder()
                .address(addressBase)
                .email("123@qq.com")
                .name("dddd")
                .mobile("92****596")
                .phone("17654546969")
                .addressId("440174520062")
                .build();
        UserInfo userInfo = UserInfo.builder().topUserKey(TOP_USER_KEY).build();
        UpdateRequest updateRequest = UpdateRequest.builder()
                .handoverOrderId("")
                .client(CLIENT)
                .locale("zh_CN")
                .orderCodeList(Collections.singletonList("3030096741091976"))
                .pickInfo(addressInfo)
                .remark("dd")
                .returnInfo(addressInfo)
                .type("cainiao_pickup")
                .userInfo(userInfo)
                .weight(new BigDecimal(1000))
                .weightUnit("kg")
                .build();
        HandoverQueryRequest handoverQueryRequest = HandoverQueryRequest.builder()
                .client(PathConstants.CLIENT)
                .locale("zh_CN")
                .orderCode("8183868002476390")
                .trackingNumber("LP00629346935157")
                .userInfo(UserInfo.builder().topUserKey(PathConstants.TOP_USER_KEY).build())
                .build();
        IopResponse response = aliExpressHandoverService.update(authMap, updateRequest);
        BaseResult baseResult = JSONObject.parseObject(response.getBody(), BaseResult.class);
        System.out.println(baseResult);
    }

    /**
     * 提供给ISV通过该接口取消交接单
     * @throws com.erp.tms.aliexpress.util.ApiException
     */
    @Test
    public void cancel() throws com.erp.tms.aliexpress.util.ApiException {
        CancelRequest cancelRequest = CancelRequest.builder()
                .client(CLIENT)
                .handoverContentId(8183868002476390L)
                .handoverOrderId("8183868002476390")
                .locale("zh_CN")
                .trackingNumber("LP00629346935157")
                .userInfo(UserInfo.builder().topUserKey(PathConstants.TOP_USER_KEY).build())
                .build();
        IopResponse response = aliExpressHandoverService.cancel(authMap, cancelRequest);
//        BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
        BaseResult baseResult = JSONObject.parseObject(response.getBody(), BaseResult.class);
        System.out.println(baseResult);
    }
    /**
     * 提供给ISV通过该接口获取面单云打印数据
     * @throws com.erp.tms.aliexpress.util.ApiException
     */
    @Test
    public void cloudPrint() throws com.erp.tms.aliexpress.util.ApiException {
        CloudPrintRequest cloudPrintRequest = CloudPrintRequest.builder()
                .client(CLIENT)
                .locale("zh_CN")
                .orderCode("LP00631998284401")
//                .trackingNumber("LP00629346935157")
                .userInfo(UserInfo.builder().topUserKey(PathConstants.TOP_USER_KEY).build())
                .build();
        IopResponse response = aliExpressHandoverService.cloudPrint(authMap, cloudPrintRequest);
        BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
        BaseResult baseResult = JSONObject.parseObject(baseResponse.getResult(), BaseResult.class);
        System.out.println(baseResult);
    }
    /**
     * 返回指定大包面单的PDF文件数据
     * @throws com.erp.tms.aliexpress.util.ApiException
     */
    @Test
    public void getPdf() throws com.erp.tms.aliexpress.util.ApiException {
        PdfRequest pdfRequest = PdfRequest.builder()
                .client(CLIENT)
                .handoverContentId(10084152400L)
                .locale("zh_CN")
                .type(1)
                .userInfo(UserInfo.builder().topUserKey(PathConstants.TOP_USER_KEY).build())
                .build();
        IopResponse response = aliExpressHandoverService.getPdf(authMap, pdfRequest);
        BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
        BaseResult baseResult = JSONObject.parseObject(baseResponse.getResult(), BaseResult.class);
        if (StringUtils.isNotEmpty(baseResult.getErrorMsg()) || StringUtils.isEmpty(baseResult.getData())){
            //接口异常
        }
        System.out.println(baseResult);
        PdfResponse pdfResponse = JSONObject.parseObject(baseResult.getData(), PdfResponse.class);
        System.out.println(pdfResponse);
        String prefix = "data:application/pdf;base64,";
        String base64Str = prefix + pdfResponse.getBody();
        //文件base64
        System.out.println(base64Str);
    }
    /**
     * 揽收资源推荐
     * @throws com.erp.tms.aliexpress.util.ApiException
     */
    @Test
    public void resourceRecommend() throws com.erp.tms.aliexpress.util.ApiException {
        AddressBase addressBase = AddressBase.builder()
                .city("Rinchoa")
                .country("PT")
                .detailAddress("Praca do R********")
                .province("Rio de Mouro")
                .district("Rio")
                .zipCode("2635-457")
                .street("mouro")
                .build();
        AddressInfo addressInfo = AddressInfo.builder()
                .address(addressBase)
                .email("123@qq.com")
                .name("dddd")
                .mobile("92****596")
                .phone("17654546969")
                .addressId("440174520062")
                .build();
        ResourceRecommendRequest resourceRecommendRequest = ResourceRecommendRequest.builder()
                .pickInfo(addressInfo)
                .pickupType("SELF_POST")
                .solutionCode("ddd")
                .userInfo(UserInfo.builder().topUserKey(PathConstants.TOP_USER_KEY).build())
                .build();
        IopResponse response = aliExpressHandoverService.resourceRecommend(authMap, resourceRecommendRequest);
//        BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
        BaseResult baseResult = JSONObject.parseObject(response.getBody(), BaseResult.class);
        System.out.println(baseResult);
    }
    /**
     * 查询出所有的实际承运商
     * @throws com.erp.tms.aliexpress.util.ApiException
     */
    @Test
    public void queryCarrierList() throws com.erp.tms.aliexpress.util.ApiException {
        String locale ="zh_CN";
        IopResponse response = aliExpressHandoverService.queryCarrierList(authMap, locale);
//        BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
        BaseResult baseResult = JSONObject.parseObject(response.getBody(), BaseResult.class);
        System.out.println(baseResult);
        JSONObject jsonObject1 = JSONObject.parseObject(baseResult.getResult());
        JSONObject jsonObject2 = JSONObject.parseObject(jsonObject1.getString("data"));
        List<CarrierResponse> carrierResponses = JSONObject.parseArray(jsonObject2.getString("courier_list"), CarrierResponse.class);
        System.out.println(carrierResponses);
    }
}
