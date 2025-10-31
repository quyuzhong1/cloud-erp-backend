package com.erp.wms.aliexpress.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.erp.wms.aliexpress.api.IopClient;
import com.erp.wms.aliexpress.api.IopClientImpl;
import com.erp.wms.aliexpress.api.IopRequest;
import com.erp.wms.aliexpress.api.IopResponse;
import com.erp.wms.aliexpress.domain.Protocol;
import com.erp.wms.aliexpress.model.AliexpressAuthDTO;
import com.erp.wms.aliexpress.model.inbound.AliexpressInboundDTO;
import com.erp.wms.aliexpress.model.inbound.ApiInboundResponseDTO;
import com.erp.wms.aliexpress.model.inventory.ApiInventoryResponseDTO;
import com.erp.wms.aliexpress.model.order.AliexpressCancelOrderDTO;
import com.erp.wms.aliexpress.model.order.AliexpressOrderDTO;
import com.erp.wms.aliexpress.model.order.ApiOrderResponseDTO;
import com.erp.wms.aliexpress.model.ApiResponseDTO;
import com.erp.wms.aliexpress.model.product.AliexpressProductDTO;
import com.erp.wms.aliexpress.model.returnorder.AliexpressReturnInstockDTO;
import com.erp.wms.aliexpress.model.returnorder.ApiReturnOrderResponseDTO;
import com.erp.wms.aliexpress.util.ApiException;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AliexpressWarehouseService {

    private static String CLIENT_ID = "clientId";
    private static String CLIENT_SECRET = "clientSecret";

//    public static void main(String[] args) throws ApiException, JsonProcessingException {
//        String url = "https://api-sg.aliexpress.com/sandbox";
//        String appkey = "503630";
//        String appSecret = "PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ";
//        String accessToken = "50000701530cnHtbirhzrd7ijPeou2emiSIajwwCg118389611msriNQyROLyoVZ1y1j";
//        IopClient client = new IopClientImpl(url, appkey, appSecret);
//        IopRequest request = new IopRequest();
//        request.setApiName("cainiao.cnap.singleitem.synchronize");
//        request.addApiParameter("action_type", "add");
//        request.addApiParameter("simplify", "true");
//        AliexpressProductDTO.ProductDTO aliexpressProductDTO = new AliexpressProductDTO.ProductDTO();
//        aliexpressProductDTO.setItemCode("2028");
//        aliexpressProductDTO.setItemName("YN50MM F1.8 C");
//        aliexpressProductDTO.setBarCode("2028");
//        aliexpressProductDTO.setItemType("ZC");
//        aliexpressProductDTO.setHeight(new BigDecimal("1.1"));
//        aliexpressProductDTO.setWidth(new BigDecimal("3.3"));
//        aliexpressProductDTO.setLength(new BigDecimal("2.2"));
//        aliexpressProductDTO.setNetWeight(new BigDecimal("1.11"));
//        aliexpressProductDTO.setGrossWeight(new BigDecimal("0.1"));
//        request.addApiParameter("item", JSONUtil.toJsonStr(aliexpressProductDTO));
//        request.addApiParameter("owner_code", "17379911544");
//        request.addApiParameter("warehouse_code", "STB");
//        IopResponse response = client.execute(request, accessToken, Protocol.TOP);
//        String result = response.getBody();
//        System.out.println(response.getBody());
//        ApiResponseDTO apiResponseDTO = JSON.parseObject(result,new TypeReference<ApiResponseDTO>() {}.getType());
//        System.out.println(123);
//    }

    public ApiResponseDTO pushListing(AliexpressProductDTO aliexpressProductDTO) throws ApiException {
        AliexpressAuthDTO aliexpressAuthDTO = aliexpressProductDTO.getAliexpressAuthDTO();
        String url = aliexpressAuthDTO.getUrl();
        if (!BusinessCommonConstants.hasProfile("prod")) {
//            url = url + "/sandbox";
            aliexpressProductDTO.setWarehouseCode("STB");
        }else{
            aliexpressProductDTO.setWarehouseCode("other");
        }
        log.warn("菜鸟仓推送货品,{}",JSONUtil.toJsonStr(aliexpressProductDTO));
        String appkey = aliexpressAuthDTO.getAppKey();
        String appSecret = aliexpressAuthDTO.getAppSecret();
        String accessToken = aliexpressAuthDTO.getAccessToken();
        String actionType = aliexpressProductDTO.getActionType();
        IopClient client = new IopClientImpl(url, appkey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.cnap.singleitem.synchronize");
        request.addApiParameter("action_type", actionType);
        request.addApiParameter("simplify", "true");
        AliexpressProductDTO.ProductDTO productDTO = aliexpressProductDTO.getProductDTO();
        request.addApiParameter("item", JSONUtil.toJsonStr(productDTO));
        request.addApiParameter("owner_code", aliexpressAuthDTO.getOwnerCode());
        request.addApiParameter("warehouse_code", aliexpressProductDTO.getWarehouseCode());
        IopResponse response = client.execute(request, accessToken, Protocol.TOP);
        return JSON.parseObject(response.getBody(),new TypeReference<ApiResponseDTO>() {}.getType());
    }

    public ApiOrderResponseDTO createOutbound(AliexpressOrderDTO aliexpressOrderDTO) throws ApiException {
        AliexpressAuthDTO aliexpressAuthDTO = aliexpressOrderDTO.getAliexpressAuthDTO();
        String url = aliexpressAuthDTO.getUrl();
//        if (!BusinessCommonConstants.hasProfile("prod")) {
//            url = url + "/sandbox";
//        }
        log.warn("菜鸟仓创建出库单,{}",JSONUtil.toJsonStr(aliexpressOrderDTO));
        String appKey = aliexpressAuthDTO.getAppKey();
        String appSecret = aliexpressAuthDTO.getAppSecret();
        String accessToken = aliexpressAuthDTO.getAccessToken();
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.cnap.deliveryorder.create");
        request.addApiParameter("simplify", "true");
        request.addApiParameter("order_lines", JSONUtil.toJsonStr(aliexpressOrderDTO.getOrderLines()));
        request.addApiParameter("extend_props", JSONUtil.toJsonStr(aliexpressOrderDTO.getExtendProps()));
        request.addApiParameter("delivery_order", JSONUtil.toJsonStr(aliexpressOrderDTO.getDeliveryOrder()));
        IopResponse response = client.execute(request, accessToken, Protocol.TOP);
        log.warn("菜鸟仓出库单回参{}",JSONUtil.toJsonStr(response.getBody()));
        return JSON.parseObject(response.getBody(),new TypeReference<ApiOrderResponseDTO>() {}.getType());
    }

    public ApiOrderResponseDTO cancelOrder(AliexpressCancelOrderDTO aliexpressCancelOrderDTO) throws ApiException {
        AliexpressAuthDTO aliexpressAuthDTO = aliexpressCancelOrderDTO.getAliexpressAuthDTO();
        String url = aliexpressAuthDTO.getUrl();
//        if (!BusinessCommonConstants.hasProfile("prod")) {
//            url = url + "/sandbox";
//        }
        log.warn("菜鸟仓取消订单,{}",JSONUtil.toJsonStr(aliexpressCancelOrderDTO));
        String appKey = aliexpressAuthDTO.getAppKey();
        String appSecret = aliexpressAuthDTO.getAppSecret();
        String accessToken = aliexpressAuthDTO.getAccessToken();
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.cnap.order.cancel");
        request.addApiParameter("simplify", "true");
        request.addApiParameter("order_type", aliexpressCancelOrderDTO.getOrderType());
        request.addApiParameter("owner_code", aliexpressCancelOrderDTO.getOwnerCode());
        request.addApiParameter("order_id", aliexpressCancelOrderDTO.getOrderId());
        request.addApiParameter("order_code", aliexpressCancelOrderDTO.getOrderCode());
        request.addApiParameter("warehouse_code", aliexpressCancelOrderDTO.getWarehouseCode());
        IopResponse response = client.execute(request, accessToken, Protocol.TOP);
        log.warn("菜鸟仓取消订单{}",JSONUtil.toJsonStr(response.getBody()));
        return JSON.parseObject(response.getBody(),new TypeReference<ApiOrderResponseDTO>() {}.getType());
    }

    public ApiInboundResponseDTO createInbound(AliexpressInboundDTO aliexpressInboundDTO) throws ApiException {
        AliexpressAuthDTO aliexpressAuthDTO = aliexpressInboundDTO.getAliexpressAuthDTO();
        String url = aliexpressAuthDTO.getUrl();
//        if (!BusinessCommonConstants.hasProfile("prod")) {
//            url = url + "/sandbox";
//        }
        log.warn("菜鸟仓创建入库单,{}",JSONUtil.toJsonStr(aliexpressInboundDTO));
        String appKey = aliexpressAuthDTO.getAppKey();
        String appSecret = aliexpressAuthDTO.getAppSecret();
        String accessToken = aliexpressAuthDTO.getAccessToken();
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.cnap.entryorder.create");
        request.addApiParameter("simplify", "true");
        request.addApiParameter("order_lines", JSONUtil.toJsonStr(aliexpressInboundDTO.getOrderLines()));
        request.addApiParameter("extend_props", JSONUtil.toJsonStr(aliexpressInboundDTO.getExtendProps()));
        request.addApiParameter("entry_order", JSONUtil.toJsonStr(aliexpressInboundDTO.getEntryOrder()));
        IopResponse response = client.execute(request, accessToken, Protocol.TOP);
        log.warn("菜鸟仓入库单回参{}",JSONUtil.toJsonStr(response.getBody()));
        return JSON.parseObject(response.getBody(),new TypeReference<ApiInboundResponseDTO>() {}.getType());
    }

    public ApiReturnOrderResponseDTO createReturnInstockOrder(AliexpressReturnInstockDTO aliexpressReturnInstockDTO) throws ApiException {
        AliexpressAuthDTO aliexpressAuthDTO = aliexpressReturnInstockDTO.getAliexpressAuthDTO();
        String url = aliexpressAuthDTO.getUrl();
//        if (!BusinessCommonConstants.hasProfile("prod")) {
//            url = url + "/sandbox";
//        }
        log.warn("菜鸟仓创建退货入库单,{}",JSONUtil.toJsonStr(aliexpressReturnInstockDTO));
        String appKey = aliexpressAuthDTO.getAppKey();
        String appSecret = aliexpressAuthDTO.getAppSecret();
        String accessToken = aliexpressAuthDTO.getAccessToken();
        // 初始化分页参数
        int pageSize = 100;
        int currentPage = 1;
        int totalCount = 0;
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.cnap.returnorder.create");
        request.addApiParameter("simplify", "true");
        request.addApiParameter("order_lines", JSONUtil.toJsonStr(aliexpressReturnInstockDTO.getOrderLines()));
        request.addApiParameter("extend_props", JSONUtil.toJsonStr(aliexpressReturnInstockDTO.getExtendProps()));
        request.addApiParameter("return_order", JSONUtil.toJsonStr(aliexpressReturnInstockDTO.getReturnOrder()));
        IopResponse response = client.execute(request, accessToken, Protocol.TOP);
        log.warn("菜鸟仓退货入库单回参{}",JSONUtil.toJsonStr(response.getBody()));
        return JSON.parseObject(response.getBody(),new TypeReference<ApiReturnOrderResponseDTO>() {}.getType());
    }

    public ApiInventoryResponseDTO getInventory(AliexpressAuthDTO aliexpressAuthDTO) throws ApiException {
        String url = aliexpressAuthDTO.getUrl();
//        if (!BusinessCommonConstants.hasProfile("prod")) {
//            url = url + "/sandbox";
//        }
        String appKey = aliexpressAuthDTO.getAppKey();
        String appSecret = aliexpressAuthDTO.getAppSecret();
        String accessToken = aliexpressAuthDTO.getAccessToken();
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        List<ApiInventoryResponseDTO.Result.Data.ItemsDTO> itemsDTOS = new ArrayList<>();
        Integer pageSize = 100;
        Integer currentPage = 1;
        Integer totalCount = 0;
        do {
            IopRequest request = new IopRequest();
            request.setApiName("cainiao.cnap.stock.query");
            request.addApiParameter("simplify", "true");
            request.addApiParameter("owner_code", aliexpressAuthDTO.getOwnerCode());
            request.addApiParameter("page_size", String.valueOf(pageSize));
            request.addApiParameter("page", String.valueOf(currentPage));

            IopResponse response = client.execute(request, accessToken, Protocol.TOP);
            ApiInventoryResponseDTO pageResponse = JSON.parseObject(
                    response.getBody(),
                    new TypeReference<ApiInventoryResponseDTO>() {}.getType()
            );
            if(!pageResponse.isSuccess()){
                return pageResponse;
            }

            // 第一次请求时获取总记录数
            if (totalCount == 0 && pageResponse.getResult().getData().getTotalCount() > 0) {
                totalCount = pageResponse.getResult().getData().getTotalCount();
            }

            // 合并当页数据
            if (pageResponse.getResult().getData().getItems() != null) {
                itemsDTOS.addAll(pageResponse.getResult().getData().getItems());
            }

            // 计算剩余页数
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            if (currentPage >= totalPages) break;

            currentPage++;  // 翻到下一页

        } while (true);
        ApiInventoryResponseDTO apiInventoryResponseDTO = new ApiInventoryResponseDTO();
        apiInventoryResponseDTO.setDataList(itemsDTOS);
        apiInventoryResponseDTO.setResult(new ApiInventoryResponseDTO.Result(new ApiInventoryResponseDTO.Result.Data(),true));
        return apiInventoryResponseDTO;
    }
}
