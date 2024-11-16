package com.erp.server.tms.service.logistics;

import com.alibaba.fastjson.JSON;
import com.common.business.enums.LogisticsPlatformEnum;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.request.OrderRequest;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.server.tms.ErpServerTmsApplication;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.model.handover.*;
import com.erp.tms.aliexpress.model.handover.request.CommitRequest;
import com.erp.tms.aliexpress.service.AliExpressHandoverService;
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
        System.out.println(JSON.toJSONString(orderList));
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
                .userNick("cn123435sss")
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
                .returnInfo(addressInfo)
                .pickInfo(addressInfo)
                .orderCodeList(Collections.singletonList("3030096741091976"))
                .weight(new BigDecimal(1000.0000).setScale(0))
                .handoverOrderId("")
                .userInfo(userInfo)
                .weightUnit("g")
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
        System.out.println(commit);
    }
}
