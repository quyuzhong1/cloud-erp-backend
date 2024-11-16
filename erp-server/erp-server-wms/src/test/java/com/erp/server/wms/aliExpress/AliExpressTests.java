package com.erp.server.wms.aliExpress;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.tms.entity.TmsCarrierEntity;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.dto.request.DeclareDeliverRequest;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.FbaShipmentReceiveService;
import com.erp.server.wms.service.FbaShipmentService;
import com.erp.tms.aliexpress.model.order.response.AllCarrierResponse;
import com.erp.tms.aliexpress.model.query.request.QueryShipmentOrder;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class AliExpressTests {

    @Resource
    private FbaShipmentService fbaShipmentService;
    @Resource
    private FbaShipmentReceiveService fbaShipmentReceiveService;

    @Test
    public void querySellerShipmentInfo() {
        String orderId = "1106029852145370";
        try {
            String appKey = "503630";
            String appSecret = "PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ";
            String baseUrl = "https://api-sg.aliexpress.com";
            String token = "50000100620rOCpJesBimsVqwhdznwFL19448dbexeSxecBcvdotvGJILKkYWzw0qLEq";
            IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
            IopRequest request = new IopRequest();
            request.setApiName("aliexpress.logistics.querysellershipmentinfo");
            request.addApiParameter("trade_order_id", orderId);
            log.warn("【{}】速卖通查询声明发货信息:请求参数={}", orderId, JSONUtil.toJsonStr(request));
            IopResponse response = client.execute(request, token, Protocol.TOP);
            log.warn("【{}】速卖通查询声明发货信息:响应结果={}", orderId, JSONUtil.toJsonStr(response));
            String body = response.getBody();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            String jsonStr = JSONUtil.toJsonStr(jsonObject);
            System.out.println(jsonStr);
        } catch (Exception e) {
            throw new ServiceException("查询速卖通订单地址失败" + JSONUtil.toJsonStr(e));
        }

    }


    @Test
    public void shipOrder() {
        QueryShipmentOrder.Shipment shipment = QueryShipmentOrder.Shipment.builder()
                .logistics_no("CNG00660781498574")
                .service_name("CAINIAO_STANDARD")
                .build();

        QueryShipmentOrder.SubTradeOrder tradeOrder = QueryShipmentOrder.SubTradeOrder.builder()
                .send_type("all")
//                .sub_trade_order_index("LP00659545751639")
                .sub_trade_order_index("1")
                .shipment_list(Collections.singletonList(shipment))
                .build();

        QueryShipmentOrder declareDeliverRequest = QueryShipmentOrder.builder()
                .trade_order_id("1106029852145370")
                .sub_trade_order_list(Collections.singletonList(tradeOrder))
                .build();

//        DeclareDeliverRequest declareDeliverRequest = DeclareDeliverRequest.builder().
//                outRef("5388490211908536").
//                logisticsNo("CNG00659918457200").
//                shopId("1735117862084808706").
//                serviceName("AliExpress Standard Shipping").
//                sendType("all").
//                build();

//        标准-AliExpress Standard Shipping(菜鸟无忧物流-标准)

        try {
            String appKey = "503630";
            String appSecret = "PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ";
            String baseUrl = "https://api-sg.aliexpress.com";
            String apiName = AliexpressConstants.SUB_DECLARE_DELIVER;
            String token = "50000100620rOCpJesBimsVqwhdznwFL19448dbexeSxecBcvdotvGJILKkYWzw0qLEq";
            IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
            IopRequest request = new IopRequest();
            request.addApiParameter("param_aeop_seller_shipment_sub_trade_order_request", JSONUtil.toJsonStr(declareDeliverRequest));
            request.setApiName(apiName);
            log.warn("【{}】速卖通标记发货:请求参数={}", declareDeliverRequest.getTrade_order_id(), JSONUtil.toJsonStr(request));
            IopResponse response = client.execute(request, token, Protocol.TOP);
            log.warn("【{}】速卖通标记发货:响应结果={}", declareDeliverRequest.getTrade_order_id(), JSONUtil.toJsonStr(response));
            String body = response.getBody();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            Boolean success = jsonObject.getBool("result_success", Boolean.FALSE);
            if (!success) {
                String msg = jsonObject.getOrDefault("result_error_desc", "").toString();
                throw new ServiceException(ApiError.Default, msg);
            }
        } catch (Exception e) {
            throw new ServiceException("查询速卖通订单地址失败" + JSONUtil.toJsonStr(e));
        }
    }

    @Test
    public void testService() throws Exception {
        String appKey = "502978";
        String appSecret = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
        String url = "https://api-sg.aliexpress.com";
        com.erp.tms.aliexpress.api.IopClient client = new com.erp.tms.aliexpress.api.IopClientImpl(url, appKey, appSecret);
        com.erp.tms.aliexpress.api.IopRequest request = new com.erp.tms.aliexpress.api.IopRequest();
        request.setApiName("aliexpress.logistics.redefining.supercalifragilistic");
        request.addApiParameter("simplify", "true");
        com.erp.tms.aliexpress.api.IopResponse response = client.execute(request, "50000200123dJAvRobgSKEtBJjvZtxEAZfV17b52f96gJQg0OG9CCvBqT1l8Mocp35cG", com.erp.tms.aliexpress.domain.Protocol.TOP);
        System.out.println("响应结果");
        System.out.println(JSONUtil.toJsonStr(response));
    }


    @Test
    public void testExpress() throws Exception {
        SoB2cEntity mainEntity = new SoB2cEntity();
        mainEntity.setCode("1106059230784298");
        mainEntity.setPlatformCode("1106059230784298");
        List<String> subTradeOrderList = Collections.singletonList("");

        DeclareDeliverRequest declareDeliverRequest = DeclareDeliverRequest.builder()
                .outRef(mainEntity.getPlatformCode())
                .logisticsNo("CNG00661843204255")
                .shopId(mainEntity.getShopId()).shopName(mainEntity.getShopName())
                .serviceName("CAINIAO_STANDARD")
                .sendType("all")
                .subTradeOrderIndexList(subTradeOrderList)
                .build();
        String appKey = "503630";
        String appSecret = "PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ";
        String baseUrl = "https://api-sg.aliexpress.com";
        String apiName = AliexpressConstants.SUB_DECLARE_DELIVER;
        String token = "50000700423zHPZZqMly9iku4MQdb7h0hqR18ff9902ExugZffT7nzxEiFwFyWHdZKNC";
        try {
            // 组合请求参数
            QueryShipmentOrder.Shipment shipment = QueryShipmentOrder.Shipment.builder()
                    .logistics_no(declareDeliverRequest.getLogisticsNo())
                    .service_name(declareDeliverRequest.getServiceName())
                    .build();
            if (CharSequenceUtil.isNotBlank(declareDeliverRequest.getActualCarrier())){
                shipment.setActual_carrier(declareDeliverRequest.getActualCarrier());
            }
            if (CharSequenceUtil.isNotBlank(declareDeliverRequest.getTrackingWebSite())){
                shipment.setTracking_web_site(declareDeliverRequest.getTrackingWebSite());
            }
            List<QueryShipmentOrder.SubTradeOrder> subTradeOrders = new LinkedList<>();
            for (String subOrder : subTradeOrderList) {
                QueryShipmentOrder.SubTradeOrder tradeOrder = QueryShipmentOrder.SubTradeOrder.builder()
                        .send_type(declareDeliverRequest.getSendType())
                        .sub_trade_order_index(subOrder)
                        .shipment_list(Collections.singletonList(shipment))
                        .build();
                subTradeOrders.add(tradeOrder);
            }

            QueryShipmentOrder requestParams = QueryShipmentOrder.builder()
                    .trade_order_id(declareDeliverRequest.getOutRef())
                    .sub_trade_order_list(subTradeOrders)
                    .build();

            IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);

            IopRequest request = new IopRequest();
            request.setApiName(apiName);
            request.addApiParameter("param_aeop_seller_shipment_sub_trade_order_request", JSONUtil.toJsonStr(requestParams));
            log.warn("【{}】速卖通子声明标记发货:请求参数={}", declareDeliverRequest.getOutRef(), JSONUtil.toJsonStr(request));
            IopResponse response = client.execute(request, token, Protocol.TOP);
            log.warn("【{}】速卖通子声明标记发货:响应结果={}", declareDeliverRequest.getOutRef(), JSONUtil.toJsonStr(response));
            String body = response.getBody();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            JSONObject resultJsONObject = jsonObject.getJSONObject("aliexpress_logistics_order_shipment_response");
            JSONObject resultJson = JSONUtil.parseObj(resultJsONObject.get("result"));
            boolean success = resultJson.getBool("success", Boolean.FALSE);
            if (!success){
                String errorMsg = resultJson.getStr("error_msg", "");
                Integer errorCode = resultJson.getInt("error_code", -1000000);
                if (CharSequenceUtil.isNotBlank(errorMsg)){
                    ServiceException.runError(errorCode, errorMsg);
                } else {
                    ServiceException.runError(errorCode, JSONUtil.toJsonStr(body));
                }
            }
        } catch (ServiceException e) {
            if (-353 == e.getCode()) {
                log.warn("【速卖通标记发货】销售订单【{}】,平台订单【{}】速卖通标记发货API提示重复操作(忽略) >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
                return;
            }
            log.error("【速卖通标记发货】销售订单【{}】,平台订单【{}】速卖通标记发货API提示异常 >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
            throw new ServiceException("速卖通API标记发货失败:" + e.getMessage());
        } catch (Exception e) {
            log.error("【速卖通标记发货】销售订单【{}】,平台订单【{}】速卖通标记发货失败 >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
            throw new ServiceException("速卖通标记发货失败:" + e.getMessage());
        }
    }


    @Test
    public void testExpressCarrier() {
        String appKey = "503630";
        String appSecret = "PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ";
        String baseUrl = "https://api-sg.aliexpress.com";
        String apiName = AliexpressConstants.ALIEXPRESS_CARRIER_QUERY_LIST;
        String token = "50000101714jiEhoattd9gAtAs1b36f6d2wukiGxo0EzuFCyEOscLSZUnQkqkvOeUsJk";

        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(apiName);
        request.addApiParameter("locale", "zh_CN");
        log.warn("速卖通实际承运商:请求参数={}", JSONUtil.toJsonStr(request));
        IopResponse response = null;
        try {
            response = client.execute(request, token, Protocol.TOP);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        System.out.println(JSONUtil.toJsonStr(response.getBody()));
    }

    @Test
    public void testExpressJson() {
        DeclareDeliverRequest declareDeliverRequest = DeclareDeliverRequest.builder().
                outRef("1111").
                logisticsNo("4PX3001190473862CN").
                shopId("123456").
                shopName("123").
                serviceName("Other").
                sendType("all").
//                .trackingWebSite("https://www.track123.com")
//                .actualCarrier("3011").
        build();

        // 组合请求参数
        QueryShipmentOrder.Shipment shipment = QueryShipmentOrder.Shipment.builder()
                .logistics_no(declareDeliverRequest.getLogisticsNo())
                .service_name(declareDeliverRequest.getServiceName())
                .build();
        if (CharSequenceUtil.isNotBlank(declareDeliverRequest.getActualCarrier())) {
            shipment.setActual_carrier(declareDeliverRequest.getActualCarrier());
        }
        if (CharSequenceUtil.isNotBlank(declareDeliverRequest.getTrackingWebSite())) {
            shipment.setTracking_web_site(declareDeliverRequest.getTrackingWebSite());
        }
        QueryShipmentOrder.SubTradeOrder tradeOrder = QueryShipmentOrder.SubTradeOrder.builder()
                .send_type(declareDeliverRequest.getSendType())
                .sub_trade_order_index("1")
                .shipment_list(Collections.singletonList(shipment))
                .build();
        QueryShipmentOrder requestParams = QueryShipmentOrder.builder()
                .trade_order_id(declareDeliverRequest.getOutRef())
                .sub_trade_order_list(Collections.singletonList(tradeOrder))
                .build();
        System.out.println("json结果");
        System.out.println(JSONUtil.toJsonStr(requestParams));
    }
}
