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
import com.erp.wms.aliexpress.model.order.AliexpressCancelOrderDTO;
import com.erp.wms.aliexpress.model.order.AliexpressOrderDTO;
import com.erp.wms.aliexpress.model.order.ApiOrderResponseDTO;
import com.erp.wms.aliexpress.model.ApiResponseDTO;
import com.erp.wms.aliexpress.model.product.AliexpressProductDTO;
import com.erp.wms.aliexpress.util.ApiException;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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

    public static void main(String[] args) throws ApiException, JsonProcessingException {
        AliexpressWarehouseService aliexpressWarehouseService = new AliexpressWarehouseService();
        AliexpressAuthDTO aliexpressAuthDTO = new AliexpressAuthDTO();
        aliexpressAuthDTO.setUrl("https://api-sg.aliexpress.com/sandbox");
        aliexpressAuthDTO.setAppKey("503630");
        aliexpressAuthDTO.setAppSecret("PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ");
        aliexpressAuthDTO.setAccessToken("50000701530cnHtbirhzrd7ijPeou2emiSIajwwCg118389611msriNQyROLyoVZ1y1j");
        AliexpressOrderDTO aliexpressCancelOrderDTO = new AliexpressOrderDTO();
        aliexpressCancelOrderDTO.setAliexpressAuthDTO(aliexpressAuthDTO);
        aliexpressCancelOrderDTO.setDeliveryOrder(AliexpressOrderDTO.DeliveryOrder.builder()
                        .orderType("JYCK")
                        .ownerCode("17379911544")
                        .receiverInfo(AliexpressOrderDTO.DeliveryOrder.ReceiverInfoDTO.builder()
                                .countryCode("US")
                                .build())
                        .deliveryOrderCode("TESTWJ062504")
                        .warehouseCode("STB")
                        .shopNick("测试店铺")
                        .logisticsCode("other")
                        .createTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .sourcePlatformCode("AE")
                        .expressCode("test-062504")
                .build());
        aliexpressCancelOrderDTO.setOrderLines(Arrays.asList(AliexpressOrderDTO.OrderLines.builder()
                        .inventoryType("1")
                        .planQty(2)
                        .ownerCode("17379911544")
                        .itemCode("12000043163694871")
//                        .itemId(1129930008)
                .build()));
        aliexpressCancelOrderDTO.setExtendProps(AliexpressOrderDTO.ExtendProps.builder()
                        .merchantType("POP")
                        .printInfo("https://cno-oss.oss-cn-zhangjiakou.aliyuncs.com/aePopDeliveryFlag/FB1046000016902640596-20250619143440995.pdf?Expires=1752906881&OSSAccessKeyId=LTAI5tLxwRuzKhwK2qzxjuCE&Signature=W%2BcwRRR5QpVHiI1MUCTS87ZVz6M%3D")
                .build());
        ApiOrderResponseDTO apiOrderResponseDTO = aliexpressWarehouseService.createOutbound(aliexpressCancelOrderDTO);
        System.out.println(apiOrderResponseDTO);
    }

//        public static void main(String[] args) throws ApiException, JsonProcessingException {
//        AliexpressWarehouseService aliexpressWarehouseService = new AliexpressWarehouseService();
//        AliexpressAuthDTO aliexpressAuthDTO = new AliexpressAuthDTO();
//        aliexpressAuthDTO.setUrl("https://api-sg.aliexpress.com/sandbox");
//        aliexpressAuthDTO.setAppKey("503630");
//        aliexpressAuthDTO.setAppSecret("PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ");
//        aliexpressAuthDTO.setAccessToken("50000701530cnHtbirhzrd7ijPeou2emiSIajwwCg118389611msriNQyROLyoVZ1y1j");
//        AliexpressCancelOrderDTO aliexpressCancelOrderDTO = new AliexpressCancelOrderDTO();
//        aliexpressCancelOrderDTO.setAliexpressAuthDTO(aliexpressAuthDTO);
//            aliexpressCancelOrderDTO.setOrderId("LBX0395442105096030");
//            aliexpressCancelOrderDTO.setOwnerCode("17379911544");
//            aliexpressCancelOrderDTO.setWarehouseCode("STB");
//            aliexpressCancelOrderDTO.setOrderType("JYCK");
//            aliexpressCancelOrderDTO.setOrderCode("TEST13456");
//        ApiOrderResponseDTO apiOrderResponseDTO = aliexpressWarehouseService.cancelOutbound(aliexpressCancelOrderDTO);
//        System.out.println(apiOrderResponseDTO);
//    }

    public ApiResponseDTO pushListing(AliexpressProductDTO aliexpressProductDTO) throws ApiException {
        log.warn("菜鸟仓推送货品,{}",JSONUtil.toJsonStr(aliexpressProductDTO));
        AliexpressAuthDTO aliexpressAuthDTO = aliexpressProductDTO.getAliexpressAuthDTO();
        String url = aliexpressAuthDTO.getUrl();
        if (!BusinessCommonConstants.hasProfile("prod")) {
            url = url + "/sandbox";
            aliexpressProductDTO.setWarehouseCode("STB");
        }else{
            aliexpressProductDTO.setWarehouseCode("other");
        }
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
        log.warn("菜鸟仓创建出库单,{}",JSONUtil.toJsonStr(aliexpressOrderDTO));
        AliexpressAuthDTO aliexpressAuthDTO = aliexpressOrderDTO.getAliexpressAuthDTO();
        String url = aliexpressAuthDTO.getUrl();
//        if (!BusinessCommonConstants.hasProfile("prod")) {
//            url = url + "/sandbox";
//        }
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

    public ApiOrderResponseDTO cancelOutbound(AliexpressCancelOrderDTO aliexpressCancelOrderDTO) throws ApiException {
        log.warn("菜鸟仓取消出库单,{}",JSONUtil.toJsonStr(aliexpressCancelOrderDTO));
        AliexpressAuthDTO aliexpressAuthDTO = aliexpressCancelOrderDTO.getAliexpressAuthDTO();
        String url = aliexpressAuthDTO.getUrl();
        if (!BusinessCommonConstants.hasProfile("prod")) {
            url = url + "/sandbox";
        }
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
        return JSON.parseObject(response.getBody(),new TypeReference<ApiOrderResponseDTO>() {}.getType());
    }
}
