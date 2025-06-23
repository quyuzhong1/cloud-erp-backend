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
import com.erp.wms.aliexpress.model.ApiResponseDTO;
import com.erp.wms.aliexpress.model.product.AliexpressProductDTO;
import com.erp.wms.aliexpress.util.ApiException;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
public class AliexpressWarehouseService {

    private static String CLIENT_ID = "clientId";
    private static String CLIENT_SECRET = "clientSecret";

    public static void main(String[] args) throws ApiException, JsonProcessingException {
        String url = "https://api-sg.aliexpress.com/sandbox";
        String appkey = "503630";
        String appSecret = "PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ";
        String accessToken = "50000701530cnHtbirhzrd7ijPeou2emiSIajwwCg118389611msriNQyROLyoVZ1y1j";
        IopClient client = new IopClientImpl(url, appkey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.cnap.singleitem.synchronize");
        request.addApiParameter("action_type", "add");
        request.addApiParameter("simplify", "true");
        AliexpressProductDTO.ProductDTO aliexpressProductDTO = new AliexpressProductDTO.ProductDTO();
        aliexpressProductDTO.setItemCode("0044");
        aliexpressProductDTO.setItemName("YN50MM F1.8 C");
        aliexpressProductDTO.setBarCode("0044");
        aliexpressProductDTO.setItemType("ZC");
        aliexpressProductDTO.setHeight(new BigDecimal("1.1"));
        aliexpressProductDTO.setWidth(new BigDecimal("3.3"));
        aliexpressProductDTO.setLength(new BigDecimal("2.2"));
        aliexpressProductDTO.setNetWeight(new BigDecimal("1.11"));
        aliexpressProductDTO.setGrossWeight(new BigDecimal("0.1"));
        request.addApiParameter("item", JSONUtil.toJsonStr(aliexpressProductDTO));
        request.addApiParameter("owner_code", "17379911544");
        request.addApiParameter("warehouse_code", "STB");
        IopResponse response = client.execute(request, accessToken, Protocol.TOP);
        String result = response.getBody();
        System.out.println(response.getBody());
        ApiResponseDTO apiResponseDTO = JSON.parseObject(result,new TypeReference<ApiResponseDTO>() {}.getType());

        System.out.println(123);
    }

    public ApiResponseDTO pushListing(AliexpressProductDTO aliexpressProductDTO) throws ApiException {
        AliexpressAuthDTO aliexpressAuthDTO = new AliexpressAuthDTO();
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

}
