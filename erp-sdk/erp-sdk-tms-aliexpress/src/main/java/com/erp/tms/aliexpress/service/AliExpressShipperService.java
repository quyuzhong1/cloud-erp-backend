package com.erp.tms.aliexpress.service;

import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.tms.aliexpress.api.IopClient;
import com.erp.tms.aliexpress.api.IopClientImpl;
import com.erp.tms.aliexpress.api.IopRequest;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.constants.PathConstants;
import com.erp.tms.aliexpress.domain.Protocol;
import com.erp.tms.aliexpress.model.channel.response.ChannelResult;
import com.erp.tms.aliexpress.model.label.request.LabelRequest;
import com.erp.tms.aliexpress.model.order.request.OrderRequest;
import com.erp.tms.aliexpress.model.order.request.QueryOrderRequest;
import com.erp.tms.aliexpress.model.order.response.BaseResult;
import com.erp.tms.aliexpress.model.order.response.OrderResult;
import com.erp.tms.aliexpress.model.query.request.QueryLogisticsRequest;
import com.erp.tms.aliexpress.util.ApiException;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zdy
 * @ClassName AliExpressShipperService
 * @description: TODO
 * @date 2023年11月17日
 * @version: 1.0
 */
@Slf4j
@Component
public class AliExpressShipperService {
    private void validate(String appKey,String appSecret,String token,String url){
        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(appSecret) || StringUtils.isBlank(token) || StringUtils.isBlank(token) ) throw new ServiceException("授权信息不能为空");
    }
    public ChannelResult getChanelList(Map<String, String> authMap) throws ApiException, InterruptedException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.redefining.listlogisticsservice");
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        return JSONObject.parseObject(response.getBody(), ChannelResult.class);
    }

    public OrderResult createOrder(Map<String, String> authMap, OrderRequest orderRequest) throws ApiException, InterruptedException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.order.createorder");
        request.addApiParameter("is_agree_upgrade_reverse_parcel_insure", String.valueOf(orderRequest.getIs_agree_upgrade_reverse_parcel_insure()));
        request.addApiParameter("oaid", orderRequest.getOaid());
        request.addApiParameter("pickup_type", orderRequest.getPickup_type());
        request.addApiParameter("address_d_t_os", JSONObject.toJSONString(orderRequest.getAddress_d_t_os()));
        request.addApiParameter("declare_product_d_t_os", JSONObject.toJSONString(orderRequest.getDeclareProducts()));
        request.addApiParameter("domestic_logistics_company", orderRequest.getDomestic_logistics_company());
        request.addApiParameter("domestic_logistics_company_id", String.valueOf(orderRequest.getDomestic_logistics_company_id()));
        request.addApiParameter("domestic_tracking_no", orderRequest.getDomestic_tracking_no());
        request.addApiParameter("package_num", String.valueOf(orderRequest.getPackage_num()));
        request.addApiParameter("trade_order_from", orderRequest.getTrade_order_from());
        request.addApiParameter("trade_order_id", orderRequest.getTrade_order_id());
        request.addApiParameter("undeliverable_decision", orderRequest.getUndeliverable_decision());
        request.addApiParameter("warehouse_carrier_service", orderRequest.getWarehouse_carrier_service());
        request.addApiParameter("invoice_number", orderRequest.getInvoice_number());
        request.addApiParameter("top_user_key", orderRequest.getTop_user_key());
//        request.addApiParameter("insurance_coverage", JSONObject.toJSONString(orderRequest.getInsuranceCoverage()));
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        log.info("下单完成：{}",JSONObject.toJSONString(response));
        return JSONObject.parseObject(response.getBody(), OrderResult.class);
    }
    public OrderResult createWareHouseOrder(Map<String, String> authMap, OrderRequest orderRequest) throws ApiException, InterruptedException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.createwarehouseorder");
        request.addApiParameter("declare_product_d_t_os", JSONObject.toJSONString(orderRequest.getDeclareProducts()));
        request.addApiParameter("domestic_logistics_company", orderRequest.getDomestic_logistics_company());
        request.addApiParameter("domestic_logistics_company_id", String.valueOf(orderRequest.getDomestic_logistics_company_id()));
        request.addApiParameter("domestic_tracking_no", orderRequest.getDomestic_tracking_no());
        request.addApiParameter("package_num", String.valueOf(orderRequest.getPackage_num()));
        request.addApiParameter("trade_order_from", orderRequest.getTrade_order_from());
        request.addApiParameter("trade_order_id", orderRequest.getTrade_order_id());
        request.addApiParameter("undeliverable_decision", orderRequest.getUndeliverable_decision());
        request.addApiParameter("warehouse_carrier_service", orderRequest.getWarehouse_carrier_service());
        request.addApiParameter("address_d_t_os", JSONObject.toJSONString(orderRequest.getAddress_d_t_os()));
        request.addApiParameter("top_user_key", orderRequest.getTop_user_key());
        request.addApiParameter("insurance_coverage", JSONObject.toJSONString(orderRequest.getInsuranceCoverage()));
        request.addApiParameter("is_agree_upgrade_reverse_parcel_insure", String.valueOf(orderRequest.getIs_agree_upgrade_reverse_parcel_insure()));
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        return JSONObject.parseObject(response.getBody(), OrderResult.class);
    }

    public IopResponse logisticsCompany(Map<String, String> authMap) throws ApiException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.redefining.qureywlbdomesticlogisticscompany");
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        return response;
    }

    public IopResponse getLabelList(Map<String, String> authMap, LabelRequest labelRequest) throws ApiException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.redefining.getprintinfos");
        request.addApiParameter("simplify", "true");
        request.addApiParameter("print_detail", String.valueOf(labelRequest.getPrint_detail()));
        request.addApiParameter("warehouse_order_query_d_t_os", JSONObject.toJSONString(labelRequest.getWarehouseOrderQueries()));
        IopResponse response = client.execute(request, token, Protocol.TOP);
        return response;
