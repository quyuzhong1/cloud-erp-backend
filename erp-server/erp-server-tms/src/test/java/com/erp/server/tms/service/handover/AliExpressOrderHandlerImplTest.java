package com.erp.server.tms.service.handover;

import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.LogisticsPlatformEnum;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.request.OrderRequest;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.server.tms.ErpServerTmsApplication;
import com.erp.server.tms.service.logistics.AliExpressLogisticsHandlerImpl;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.model.handover.*;
import com.erp.tms.aliexpress.model.handover.request.CommitRequest;
import com.erp.tms.aliexpress.model.handover.request.ServiceRequest;
import com.erp.tms.aliexpress.model.order.request.QueryOrderRequest;
import com.erp.tms.aliexpress.model.order.response.BaseResult;
import com.erp.tms.aliexpress.service.AliExpressHandoverService;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import lombok.extern.slf4j.Slf4j;
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
        String CLIENT_CODE = "502978";
        String CHECK_WORD = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
        String token = "50000200d30A5lnunrfByGwhJPhVvkBDBpfoTUjoDx176b9edfJWFw0FE4HHX5FiL4Vt";
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
//                .userNick("au2801869788uepae")
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
        IopResponse commit = aliExpressHandoverService.commit(authMap, commitRequest);
        System.out.println("结果输出");
        System.out.println(JSONObject.toJSONString(commit));
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
        IopResponse iopResponse = aliExpressHandoverService.queryService(authMap, serviceRequest);
        System.out.println("结果输出");
        System.out.println(JSONObject.toJSONString(iopResponse));
    }

    /**
     * 订单明细查询
     */
    @Test
    public void queryOrder() throws com.erp.tms.aliexpress.util.ApiException {
        QueryOrderRequest queryOrderRequest = QueryOrderRequest.builder()
                .trade_order_id("3030096741091976")
                .current_page(1)
                .page_size(20)
                .build();
        BaseResult baseResult = aliExpressShipperService.queryLogisticsOrder(authMap, queryOrderRequest);
        System.out.println("结果输出");
        System.out.println(JSONObject.toJSONString(baseResult));
    }
}
