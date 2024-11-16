package com.erp.tms.aliexpress.service;

import com.alibaba.fastjson.JSON;
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

import java.util.Map;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName AliExpressShipperService
 * @date 2023年11月17日
 * @version: 1.0
 */
@Slf4j
@Component
public class AliExpressShipperService {
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String TOKEN = "token";
    private static final String URL = "url";
    private static final String SIMPLIFY = "simplify";
    private static final String TRADE_ORDER_ID = "trade_order_id";
    private static final String WAREHOUSE_CARRIER_SERVICE = "warehouse_carrier_service";
    
    private void validate(String appKey,String appSecret,String token){
        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(appSecret) || StringUtils.isBlank(token) ) throw new ServiceException("授权信息不能为空");
    }
    public ChannelResult getChanelList(Map<String, String> authMap) throws ApiException {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.redefining.listlogisticsservice");
        request.addApiParameter(SIMPLIFY, "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        return JSON.parseObject(response.getBody(), ChannelResult.class);
    }

    public OrderResult createOrder(Map<String, String> authMap, OrderRequest orderRequest) throws ApiException, InterruptedException {
        log.info("==========AliExpressShipperService.createOrder==========start");
        log.info("authMap:{}, orderRequest:{}",authMap, orderRequest);
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.order.createorder");
        if (Objects.nonNull(orderRequest.getIs_agree_upgrade_reverse_parcel_insure())){
            request.addApiParameter("is_agree_upgrade_reverse_parcel_insure", String.valueOf(orderRequest.getIs_agree_upgrade_reverse_parcel_insure()));
        }
        if (StringUtils.isNotEmpty(orderRequest.getOaid())){
            request.addApiParameter("oaid", orderRequest.getOaid());
        }
        if(StringUtils.isNotEmpty(orderRequest.getPickup_type())){
            request.addApiParameter("pickup_type", orderRequest.getPickup_type());
        }
        request.addApiParameter("address_d_t_os", JSON.toJSONString(orderRequest.getAddress_d_t_os()));
        request.addApiParameter("declare_product_d_t_os", JSON.toJSONString(orderRequest.getDeclareProducts()));
        if (StringUtils.isNotEmpty(orderRequest.getDomestic_logistics_company())){
            request.addApiParameter("domestic_logistics_company", orderRequest.getDomestic_logistics_company());
        }
        if (Objects.nonNull(orderRequest.getDomestic_logistics_company_id())){
            request.addApiParameter("domestic_logistics_company_id", String.valueOf(orderRequest.getDomestic_logistics_company_id()));
        }
        if (StringUtils.isNotEmpty(orderRequest.getDomestic_tracking_no())){
            request.addApiParameter("domestic_tracking_no", orderRequest.getDomestic_tracking_no());
        }
        if (Objects.nonNull(orderRequest.getPackage_num())){
            request.addApiParameter("package_num", String.valueOf(orderRequest.getPackage_num()));
        }
        if (StringUtils.isNotEmpty(orderRequest.getTrade_order_from())){
            request.addApiParameter("trade_order_from", orderRequest.getTrade_order_from());
        }
        if (StringUtils.isNotEmpty(orderRequest.getTrade_order_id())){
            request.addApiParameter(TRADE_ORDER_ID, orderRequest.getTrade_order_id());
        }
        if (StringUtils.isNotEmpty(orderRequest.getUndeliverable_decision())){
            request.addApiParameter("undeliverable_decision", orderRequest.getUndeliverable_decision());
        }
        if (StringUtils.isNotEmpty(orderRequest.getWarehouse_carrier_service())){
            request.addApiParameter(WAREHOUSE_CARRIER_SERVICE, orderRequest.getWarehouse_carrier_service());
        }
        if (StringUtils.isNotEmpty(orderRequest.getInvoice_number())){
            request.addApiParameter("invoice_number", orderRequest.getInvoice_number());
        }
        if (StringUtils.isNotEmpty(orderRequest.getTop_user_key())){
            request.addApiParameter("top_user_key", orderRequest.getTop_user_key());
        }
        if (Objects.nonNull(orderRequest.getInsuranceCoverage())){
            request.addApiParameter("insurance_coverage", JSON.toJSONString(orderRequest.getInsuranceCoverage()));
        }
        request.addApiParameter(SIMPLIFY, "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        log.info("下单完成：{}", JSON.toJSONString(response));
        return JSON.parseObject(response.getBody(), OrderResult.class);
    }
    public OrderResult createWareHouseOrder(Map<String, String> authMap, OrderRequest orderRequest) throws ApiException {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.createwarehouseorder");
        request.addApiParameter("declare_product_d_t_os", JSON.toJSONString(orderRequest.getDeclareProducts()));
        request.addApiParameter("domestic_logistics_company", orderRequest.getDomestic_logistics_company());
        request.addApiParameter("domestic_logistics_company_id", String.valueOf(orderRequest.getDomestic_logistics_company_id()));
        request.addApiParameter("domestic_tracking_no", orderRequest.getDomestic_tracking_no());
        request.addApiParameter("package_num", String.valueOf(orderRequest.getPackage_num()));
        request.addApiParameter("trade_order_from", orderRequest.getTrade_order_from());
        request.addApiParameter(TRADE_ORDER_ID, orderRequest.getTrade_order_id());
        request.addApiParameter("undeliverable_decision", orderRequest.getUndeliverable_decision());
        request.addApiParameter(WAREHOUSE_CARRIER_SERVICE, orderRequest.getWarehouse_carrier_service());
        request.addApiParameter("address_d_t_os", JSON.toJSONString(orderRequest.getAddress_d_t_os()));
        request.addApiParameter("top_user_key", orderRequest.getTop_user_key());
        request.addApiParameter("insurance_coverage", JSON.toJSONString(orderRequest.getInsuranceCoverage()));
        request.addApiParameter("is_agree_upgrade_reverse_parcel_insure", String.valueOf(orderRequest.getIs_agree_upgrade_reverse_parcel_insure()));
        request.addApiParameter(SIMPLIFY, "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        return JSON.parseObject(response.getBody(), OrderResult.class);
    }

    public IopResponse logisticsCompany(Map<String, String> authMap) throws ApiException {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.redefining.qureywlbdomesticlogisticscompany");
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
    }

    public IopResponse getLabelList(Map<String, String> authMap, LabelRequest labelRequest) throws ApiException {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.redefining.getprintinfos");
        request.addApiParameter(SIMPLIFY, "true");
        request.addApiParameter("print_detail", String.valueOf(labelRequest.getPrint_detail()));
        request.addApiParameter("warehouse_order_query_d_t_os", JSON.toJSONString(labelRequest.getWarehouseOrderQueries()));
        return client.execute(request, token, Protocol.TOP);
    }

    /**
     * 查询物流单明细
     * @param authMap
     * @param queryOrderRequest
     * @return
     * @throws ApiException
     */
    public BaseResult queryLogisticsOrder(Map<String, String> authMap, QueryOrderRequest queryOrderRequest) throws ApiException {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.querylogisticsorderdetail");
        request.addApiParameter("current_page", String.valueOf(queryOrderRequest.getCurrent_page()));
        if (StringUtils.isNotBlank(queryOrderRequest.getDomestic_logistics_num())){
            request.addApiParameter("domestic_logistics_num", queryOrderRequest.getDomestic_logistics_num());
        }
        if (StringUtils.isNotBlank(queryOrderRequest.getGmt_create_end_str())){
            request.addApiParameter("gmt_create_end_str", queryOrderRequest.getGmt_create_end_str());
        }
        if (StringUtils.isNotBlank(queryOrderRequest.getGmt_create_start_str())){
            request.addApiParameter("gmt_create_start_str", queryOrderRequest.getGmt_create_start_str());
        }
        if (StringUtils.isNotBlank(queryOrderRequest.getInternational_logistics_num())){
            request.addApiParameter("international_logistics_num", queryOrderRequest.getInternational_logistics_num());
        }
        if (StringUtils.isNotBlank(queryOrderRequest.getLogistics_status())){
            request.addApiParameter("logistics_status", queryOrderRequest.getLogistics_status());
        }
        if (Objects.nonNull(queryOrderRequest.getPage_size())){
            request.addApiParameter("page_size", String.valueOf(queryOrderRequest.getPage_size()));
        }
        request.addApiParameter(TRADE_ORDER_ID, queryOrderRequest.getTrade_order_id());
        if (Objects.nonNull(queryOrderRequest.getPage_size())){
            request.addApiParameter(WAREHOUSE_CARRIER_SERVICE, queryOrderRequest.getWarehouse_carrier_service());
        }
        request.addApiParameter(SIMPLIFY, "true");
        IopResponse response = client.execute(request, token, Protocol.TOP);
        return JSON.parseObject(response.getBody(), BaseResult.class);
    }

    public IopResponse getLogisticsAddress(Map<String, String> authMap) throws ApiException {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.redefining.getlogisticsselleraddresses");
        request.addApiParameter("seller_address_query", "sender,pickup,refund");
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
    }

    public IopResponse getLogisticsService(Map<String, String> authMap, QueryLogisticsRequest queryLogisticsRequest) throws ApiException {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.service.query");
        request.addApiParameter("interface_request", JSON.toJSONString(queryLogisticsRequest));
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
    }

    public IopResponse getSellerInfo(Map<String, String> authMap) throws ApiException {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)){
            url = PathConstants.BASE_URL;
        }
        validate(appKey,appSecret,token);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.merchant.profile.get");
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
    }
    /**
     * @description 获取服务列表
     * @param
     * @return
     * @date 2024-03-04 15:27
     * @author Lambda
     */
    public IopResponse listLogisticsService(Map<String, String> authMap) throws ApiException {
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String token = authMap.get(TOKEN);
        String url = authMap.get(URL);
        if (StringUtils.isBlank(url)) {
            url = PathConstants.BASE_URL;
        }
        validate(appKey, appSecret, token);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.redefining.listlogisticsservice");
        request.addApiParameter(SIMPLIFY, "true");
        return client.execute(request, token, Protocol.TOP);
    }
}