//        return JSONObject.parseObject(response.getBody(), LabelResult.class);
    }

    /**
     * 查询物流单明细
     * @param authMap
     * @param queryOrderRequest
     * @return
     * @throws ApiException
     */
    public BaseResult queryLogisticsOrder(Map<String, String> authMap, QueryOrderRequest queryOrderRequest) throws ApiException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.querylogisticsorderdetail");
        request.addApiParameter("current_page", String.valueOf(queryOrderRequest.getCurrent_page()));
        request.addApiParameter("domestic_logistics_num", queryOrderRequest.getDomestic_logistics_num());
        request.addApiParameter("gmt_create_end_str", queryOrderRequest.getGmt_create_end_str());
        request.addApiParameter("gmt_create_start_str", queryOrderRequest.getGmt_create_start_str());
        request.addApiParameter("international_logistics_num", queryOrderRequest.getInternational_logistics_num());
        request.addApiParameter("logistics_status", queryOrderRequest.getLogistics_status());
        request.addApiParameter("page_size", String.valueOf(queryOrderRequest.getPage_size()));
        request.addApiParameter("trade_order_id", queryOrderRequest.getTrade_order_id());
        request.addApiParameter("warehouse_carrier_service", queryOrderRequest.getWarehouse_carrier_service());
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        return JSONObject.parseObject(response.getBody(), BaseResult.class);
    }

    public IopResponse getLogisticsAddress(Map<String, String> authMap) throws ApiException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.redefining.getlogisticsselleraddresses");
        request.addApiParameter("seller_address_query", "sender,pickup,refund");
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());
        return response;
    }

    public IopResponse getLogisticsService(Map<String, String> authMap, QueryLogisticsRequest queryLogisticsRequest) throws ApiException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.service.query");
        request.addApiParameter("interface_request", JSONObject.toJSONString(queryLogisticsRequest));
        request.addApiParameter("simplify", "true");
//        request.addApiParameter("interface_request", "{\"goods_length\":\"1\",\"goods_height\":\"1\",\"goods_width\":\"1\",\"sub_order_list\":[{\"goods_length\":\"1\",\"goods_height\":\"1\",\"goods_width\":\"1\",\"locale\":\"zh_CN\",\"order_id\":\"8001498863155804\",\"goods_weight\":\"0.1\"},{\"goods_length\":\"1\",\"goods_height\":\"1\",\"goods_width\":\"1\",\"locale\":\"zh_CN\",\"order_id\":\"8001498863155804\",\"goods_weight\":\"0.1\"}],\"locale\":\"zh_CN\",\"order_id\":\"8001498863145804\",\"goods_weight\":\"0.1\"}");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());
        return response;
    }

    public IopResponse getSellerInfo(Map<String, String> authMap) throws ApiException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.merchant.profile.get");
        request.addApiParameter("simplify", "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());
        return response;
    }

    public static void main(String[] args) throws ApiException {
        AliExpressShipperService service = new AliExpressShipperService();
        Map<String, String> authMap = new HashMap<>();
        String CLIENT_CODE = "503630";
        String CHECK_WORD = "PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ";
        String token = "50000201326tOAyeueoufxwfeiat5mxXHHeLgx183c2ca0etyIE3GSShKP4IUHrpU3LN";
        authMap.put("clientId",CLIENT_CODE);
        authMap.put("clientSecret",CHECK_WORD);
        authMap.put("token",token);
        authMap.put("url","https://api-sg.aliexpress.com");
//        IopResponse logisticsAddress = service.getLogisticsAddress(authMap);
//        System.out.println(logisticsAddress);
        QueryLogisticsRequest queryLogisticsRequest =  QueryLogisticsRequest.builder()
                .order_id(1102446978507349L)
                .goods_weight("1")
                .goods_height(1L)
                .goods_width(1L)
                .goods_length(1L)
//                .order_id(1102175972276889L)
                .build();
        QueryLogisticsRequest queryLogisticsRequest1 =  QueryLogisticsRequest.builder()
                .order_id(8182864532297635L)
                .goods_weight("1")
                .goods_height(1L)
                .goods_width(1L)
                .goods_length(1L)
                .sub_order_list(Collections.singletonList(queryLogisticsRequest))
                .build();
        IopResponse logisticsService = service.getLogisticsService(authMap, queryLogisticsRequest1);
        System.out.println(logisticsService);
    }
}
